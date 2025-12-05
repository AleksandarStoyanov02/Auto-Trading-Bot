package com.trading.autotradingbot.common;

import java.math.BigDecimal;

public final class AccountConstants {
    private AccountConstants() {
        throw new AssertionError("Constants class should not be instantiated.");
    }

    // Constants used in multiple Account and Bot Services
    public static final Long LIVE_ACCOUNT_ID = 1L;
    public static final Long BACKTEST_ACCOUNT_ID = 2L;
    public static final BigDecimal DEFAULT_CAPITAL = new BigDecimal("10000.00");
    public static final BigDecimal STOP_LOSS_THRESHOLD = new BigDecimal("0.98");
    public static final int SCALE = 8;
}