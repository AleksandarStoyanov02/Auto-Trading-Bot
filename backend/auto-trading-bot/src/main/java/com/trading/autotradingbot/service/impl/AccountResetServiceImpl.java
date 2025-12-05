package com.trading.autotradingbot.service.impl;

import com.trading.autotradingbot.repository.AccountRepository;
import com.trading.autotradingbot.repository.PortfolioRepository;
import com.trading.autotradingbot.repository.SnapshotRepository;
import com.trading.autotradingbot.repository.TradeRepository;
import com.trading.autotradingbot.service.AccountResetService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class AccountResetServiceImpl implements AccountResetService {

    private final AccountRepository accountRepository;
    private final TradeRepository tradeRepository;
    private final PortfolioRepository portfolioRepository;
    private final SnapshotRepository snapshotRepository;

    public AccountResetServiceImpl(AccountRepository accountRepository, TradeRepository tradeRepository, PortfolioRepository portfolioRepository, SnapshotRepository snapshotRepository) {
        this.accountRepository = accountRepository;
        this.tradeRepository = tradeRepository;
        this.portfolioRepository = portfolioRepository;
        this.snapshotRepository = snapshotRepository;
    }

    @Override
    @Transactional
    public void resetAllAccountData(Long accountId, BigDecimal startingCapital) {
        tradeRepository.deleteAllByAccountId(accountId);
        portfolioRepository.deleteAllByAccountId(accountId);
        snapshotRepository.deleteAllByAccountId(accountId);

        accountRepository.resetAccount(accountId, startingCapital);
    }
}