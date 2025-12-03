package com.trading.autotradingbot.service;

import java.math.BigDecimal;

public interface OrderExecutionHandler {
    void executeBuy(Long accountId, String symbol, BigDecimal price, String strategyName);
    void executeSell(Long accountId, String symbol, BigDecimal price, String strategyName);
}