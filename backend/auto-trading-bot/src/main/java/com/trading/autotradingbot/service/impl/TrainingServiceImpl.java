package com.trading.autotradingbot.service.impl;

import com.trading.autotradingbot.entity.Account;
import com.trading.autotradingbot.entity.BarData;
import com.trading.autotradingbot.entity.enums.AccountType;
import com.trading.autotradingbot.entity.enums.Signal;
import com.trading.autotradingbot.repository.*;
import com.trading.autotradingbot.service.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Service
public class TrainingServiceImpl implements TrainingService {
    private final AccountRepository accountRepository;
    private final TradeRepository tradeRepository;
    private final PortfolioRepository portfolioRepository;
    private final SnapshotRepository snapshotRepository;
    private final BarDataRepository barDataRepository;

    private final MarketDataProvider marketDataProvider;
    private final TradingStrategyService tradingStrategy;
    private final OrderExecutionHandler orderExecutionHandler;
    private final SnapshotService snapshotService;

    static final int INITIAL_BAR_LIMIT = 1000;
    private static final BigDecimal STARTING_CAPITAL = new BigDecimal("10000.00");


    // Constructor Injection (all dependencies)
    public TrainingServiceImpl(AccountRepository accountRepository, TradeRepository tradeRepository,
                               PortfolioRepository portfolioRepository, SnapshotRepository snapshotRepository,
                               BarDataRepository barDataRepository, MarketDataProvider marketDataProvider,
                               TradingStrategyService tradingStrategy, OrderExecutionHandler orderExecutionHandler,
                               SnapshotService snapshotService) {
        this.accountRepository = accountRepository;
        this.tradeRepository = tradeRepository;
        this.portfolioRepository = portfolioRepository;
        this.snapshotRepository = snapshotRepository;
        this.barDataRepository = barDataRepository;
        this.marketDataProvider = marketDataProvider;
        this.tradingStrategy = tradingStrategy;
        this.orderExecutionHandler = orderExecutionHandler;
        this.snapshotService = snapshotService;
    }

    /**
     * Wipes all data associated with the backtest account (ID 2).
     */
    @Override
    @Transactional
    public void resetData(Long accountId) {
        tradeRepository.deleteAllByAccountId(accountId);
        portfolioRepository.deleteAllByAccountId(accountId);
        snapshotRepository.deleteAllByAccountId(accountId);

        accountRepository.resetAccount(accountId, STARTING_CAPITAL);
    }

    /**
     * Executes a simulated trading run (backtest) using historical market data.
     * <p>
     * This method ensures initialization safety by checking the account type,
     * handles data caching, and runs the core strategy loop, recording performance
     * metrics after every bar iteration.
     * </p>
     *
     * @param accountId The ID of the target account (must be BACKTEST ID 2).
     * @param symbol The crypto asset symbol (e.g., "BTCUSDT").
     * @param interval The kline interval (e.g., "1h").
     * @throws SecurityException if the method is called with a LIVE account ID.
     * @throws IllegalStateException if initialization fails or data cannot be fetched.
     */
    @Override
    @Transactional
    public void runBacktest(Long accountId, String symbol, String interval) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalStateException("Invalid Account ID."));

        if (account.getAccountType() == AccountType.LIVE) {
            throw new SecurityException("Attempted to run backtest simulation on LIVE trading account. Operation aborted.");
        }
        resetData(accountId);

        if (barDataRepository.isCacheEmpty(symbol, interval)) {
            List<BarData> freshData = marketDataProvider.getHistoricalData(symbol, interval, INITIAL_BAR_LIMIT);
            barDataRepository.saveAll(freshData);
        }

        List<BarData> historicalBars = barDataRepository.findAllBySymbolAndInterval(symbol, interval);
        tradingStrategy.initializeSeries(historicalBars);
        int minBarsForAnalysis = tradingStrategy.getMinBarsForAnalysis();

        for (int i = 0; i < historicalBars.size(); i++) {
            BarData currentBar = historicalBars.get(i);

            BigDecimal price = currentBar.getClosePrice();
            ZonedDateTime timestamp = ZonedDateTime.of(currentBar.getOpenTime(), ZoneId.systemDefault());

            Signal signal = tradingStrategy.getSignal(price, timestamp);

            if (i >= minBarsForAnalysis) {
                if (signal == Signal.BUY) {
                    orderExecutionHandler.executeBuy(accountId, symbol, price, tradingStrategy.getStrategyName());
                } else if (signal == Signal.SELL) {
                    orderExecutionHandler.executeSell(accountId, symbol, price, tradingStrategy.getStrategyName());
                }
            }

            snapshotService.captureSnapshot(accountId, price, currentBar.getOpenTime());
        }
    }
}