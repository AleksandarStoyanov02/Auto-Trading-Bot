package com.trading.autotradingbot.service;

import com.trading.autotradingbot.entity.BotConfig;
import com.trading.autotradingbot.entity.enums.BotStatus;
import com.trading.autotradingbot.entity.enums.TradingMode;

public interface BotManagementService {
    BotConfig getConfig();
    void switchMode(TradingMode mode);
    void changeSymbol(String symbol);
    void setStatus(BotStatus status);
}