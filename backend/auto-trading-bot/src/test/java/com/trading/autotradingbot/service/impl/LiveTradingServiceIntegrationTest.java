package com.trading.autotradingbot.service.impl;

import com.trading.autotradingbot.entity.BarData;
import com.trading.autotradingbot.entity.Trade;
import com.trading.autotradingbot.entity.enums.BotStatus;
import com.trading.autotradingbot.entity.enums.TradeAction;
import com.trading.autotradingbot.entity.enums.TradingMode;
import com.trading.autotradingbot.repository.AccountRepository;
import com.trading.autotradingbot.repository.BarDataRepository;
import com.trading.autotradingbot.repository.BotConfigRepository;
import com.trading.autotradingbot.repository.TradeRepository;
import com.trading.autotradingbot.service.MarketDataProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
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
class LiveTradingServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:15");

    @MockitoBean
    private MarketDataProvider marketDataProvider;

    @Autowired private LiveTradingServiceImpl liveTradingService;
    @Autowired private AccountRepository accountRepository;
    @Autowired private BotConfigRepository botConfigRepository;
    @Autowired private BarDataRepository barDataRepository;
    @Autowired private TradeRepository tradeRepository;

    private static final Long LIVE_ACCOUNT_ID = 1L;
    private static final String SYMBOL = "BTCUSDT";
    private static final String INTERVAL = "1h";
    private static final BigDecimal INITIAL_CAPITAL = new BigDecimal("10000.00");
    private static final int WARMUP_BARS_COUNT = 15;

    @BeforeEach
    void setupDatabaseAndCache() {
        accountRepository.resetAccount(LIVE_ACCOUNT_ID, INITIAL_CAPITAL);
        tradeRepository.deleteAllByAccountId(LIVE_ACCOUNT_ID);
        barDataRepository.deleteAllBySymbolAndInterval(SYMBOL, INTERVAL);

        botConfigRepository.updateSymbol(SYMBOL);
        botConfigRepository.updateMode(TradingMode.TRADING);
        botConfigRepository.updateStatus(BotStatus.IDLE);
    }

    @Test
    @Transactional
    void shouldInitializeStrategyAndExecuteBuySignal() {
        List<BarData> initialBars = createInitialHistoricalBars(WARMUP_BARS_COUNT, SYMBOL, INTERVAL);
        when(marketDataProvider.getHistoricalData(anyString(), anyString(), anyInt()))
                .thenReturn(initialBars);

        BigDecimal buyPrice = new BigDecimal("80.00"); // Price to trigger BUY signal
        when(marketDataProvider.getLivePrice(anyString())).thenReturn(buyPrice);

        liveTradingService.startLiveTrading(SYMBOL, INTERVAL);

        assertEquals(BotStatus.RUNNING, botConfigRepository.getConfig().getStatus(), "Status should be RUNNING after calling startLiveTrading.");
        assertEquals(WARMUP_BARS_COUNT, barDataRepository.findAllBySymbolAndInterval(SYMBOL, INTERVAL).size(), "DB cache should hold 14 bars after initialization.");

        liveTradingService.runLiveTradingLoop();

        List<Trade> trades = tradeRepository.findAllByAccountId(LIVE_ACCOUNT_ID);
        assertFalse(trades.isEmpty(), "A BUY trade should have been executed.");
        assertEquals(TradeAction.BUY, trades.getFirst().getAction());

        BigDecimal finalCashBalance = accountRepository.getAccountBalance(LIVE_ACCOUNT_ID);
        assertTrue(finalCashBalance.compareTo(INITIAL_CAPITAL) < 0, "Cash balance must reflect the asset purchase.");

        verify(marketDataProvider, times(1)).getHistoricalData(anyString(), anyString(), anyInt());
    }


    private List<BarData> createInitialHistoricalBars(int count, String symbol, String interval) {
        List<BarData> bars = new ArrayList<>();
        LocalDateTime baseTime = LocalDateTime.now().minusDays(1);
        BigDecimal neutralPrice = new BigDecimal("100.00");

        IntStream.range(0, count).forEach(i -> {
            // Create bars with slight variance for stability
            BigDecimal price = (i % 2 == 0) ? new BigDecimal("100.50") : new BigDecimal("99.50");
            bars.add(BarData.builder()
                    .symbol(symbol)
                    .interval(interval)
                    .openTime(baseTime.plusHours(i))
                    .closePrice(price)
                    .openPrice(neutralPrice)
                    .highPrice(neutralPrice.add(BigDecimal.ONE))
                    .lowPrice(neutralPrice.subtract(BigDecimal.ONE))
                    .volume(BigDecimal.TEN)
                    .build());
        });
        return bars;
    }
}