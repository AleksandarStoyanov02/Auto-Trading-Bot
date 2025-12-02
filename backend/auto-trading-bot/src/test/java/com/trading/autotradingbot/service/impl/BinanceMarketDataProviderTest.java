package com.trading.autotradingbot.service.impl;

import com.trading.autotradingbot.entity.BarData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.Instant;
import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BinanceMarketDataProviderTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private BinanceMarketDataProvider provider;

    private final String BINANCE_LIVE_PRICE_RESPONSE =
            "{\"symbol\":\"BTCUSDT\",\"price\":\"65432.12\"}";

    private final List<List<Object>> MOCK_KLINES_RESPONSE = List.of(
            // [OpenTime, OpenPrice, HighPrice, LowPrice, ClosePrice, Volume]
            List.of(1678809600000L, "25000.00", "25500.00", "24900.00", "25200.00", "100.00"),
            List.of(1678813200000L, "25200.00", "25300.00", "25100.00", "25250.00", "80.00")
    );

    private final String TEST_SYMBOL = "BTCUSDT";
    private final String TEST_INTERVAL = "ONE_HOUR";

    @Test
    void shouldReturnCorrectBigDecimalPrice() {
        // ARRANGE
        when(restTemplate.getForObject(
                anyString(),
                eq(JsonNode.class)))
                .thenReturn(new ObjectMapper().valueToTree(
                        new ObjectMapper().readTree(BINANCE_LIVE_PRICE_RESPONSE)));

        // ACT
        BigDecimal result = provider.getLivePrice(TEST_SYMBOL);

        // ASSERT
        assertNotNull(result);
        assertEquals(new BigDecimal("65432.12"), result);
    }

    @Test
    void shouldParseHistoricalDataCorrectly() {
        // ARRANGE
        when(restTemplate.getForObject(
                anyString(),
                eq(List.class)))
                .thenReturn(MOCK_KLINES_RESPONSE);

        // ACT
        List<BarData> result = provider.getHistoricalData(TEST_SYMBOL, TEST_INTERVAL, 2);

        // ASSERT
        assertEquals(2, result.size());

        BarData firstBar = result.get(0);
        assertEquals(new BigDecimal("25200.00"), firstBar.getClosePrice());

        BarData secondBar = result.get(1);
        assertEquals(new BigDecimal("80.00"), secondBar.getVolume());

        LocalDateTime expectedTime = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(1678809600000L), ZoneId.systemDefault());
        assertEquals(expectedTime, firstBar.getOpenTime());

        assertEquals(TEST_SYMBOL, firstBar.getSymbol());
        assertEquals("1h", firstBar.getInterval());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionForInvalidInterval() {
        assertThrows(IllegalArgumentException.class,
                () -> provider.getHistoricalData(TEST_SYMBOL, "INVALID_INTERVAL", 100));
    }
}