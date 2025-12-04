package com.trading.autotradingbot.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class AccountSummaryDto {
    private BigDecimal currentBalance;
    private BigDecimal currentPortfolioValue;
    private BigDecimal initialCapital;
    private BigDecimal totalProfitLoss;
}