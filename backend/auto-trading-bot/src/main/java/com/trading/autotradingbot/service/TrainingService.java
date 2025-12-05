package com.trading.autotradingbot.service;

public interface TrainingService {
    void runBacktest(Long accountId, String symbol, String interval);
}