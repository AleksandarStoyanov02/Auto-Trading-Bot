package com.trading.autotradingbot.service.impl;

import com.trading.autotradingbot.entity.Account;
import com.trading.autotradingbot.entity.AccountSnapshot;
import com.trading.autotradingbot.entity.PortfolioHolding;
import com.trading.autotradingbot.repository.AccountRepository;
import com.trading.autotradingbot.repository.PortfolioRepository;
import com.trading.autotradingbot.repository.SnapshotRepository;
import com.trading.autotradingbot.service.SnapshotService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class SnapshotServiceImpl implements SnapshotService {

    private final AccountRepository accountRepository;
    private final PortfolioRepository portfolioRepository;
    private final SnapshotRepository snapshotRepository;

    private static final int SCALE = 8;

    public SnapshotServiceImpl(AccountRepository accountRepository, PortfolioRepository portfolioRepository, SnapshotRepository snapshotRepository) {
        this.accountRepository = accountRepository;
        this.portfolioRepository = portfolioRepository;
        this.snapshotRepository = snapshotRepository;
    }

    @Override
    @Transactional
    public void captureSnapshot(Long accountId, BigDecimal currentMarketPrice, LocalDateTime timestamp) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalStateException("Snapshot failed: Account not found."));

        List<PortfolioHolding> holdings = portfolioRepository.findAllByAccountId(accountId);

        BigDecimal cashBalance = account.getCurrentBalance();
        BigDecimal totalCryptoValue = calcCryptoBalance(currentMarketPrice, holdings);

        BigDecimal totalEquity = cashBalance.add(totalCryptoValue);

        accountRepository.updatePortfolioValue(accountId, totalEquity, timestamp);

        AccountSnapshot snapshot = AccountSnapshot.builder()
                .accountId(accountId)
                .timestamp(timestamp)
                .cashBalance(cashBalance)
                .cryptoBalance(totalCryptoValue)
                .totalBalance(totalEquity)
                .build();

        snapshotRepository.save(snapshot);
    }

    private static BigDecimal calcCryptoBalance(BigDecimal currentMarketPrice, List<PortfolioHolding> holdings) {
        BigDecimal totalCryptoValue = BigDecimal.ZERO;

        for (PortfolioHolding holding : holdings) {
            BigDecimal assetValue = holding.getQuantity().multiply(currentMarketPrice)
                    .setScale(SCALE, RoundingMode.HALF_UP);

            totalCryptoValue = totalCryptoValue.add(assetValue);
        }
        return totalCryptoValue;
    }
}