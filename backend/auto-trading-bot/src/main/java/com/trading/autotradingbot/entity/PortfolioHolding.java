package com.trading.autotradingbot.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioHolding {
    private Long accountId;
    private String symbol;
    private BigDecimal quantity;
    private BigDecimal avgBuyPrice;
}
