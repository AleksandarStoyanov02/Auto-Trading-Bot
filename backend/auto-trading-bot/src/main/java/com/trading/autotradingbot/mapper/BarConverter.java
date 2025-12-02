package com.trading.autotradingbot.mapper;

import com.trading.autotradingbot.entity.BarData;
import org.ta4j.core.BaseBar; // Use the actual BaseBar class
import org.ta4j.core.num.NumFactory;
import org.ta4j.core.num.Num;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.Instant;

public class BarConverter {
    private static final Duration DEFAULT_TIME_PERIOD = Duration.ofHours(1);

    /**
     * Adapts BarData POJO into a TA4J BaseBar object using the full constructor.
     */
    public static BaseBar toTa4jBar(BarData barData, NumFactory factory) {

        ZonedDateTime zdt = ZonedDateTime.of(barData.getOpenTime(), ZoneId.systemDefault());
        Instant beginTime = zdt.toInstant();
        Instant endTime = zdt.plus(DEFAULT_TIME_PERIOD).toInstant();

        Num open = factory.numOf(barData.getOpenPrice());
        Num high = factory.numOf(barData.getHighPrice());
        Num low = factory.numOf(barData.getLowPrice());
        Num close = factory.numOf(barData.getClosePrice());
        Num volume = factory.numOf(barData.getVolume());

        return new BaseBar(
                DEFAULT_TIME_PERIOD,      // 1. Duration timePeriod
                beginTime,                // 2. Instant beginTime
                endTime,                  // 3. Instant endTime
                open,                     // 4. Num openPrice
                high,                     // 5. Num highPrice
                low,                      // 6. Num lowPrice
                close,                    // 7. Num closePrice
                volume,                   // 8. Num volume
                factory.numOf(0), // 9. Num amount (Set to zero for initial load)
                0L                        // 10. long trades (Set to zero for initial load)
        );
    }
}