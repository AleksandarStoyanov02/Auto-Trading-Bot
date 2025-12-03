package com.trading.autotradingbot.service;

import com.trading.autotradingbot.entity.BarData;
import com.trading.autotradingbot.entity.enums.Signal;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

public interface TradingStrategyService {

    void initializeSeries(List<BarData> historicalBars);

    /** * Adds the latest price data point and checks if a trade signal is generated.
     * @param newPrice The current market price.
     * @param timestamp The time of the observation.
     * @return BUY, SELL, or HOLD.
     */
    Signal getSignal(BigDecimal newPrice, ZonedDateTime timestamp);

    /** Returns the unique identifier for this strategy (e.g., "RSI_Simple_30_70"). */
    String getStrategyName();

    int getMinBarsForAnalysis();
}