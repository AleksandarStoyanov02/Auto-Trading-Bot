package com.trading.autotradingbot.service;

public interface TrainingService {
    void resetData(Long accountId);
    void runBacktest(Long accountId, String symbol, String interval);
}