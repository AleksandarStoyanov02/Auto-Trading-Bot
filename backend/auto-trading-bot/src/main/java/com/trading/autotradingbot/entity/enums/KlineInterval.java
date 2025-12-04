package com.trading.autotradingbot.entity.enums;

// Enforces supported intervals at compile-time
public enum KlineInterval {
    ONE_MINUTE("1m"),
    THREE_MINUTES("3m"),
    FIVE_MINUTES("5m"),
    FIFTEEN_MINUTES("15m"),
    THIRTY_MINUTES("30m"),

    ONE_HOUR("1h"),
    TWO_HOURS("2h"),
    FOUR_HOURS("4h"),
    SIX_HOURS("6h"),
    TWELVE_HOURS("12h"),

    ONE_DAY("1d"),
    THREE_DAYS("3d"),

    ONE_WEEK("1w"),
    ONE_MONTH("1M");

    private final String code;

    KlineInterval(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static KlineInterval fromCode(String code) {
        for (KlineInterval interval : values()) {
            if (interval.code.equalsIgnoreCase(code)) {
                return interval;
            }
        }

        throw new IllegalArgumentException(
                "Invalid kline interval code: " + code +
                        ". Must be one of the codes defined in the enum."
        );
    }
}