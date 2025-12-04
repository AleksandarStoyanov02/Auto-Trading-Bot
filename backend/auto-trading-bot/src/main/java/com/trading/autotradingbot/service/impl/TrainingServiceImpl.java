package com.trading.autotradingbot.service.impl;

import com.trading.autotradingbot.entity.Account;
import com.trading.autotradingbot.entity.BarData;
import com.trading.autotradingbot.entity.PortfolioHolding;
import com.trading.autotradingbot.entity.enums.AccountType;
import com.trading.autotradingbot.entity.enums.Signal;
import com.trading.autotradingbot.exception.TradeExecutionConstraintException;
import com.trading.autotradingbot.repository.*;
import com.trading.autotradingbot.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TrainingServiceImpl implements TrainingService {
    private static final Logger log = LoggerFactory.getLogger(TrainingServiceImpl.class);
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
    private static final BigDecimal STOP_LOSS_THRESHOLD = new BigDecimal("0.98"); // 98% of cost basis (2% drop)
    private static final int SCALE = 8;


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

            Optional<PortfolioHolding> holdingOpt = portfolioRepository.findByIdAndSymbol(accountId, symbol);
            boolean positionOpen = holdingOpt.isPresent();

            Signal signal = tradingStrategy.getSignal(price, timestamp);

            if (i >= minBarsForAnalysis) {

                if (positionOpen && isStopLossTriggered(holdingOpt.get(), price)) {
                    orderExecutionHandler.executeSell(accountId, symbol, price, "STOP_LOSS");
                } else {
                    try {
                        if (signal == Signal.BUY && !positionOpen) {
                            orderExecutionHandler.executeBuy(accountId, symbol, price, tradingStrategy.getStrategyName());
                        } else if (signal == Signal.SELL && positionOpen) {
                            orderExecutionHandler.executeSell(accountId, symbol, price, tradingStrategy.getStrategyName());
                        }
                    } catch (TradeExecutionConstraintException e) {
                        log.debug("Trade skipped for account {}: {}", accountId, e.getMessage());
                    }
                }
            }

            snapshotService.captureSnapshot(accountId, price, currentBar.getOpenTime());
        }
    }

    /**
     * Checks if the current price has dropped 2% or more below the average buy price.
     */
    private boolean isStopLossTriggered(PortfolioHolding holding, BigDecimal currentPrice) {
        BigDecimal avgBuyPrice = holding.getAvgBuyPrice();
        BigDecimal triggerPrice = avgBuyPrice.multiply(STOP_LOSS_THRESHOLD).setScale(SCALE, RoundingMode.HALF_UP);

        return currentPrice.compareTo(triggerPrice) <= 0;
    }
}