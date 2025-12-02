package com.trading.autotradingbot.service.impl;

import com.trading.autotradingbot.entity.BarData;
import com.trading.autotradingbot.entity.enums.Signal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RSIStrategyServiceTest {

    private RSIStrategyService strategyService;
    private ZonedDateTime testTime;
    private final String SYMBOL = "TESTUSDT";
    private final String INTERVAL = "1h";

    @BeforeEach
    void setUp() {
        strategyService = new RSIStrategyService();
        testTime = ZonedDateTime.of(LocalDateTime.of(2024, 1, 1, 0, 0), ZoneId.systemDefault());

        List<BarData> initialBars = new ArrayList<>();
        BigDecimal basePrice = new BigDecimal("100.00");

        for (int i = 0; i < 15; i++) {
            BigDecimal closePrice;
            if (i % 2 == 0) {
                closePrice = basePrice.subtract(new BigDecimal("0.50"));
            } else {
                closePrice = basePrice.add(new BigDecimal("0.50"));
            }

            initialBars.add(BarData.builder()
                    .symbol(SYMBOL)
                    .interval(INTERVAL)
                    .openTime(testTime.toLocalDateTime().plusHours(i))
                    .closePrice(closePrice)
                    .openPrice(basePrice)
                    .highPrice(basePrice.add(new BigDecimal("0.50")))
                    .lowPrice(basePrice.subtract(new BigDecimal("0.50")))
                    .volume(BigDecimal.ZERO)
                    .build());
        }

        strategyService.initializeSeries(initialBars);
    }

    @Test
    void testOversoldBuySignal() {
        // ARRANGE
        BigDecimal extremelyLowPrice = new BigDecimal("80.00");

        // ACT
        Signal signal = strategyService.getSignal(extremelyLowPrice, testTime.plusHours(15));

        // ASSERT
        assertEquals(Signal.BUY, signal, "Should return BUY when RSI drops below 30.");
    }

    @Test
    void testOverboughtSellSignal() {
        // ARRANGE
        BigDecimal extremelyHighPrice = new BigDecimal("120.00");

        // ACT
        Signal signal = strategyService.getSignal(extremelyHighPrice, testTime.plusHours(15));

        // ASSERT
        assertEquals(Signal.SELL, signal, "Should return SELL when RSI rises above 70.");
    }

    @Test
    void testHoldSignal() {
        // ARRANGE
        BigDecimal neutralPrice = new BigDecimal("100.001");

        // ACT
        Signal signal = strategyService.getSignal(neutralPrice, testTime.plusHours(15));

        // ASSERT
        assertEquals(Signal.HOLD, signal, "Should return HOLD when RSI is between 30 and 70.");
    }
}