package com.trading.autotradingbot.service;

import com.trading.autotradingbot.entity.BarData;
import java.math.BigDecimal;
import java.util.List;

public interface MarketDataProvider {
    BigDecimal getLivePrice(String symbol);
    List<BarData> getHistoricalData(String symbol, String interval, int limit);
}