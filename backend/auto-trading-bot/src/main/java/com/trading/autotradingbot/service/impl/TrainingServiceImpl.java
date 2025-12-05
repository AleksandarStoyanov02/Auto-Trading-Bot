package com.trading.autotradingbot.service.impl;

import com.trading.autotradingbot.common.AccountConstants;
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

import static com.trading.autotradingbot.common.AccountConstants.SCALE;
import static com.trading.autotradingbot.common.AccountConstants.STOP_LOSS_THRESHOLD;

@Service
public class TrainingServiceImpl implements TrainingService {
    private static final Logger log = LoggerFactory.getLogger(TrainingServiceImpl.class);
    private final AccountRepository accountRepository;
    private final PortfolioRepository portfolioRepository;
    private final BarDataRepository barDataRepository;

    private final MarketDataProvider marketDataProvider;
    private final TradingStrategyService tradingStrategy;
    private final OrderExecutionHandler orderExecutionHandler;
    private final SnapshotService snapshotService;
    private final AccountResetService accountResetService;

    static final int INITIAL_BAR_LIMIT = 1000;


    public TrainingServiceImpl(AccountRepository accountRepository, PortfolioRepository portfolioRepository,
                               BarDataRepository barDataRepository, MarketDataProvider marketDataProvider,
                               TradingStrategyService tradingStrategy, OrderExecutionHandler orderExecutionHandler,
                               SnapshotService snapshotService, AccountResetService accountResetService) {
        this.accountRepository = accountRepository;
        this.portfolioRepository = portfolioRepository;
        this.barDataRepository = barDataRepository;
        this.marketDataProvider = marketDataProvider;
        this.tradingStrategy = tradingStrategy;
        this.orderExecutionHandler = orderExecutionHandler;
        this.snapshotService = snapshotService;
        this.accountResetService = accountResetService;
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

        accountResetService.resetAllAccountData(AccountConstants.BACKTEST_ACCOUNT_ID, AccountConstants.DEFAULT_CAPITAL);

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

        PortfolioHolding finalHolding = portfolioRepository.findByIdAndSymbol(accountId, symbol).orElse(null);

        if (finalHolding != null) {
            BarData lastBar = historicalBars.getLast();

            orderExecutionHandler.executeSell(accountId, symbol, lastBar.getClosePrice(), "FINAL_LIQUIDATION");
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