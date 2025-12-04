package com.trading.autotradingbot.dto;

import com.trading.autotradingbot.entity.enums.TradingMode;
import com.trading.autotradingbot.entity.enums.BotStatus;
import lombok.Data;

@Data
public class BotConfigDto {
    private String selectedSymbol;
    private TradingMode tradingMode;
    private BotStatus status;
}