package com.trading.autotradingbot.service;

import java.math.BigDecimal;

public interface AccountResetService {

    /**
     * Wipes all trades, holdings, and snapshots for the given account,
     * and resets the balance to the starting capital.
     */
    void resetAllAccountData(Long accountId, BigDecimal startingCapital);
}