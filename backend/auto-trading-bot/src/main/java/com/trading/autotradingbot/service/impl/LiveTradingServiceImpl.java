package com.trading.autotradingbot.service.impl;

import com.trading.autotradingbot.entity.Account;
import com.trading.autotradingbot.entity.BarData;
import com.trading.autotradingbot.entity.BotConfig;
import com.trading.autotradingbot.entity.PortfolioHolding;
import com.trading.autotradingbot.entity.enums.*;
import com.trading.autotradingbot.exception.TradeExecutionConstraintException;
import com.trading.autotradingbot.repository.AccountRepository;
import com.trading.autotradingbot.repository.BarDataRepository;
import com.trading.autotradingbot.repository.PortfolioRepository;
import com.trading.autotradingbot.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZonedDateTime;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.trading.autotradingbot.common.AccountConstants.*;
import static com.trading.autotradingbot.service.impl.TrainingServiceImpl.INITIAL_BAR_LIMIT;

@Service
public class LiveTradingServiceImpl {
    private static final Logger log = LoggerFactory.getLogger(LiveTradingServiceImpl.class);

    private final BotManagementService botManagementService;
    private final MarketDataProvider marketDataProvider;
    private final TradingStrategyService tradingStrategy;
    private final OrderExecutionHandler orderExecutionHandler;
    private final SnapshotService snapshotService;
    private final AccountRepository accountRepository;
    private final BarDataRepository barDataRepository;
    private final PortfolioRepository portfolioRepository;

    public LiveTradingServiceImpl(
            BotManagementService botManagementService,
            MarketDataProvider marketDataProvider,
            TradingStrategyService tradingStrategy,
            OrderExecutionHandler orderExecutionHandler,
            SnapshotService snapshotService,
            AccountRepository accountRepository,
            BarDataRepository barDataRepository,
            PortfolioRepository portfolioRepository) {
        this.botManagementService = botManagementService;
        this.marketDataProvider = marketDataProvider;
        this.tradingStrategy = tradingStrategy;
        this.orderExecutionHandler = orderExecutionHandler;
        this.snapshotService = snapshotService;
        this.accountRepository = accountRepository;
        this.barDataRepository = barDataRepository;
        this.portfolioRepository = portfolioRepository;
    }

    /**
     *  CORE TRADING LOOP: Runs frequently (every 5 seconds) to check price and execute trades.
     */
    @Scheduled(fixedRate = 5000)
    private void runLiveTradingLoop() {
        Account account = accountRepository.findById(LIVE_ACCOUNT_ID)
                .orElseThrow(() -> new IllegalStateException("Invalid Account ID."));

        if (account.getAccountType() != AccountType.LIVE) {
            throw new SecurityException("Attempted to run Live Trading Mode for a Backtest valid account");
        }

        BotConfig config = botManagementService.getConfig();

        if (config.getStatus() != BotStatus.RUNNING || config.getTradingMode() != TradingMode.TRADING) {
            log.info("Currently used Bot is either in Training mode or Paused");
            return;
        }

        String symbol = config.getSelectedSymbol();
        ZonedDateTime timestamp = ZonedDateTime.now();

        try {
            BigDecimal price = marketDataProvider.getLivePrice(symbol);
            Optional<PortfolioHolding> holdingOpt = portfolioRepository.findByIdAndSymbol(LIVE_ACCOUNT_ID, symbol); // <<< NEW: Check Position
            boolean positionOpen = holdingOpt.isPresent();

            Signal signal = tradingStrategy.getSignal(price, timestamp);

            if (positionOpen && isStopLossTriggered(holdingOpt.get(), price)) {
                orderExecutionHandler.executeSell(LIVE_ACCOUNT_ID, symbol, price, "STOP_LOSS");
            } else {
                try {
                    if (signal == Signal.BUY && !positionOpen) {
                        orderExecutionHandler.executeBuy(LIVE_ACCOUNT_ID, symbol, price, tradingStrategy.getStrategyName());
                    } else if (signal == Signal.SELL && positionOpen) {
                        orderExecutionHandler.executeSell(LIVE_ACCOUNT_ID, symbol, price, tradingStrategy.getStrategyName());
                    }
                } catch (TradeExecutionConstraintException e) {
                    log.debug("Trade skipped for account {}: {}", LIVE_ACCOUNT_ID, e.getMessage());
                }
            }

            snapshotService.captureSnapshot(LIVE_ACCOUNT_ID, price, timestamp.toLocalDateTime());

        } catch (RuntimeException e) {
            log.error("Live Trading Loop encountered runtime exception: {}", e.getMessage(), e);
            botManagementService.setStatus(BotStatus.PAUSED);
        }
    }

    /**
     * ANALYTICS LOOP: Runs less frequently (configurable, default: 30 seconds)
     * to ensure the performance chart has continuous data points, even during HOLD periods.
     */
    @Scheduled(fixedRateString = "${bot.snapshot.rate.ms}")
    public void capturePeriodicSnapshot() {
        BotConfig config = botManagementService.getConfig();

        if (config.getStatus() != BotStatus.RUNNING || config.getTradingMode() != TradingMode.TRADING) {
            return;
        }

        try {
            BigDecimal currentPrice = marketDataProvider.getLivePrice(config.getSelectedSymbol());
            LocalDateTime now = LocalDateTime.now();

            snapshotService.captureSnapshot(1L, currentPrice, now);
        } catch (RuntimeException e) {
            log.error("Periodic Snapshot Failed: {}", e.getMessage(), e);
        }
    }

    /**
     * Public entry point for the BotController to start the live trading loop.
     * This method ensures the strategy is warmed up and sets the bot state to RUNNING.
     */
    public void startLiveTrading(String symbol, String interval) {
        if (barDataRepository.isCacheEmpty(symbol, interval)) {
            List<BarData> freshData = marketDataProvider.getHistoricalData(symbol, interval, INITIAL_BAR_LIMIT);
            barDataRepository.saveAll(freshData);
        }

        List<BarData> historicalBars = barDataRepository.findAllBySymbolAndInterval(symbol, interval);
        if (historicalBars.size() < tradingStrategy.getMinBarsForAnalysis()) {
            throw new IllegalStateException("Not enough historical data to initialize strategy ("
                    + historicalBars.size() + " bars found, need " + tradingStrategy.getMinBarsForAnalysis() + ").");
        }
        tradingStrategy.initializeSeries(historicalBars);

        botManagementService.changeSymbol(symbol);
        botManagementService.setStatus(BotStatus.RUNNING);

        runLiveTradingLoop();

        log.info("Live Trading started for {} ({} interval).", symbol, interval);
    }

    /**
     * Helper method to check if the current price has dropped 2% or more below the average buy price.
     */
    private boolean isStopLossTriggered(PortfolioHolding holding, BigDecimal currentPrice) {
        BigDecimal avgBuyPrice = holding.getAvgBuyPrice();
        BigDecimal triggerPrice = avgBuyPrice.multiply(STOP_LOSS_THRESHOLD).setScale(SCALE, RoundingMode.HALF_UP);

        return currentPrice.compareTo(triggerPrice) <= 0;
    }
}