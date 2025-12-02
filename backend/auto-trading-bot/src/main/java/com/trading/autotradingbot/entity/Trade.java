package com.trading.autotradingbot.entity;

import com.trading.autotradingbot.entity.enums.TradeAction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Trade {
    private Long id;
    private Long accountId;
    private LocalDateTime timestamp;
    private String symbol;
    private TradeAction action; // Maps to 'BUY' or 'SELL'
    private BigDecimal quantity;
    private BigDecimal price;
    private BigDecimal fee;
    private BigDecimal profitLoss;
    private BigDecimal finalBalance;
    private String strategyName; // e.g. "RSI_Crossover"
}