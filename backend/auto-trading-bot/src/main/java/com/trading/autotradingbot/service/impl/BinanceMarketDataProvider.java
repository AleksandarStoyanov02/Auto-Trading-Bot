package com.trading.autotradingbot.service.impl;

import com.trading.autotradingbot.entity.BarData;
import com.trading.autotradingbot.entity.enums.KlineInterval;
import com.trading.autotradingbot.exception.BinanceApiException;
import com.trading.autotradingbot.service.MarketDataProvider;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class BinanceMarketDataProvider implements MarketDataProvider {
    private static final Logger log = LoggerFactory.getLogger(BinanceMarketDataProvider.class);

    // Have some issues with Value annotation in this class, will be fixed later
    // @Value("${binance.api.url}")
    private final String baseUrl = "https://api.binance.com/api/v3";

    private final RestTemplate restTemplate;

    public BinanceMarketDataProvider(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Fetches the current live price for a symbol.
     */
    @Override
    public BigDecimal getLivePrice(String symbol) {
        String url = baseUrl + "/ticker/price?symbol=" + symbol;

        try {
            // Response: {"symbol":"BTCUSDT","price":"60000.00"}
            JsonNode response = restTemplate.getForObject(url, JsonNode.class);

            if (response == null || !response.has("price")) {
                log.error("API returned invalid/empty response for symbol: {}", symbol);
                throw new IllegalStateException("Failed to parse valid price from API response.");
            }

            return new BigDecimal(response.get("price").textValue());

        } catch (HttpClientErrorException e) {
            log.error("Binance API HTTP error while fetching price for {}: {}", symbol, e.getStatusCode());
            throw new BinanceApiException("Binance API call failed. Status: " + e.getStatusCode(), e);
        } catch (Exception e) {
            log.error("Unexpected error fetching from Binance live price for {}: {}", symbol, e.getMessage());
            throw new BinanceApiException("Unexpected error during live price fetch.", e);
        }
    }

    /**
     * Fetches historical candlestick data (Klines).
     * Returns them as mapped BarData objects ready for your cache.
     */
    @Override
    public List<BarData> getHistoricalData(String symbol, String interval, int limit) {
        KlineInterval klineInterval = getKlineInterval(interval);

        try {
            return getHistoricalDataBinance(symbol, klineInterval, limit);
        } catch (HttpClientErrorException e) {
            log.error("Binance API HTTP error for symbol {}: {}", symbol, e.getMessage());
            throw new BinanceApiException("Failed to fetch data from Binance API.", e);
        } catch (Exception e) {
            log.error("Unexpected error during historical data fetch for {}: {}", symbol, e.getMessage());
            throw new BinanceApiException("Unexpected error during Binance data fetch.", e);
        }
    }

    private KlineInterval getKlineInterval(String interval) {
        try {
            return KlineInterval.fromCode(interval);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid kline interval: " + interval +
                                               ". Must be one of: " + Arrays.toString(KlineInterval.values()));
        }
    }

    private List<BarData> getHistoricalDataBinance(String symbol, KlineInterval interval, int limit) {
        String url = String.format("%s/klines?symbol=%s&interval=%s&limit=%d",
                baseUrl, symbol, interval.getCode(), limit);

        List<BarData> bars = new ArrayList<>();

        try {
            List<List<Object>> response = restTemplate.getForObject(url, List.class);

            if (response != null) {
                for (List<Object> rawBar : response) {
                    long timestamp = Long.parseLong(rawBar.get(0).toString());
                    LocalDateTime openTime = LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());

                    bars.add(BarData.builder()
                            .symbol(symbol)
                            .interval(interval.getCode())
                            .openTime(openTime)
                            .openPrice(new BigDecimal(rawBar.get(1).toString()))
                            .highPrice(new BigDecimal(rawBar.get(2).toString()))
                            .lowPrice(new BigDecimal(rawBar.get(3).toString()))
                            .closePrice(new BigDecimal(rawBar.get(4).toString()))
                            .volume(new BigDecimal(rawBar.get(5).toString()))
                            .build());
                }
            }
        } catch (Exception e) {
            log.error("Error fetching history from Binance: {}", e.getMessage());
            throw e;
        }
        return bars;
    }
}