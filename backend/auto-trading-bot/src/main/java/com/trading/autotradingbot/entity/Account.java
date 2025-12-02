package com.trading.autotradingbot.entity;

import com.trading.autotradingbot.entity.enums.AccountType;
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
public class Account {
    private Long id;
    private BigDecimal startBalance;
    private BigDecimal currentBalance;
    private BigDecimal currentPortfolioValue;
    private LocalDateTime creationTimestamp;
    private LocalDateTime lastUpdateTimestamp;
    private AccountType accountType; // Maps to 'LIVE' or 'BACKTEST'
}