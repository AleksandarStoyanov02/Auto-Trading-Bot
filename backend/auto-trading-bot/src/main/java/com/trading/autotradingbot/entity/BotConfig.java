package com.trading.autotradingbot.entity;

import com.trading.autotradingbot.entity.enums.BotStatus;
import com.trading.autotradingbot.entity.enums.TradingMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BotConfig {
    private Integer id;
    private TradingMode tradingMode; // TRAINING or TRADING
    private BotStatus status;        // RUNNING, PAUSED, IDLE
    private String selectedSymbol;
    private Boolean isInitialized;
}