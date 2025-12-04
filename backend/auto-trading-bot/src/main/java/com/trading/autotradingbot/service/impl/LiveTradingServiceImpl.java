package com.trading.autotradingbot.service.impl;

import com.trading.autotradingbot.entity.Account;
import com.trading.autotradingbot.entity.BarData;
import com.trading.autotradingbot.entity.BotConfig;
import com.trading.autotradingbot.entity.enums.*;
import com.trading.autotradingbot.repository.AccountRepository;
import com.trading.autotradingbot.repository.BarDataRepository;
import com.trading.autotradingbot.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.time.LocalDateTime;
import java.util.List;

import static com.trading.autotradingbot.service.impl.TrainingServiceImpl.INITIAL_BAR_LIMIT;

@Service
public class LiveTradingServiceImpl {
    private static final Logger log = LoggerFactory.getLogger(LiveTradingServiceImpl.class);
    // MVP Constant
    private static final Long accountId = 1L;

    @Value("${bot.snapshot.rate.ms}")
    private long snapshotRateMs;

    private final BotManagementService botManagementService;
    private final MarketDataProvider marketDataProvider;
    private final TradingStrategyService tradingStrategy;
    private final OrderExecutionHandler orderExecutionHandler;
    private final SnapshotService snapshotService;
    private final AccountRepository accountRepository;
    private final BarDataRepository barDataRepository;

    public LiveTradingServiceImpl(
            BotManagementService botManagementService,
            MarketDataProvider marketDataProvider,
            TradingStrategyService tradingStrategy,
            OrderExecutionHandler orderExecutionHandler,
            SnapshotService snapshotService,
            AccountRepository accountRepository,
            BarDataRepository barDataRepository) {
        this.botManagementService = botManagementService;
        this.marketDataProvider = marketDataProvider;
        this.tradingStrategy = tradingStrategy;
        this.orderExecutionHandler = orderExecutionHandler;
        this.snapshotService = snapshotService;
        this.accountRepository = accountRepository;
        this.barDataRepository = barDataRepository;
    }

    /**
     *  CORE TRADING LOOP: Runs frequently (every 5 seconds) to check price and execute trades.
     */
    @Scheduled(fixedRate = 5000)
    private void runLiveTradingLoop() {
        Account account = accountRepository.findById(accountId)
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
            Signal signal = tradingStrategy.getSignal(price, timestamp);

            if (signal == Signal.BUY) {
                orderExecutionHandler.executeBuy(accountId, symbol, price, tradingStrategy.getStrategyName());
            } else if (signal == Signal.SELL) {
                orderExecutionHandler.executeSell(accountId, symbol, price, tradingStrategy.getStrategyName());
            }

            snapshotService.captureSnapshot(accountId, price, timestamp.toLocalDateTime());

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
}