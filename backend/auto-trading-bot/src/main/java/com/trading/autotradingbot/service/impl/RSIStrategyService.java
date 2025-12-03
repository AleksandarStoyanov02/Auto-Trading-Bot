package com.trading.autotradingbot.service.impl;

import com.trading.autotradingbot.entity.BarData;
import com.trading.autotradingbot.entity.enums.Signal;
import com.trading.autotradingbot.mapper.BarConverter;
import com.trading.autotradingbot.service.TradingStrategyService;
import org.springframework.stereotype.Service;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class RSIStrategyService implements TradingStrategyService {
    private static final int MAX_BAR_COUNT = 500;
    private static final int RSI_PERIOD = 14;
    private static final int MIN_BARS_FOR_ANALYSIS = RSI_PERIOD + 1;
    private static final int RSI_OVERSOLD_THRESHOLD = 30;
    private static final int RSI_OVERBOUGHT_THRESHOLD = 70;

    private String symbol;
    private String intervalCode;

    // Package private for testing
    BarSeries series;

    @Override
    public void initializeSeries(List<BarData> historicalBars) {
        if (!historicalBars.isEmpty()) {
            BarData firstBar = historicalBars.getFirst();
            this.symbol = firstBar.getSymbol();
            this.intervalCode = firstBar.getInterval();
        } else {
            throw new IllegalStateException("Cannot initialize strategy series: Historical data is empty.");
        }

        this.series = new BaseBarSeriesBuilder().withName(symbol).build();
        this.series.setMaximumBarCount(MAX_BAR_COUNT);

        for (BarData curr : historicalBars) {
            if (!Objects.equals(curr.getSymbol(), symbol)) {
                throw new IllegalArgumentException("Added bars are of different symbols");
            }

            if (!Objects.equals(curr.getInterval(), intervalCode)) {
                throw new IllegalArgumentException("Added bars are of different time intervals");
            }

            addBarData(curr);
        }
    }

    private void createNewBar(Bar lastBar, BigDecimal newPrice, ZonedDateTime timestamp) {
        Num openPrice = lastBar.getClosePrice();

        BarData newBarData = BarData.builder()
                .symbol(symbol)
                .interval(intervalCode)
                .openTime(timestamp.toLocalDateTime())
                .openPrice(openPrice.bigDecimalValue())
                .highPrice(newPrice)
                .lowPrice(newPrice)
                .closePrice(newPrice)
                .volume(BigDecimal.ZERO)
                .build();

        BaseBar newBar = BarConverter.toTa4jBar(newBarData, series.numFactory());
        series.addBar(newBar);
    }

    @Override
    public Signal getSignal(BigDecimal newPrice, ZonedDateTime timestamp) {
        if (series == null || series.getBarCount() < MIN_BARS_FOR_ANALYSIS) {
            throw new IllegalStateException("Trading analysis cannot run: BarSeries is empty or needs more data (min "
                                            + MIN_BARS_FOR_ANALYSIS + " bars for RSI " + RSI_PERIOD + ").");
        }

        Bar lastBar = series.getLastBar();
        Num priceNum = series.numFactory().numOf(newPrice);

        if (timestamp.toInstant().isAfter(lastBar.getEndTime())) {
            createNewBar(lastBar, newPrice, timestamp);
        } else {
            series.addPrice(priceNum);
        }

        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        RSIIndicator rsi = new RSIIndicator(closePrice, RSI_PERIOD);

        int endIndex = series.getEndIndex();
        Num rsiValue = rsi.getValue(endIndex);

        // Check if RSI drops below 30 (BUY) or rises above 70 (SELL)
        if (rsiValue.isLessThan(series.numFactory().numOf(RSI_OVERSOLD_THRESHOLD))) {
            return Signal.BUY;
        } else if (rsiValue.isGreaterThan(series.numFactory().numOf(RSI_OVERBOUGHT_THRESHOLD))) {
            return Signal.SELL;
        } else {
            return Signal.HOLD;
        }
    }

    @Override
    public String getStrategyName() {
        return "RSI_Simple_30_70";
    }

    @Override
    public int getMinBarsForAnalysis() {
        return MIN_BARS_FOR_ANALYSIS;
    }

    private void addBarData(BarData barData) {
        BaseBar bar = BarConverter.toTa4jBar(barData, series.numFactory());
        series.addBar(bar);
    }
}