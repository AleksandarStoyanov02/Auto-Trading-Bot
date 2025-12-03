package com.trading.autotradingbot.service.impl;

import com.trading.autotradingbot.entity.BarData;
import com.trading.autotradingbot.entity.Trade;
import com.trading.autotradingbot.entity.enums.TradeAction;
import com.trading.autotradingbot.repository.AccountRepository;
import com.trading.autotradingbot.repository.BarDataRepository;
import com.trading.autotradingbot.repository.TradeRepository;
import com.trading.autotradingbot.service.MarketDataProvider;
import com.trading.autotradingbot.service.TrainingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class TrainingServiceIntegrationTest {
    @Container
    static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:15");

    @MockitoBean
    private MarketDataProvider marketDataProvider;

    @Autowired
    private TrainingService trainingService;

    @Autowired
    private TradeRepository tradeRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    BarDataRepository barDataRepository;

    private static final Long BACKTEST_ACCOUNT_ID = 2L;
    private static final String SYMBOL = "TESTUSDT";
    private static final String INTERVAL = "1h";
    private static final BigDecimal INITIAL_CAPITAL = new BigDecimal("10000.00");

    @BeforeEach
    void setupDatabaseState() {
        Mockito.reset(marketDataProvider);
        accountRepository.resetAccount(BACKTEST_ACCOUNT_ID, INITIAL_CAPITAL);
        barDataRepository.deleteAllBySymbolAndInterval(SYMBOL, INTERVAL);
    }

    @Test
    void shouldExecuteBuyAndRecordTradeInBacktestMode() {
        // ARRANGE
        List<BarData> buySignalData = createMockBarsForBuySignal();

        when(marketDataProvider.getHistoricalData(anyString(), anyString(), anyInt()))
                .thenReturn(buySignalData);

        // ACT
        trainingService.runBacktest(BACKTEST_ACCOUNT_ID, SYMBOL, INTERVAL);

        // ASSERT

        List<Trade> trades = tradeRepository.findAllByAccountId(BACKTEST_ACCOUNT_ID);
        assertFalse(trades.isEmpty(), "At least one trade should have been executed.");

        BigDecimal finalBalance = accountRepository.getAccountBalance(BACKTEST_ACCOUNT_ID);
        assertTrue(finalBalance.compareTo(INITIAL_CAPITAL) < 0, "Final balance should be less than initial capital due to spend and fees.");
    }

    @Test
    void shouldExecuteBuyAndSellAndRealizeProfit() {
        List<BarData> buySellData = createMockBarsForBuyAndSellSignal();

        when(marketDataProvider.getHistoricalData(anyString(), anyString(), anyInt()))
                .thenReturn(buySellData);

        // ACT
        trainingService.runBacktest(BACKTEST_ACCOUNT_ID, SYMBOL, INTERVAL);

        // ASSERT

        List<Trade> trades = tradeRepository.findAllByAccountId(BACKTEST_ACCOUNT_ID);
        assertEquals(2, trades.size(), "Exactly two trades (BUY and SELL) should have been executed.");
        assertEquals(TradeAction.BUY, trades.get(1).getAction(), "The first trade should be a BUY.");
        assertEquals(TradeAction.SELL, trades.get(0).getAction(), "The final trade should be a SELL.");

        // Since we bought low (80) and sold high (130), a profit should be realized.
        BigDecimal finalBalance = accountRepository.getAccountBalance(BACKTEST_ACCOUNT_ID);

        assertTrue(finalBalance.compareTo(INITIAL_CAPITAL) > 0,
                "Final balance must be greater than starting capital, showing realized profit.");

        assertTrue(trades.getFirst().getProfitLoss().compareTo(BigDecimal.ZERO) > 0, "The SELL trade must record a profit.");
    }

    private List<BarData> createMockBarsForBuySignal() {
        // Create 15 bars at 100.00 (RSI 50 baseline) + 1 bar at 80.00 (RSI < 30)
        List<BarData> bars = new ArrayList<>();
        LocalDateTime baseTime = LocalDateTime.now().minusDays(1);
        BigDecimal neutralPrice = new BigDecimal("100.00");
        BigDecimal buyPrice = new BigDecimal("80.00");

        IntStream.range(0, 16).forEach(i -> {
            BigDecimal price = (i < 15) ? neutralPrice : buyPrice;

            bars.add(BarData.builder()
                    .symbol(TrainingServiceIntegrationTest.SYMBOL).interval(TrainingServiceIntegrationTest.INTERVAL)
                    .openTime(baseTime.plusHours(i))
                    .closePrice(price).openPrice(neutralPrice).highPrice(neutralPrice)
                    .lowPrice(price).volume(BigDecimal.TEN)
                    .build());
        });
        return bars;
    }

    // Inside TrainingServiceIntegrationTest.java

    private List<BarData> createMockBarsForBuyAndSellSignal() {
        // We need 15 neutral bars, 1 oversold bar (BUY), and 1 overbought bar (SELL).
        List<BarData> bars = new ArrayList<>();
        LocalDateTime baseTime = LocalDateTime.now().minusDays(1);

        // Baseline constants
        BigDecimal neutralPrice = new BigDecimal("100.00");
        BigDecimal buySignalPrice = new BigDecimal("80.00");  // RSI < 30
        BigDecimal sellSignalPrice = new BigDecimal("130.00"); // RSI > 70

        // 1. Generate 15 baseline bars (with small variance for stability)
        for (int i = 0; i < 15; i++) {
            BigDecimal price = (i % 2 == 0) ? new BigDecimal("100.50") : new BigDecimal("99.50");
            bars.add(BarData.builder()
                    .symbol(TrainingServiceIntegrationTest.SYMBOL).interval(TrainingServiceIntegrationTest.INTERVAL).openTime(baseTime.plusHours(i))
                    .closePrice(price).openPrice(neutralPrice)
                    .highPrice(neutralPrice.add(new BigDecimal("1.00"))).lowPrice(neutralPrice.subtract(new BigDecimal("1.00")))
                    .volume(BigDecimal.TEN).build());
        }

        // 2. Bar 16 (Signal: BUY) - Oversold
        bars.add(BarData.builder()
                .symbol(TrainingServiceIntegrationTest.SYMBOL).interval(TrainingServiceIntegrationTest.INTERVAL).openTime(baseTime.plusHours(15))
                .closePrice(buySignalPrice).openPrice(neutralPrice)
                .highPrice(neutralPrice).lowPrice(buySignalPrice)
                .volume(BigDecimal.TEN).build());

        // 3. Bar 17 (Signal: SELL) - Overbought
        bars.add(BarData.builder()
                .symbol(TrainingServiceIntegrationTest.SYMBOL).interval(TrainingServiceIntegrationTest.INTERVAL).openTime(baseTime.plusHours(16))
                .closePrice(sellSignalPrice).openPrice(buySignalPrice)
                .highPrice(sellSignalPrice).lowPrice(buySignalPrice)
                .volume(BigDecimal.TEN).build());

        return bars;
    }
}