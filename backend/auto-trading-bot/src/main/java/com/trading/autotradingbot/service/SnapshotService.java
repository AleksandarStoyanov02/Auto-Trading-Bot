package com.trading.autotradingbot.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface SnapshotService {

    /** * Captures and persists the current state of the account equity.
     * @param accountId The ID of the active account
     * @param currentMarketPrice The price of the current symbol for valuation.
     * @param timestamp The time the bar closed (for the snapshot record).
     */
    void captureSnapshot(Long accountId, BigDecimal currentMarketPrice, LocalDateTime timestamp);
}