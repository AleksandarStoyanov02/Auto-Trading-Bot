package com.trading.autotradingbot.entity;

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
public class AccountSnapshot {
    private Long id;
    private Long accountId;
    private LocalDateTime timestamp;
    private BigDecimal totalBalance;
    private BigDecimal cashBalance;
    private BigDecimal cryptoBalance;
}