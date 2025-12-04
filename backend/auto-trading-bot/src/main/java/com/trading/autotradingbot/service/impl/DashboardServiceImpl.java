package com.trading.autotradingbot.service.impl;

import com.trading.autotradingbot.entity.*;
import com.trading.autotradingbot.dto.AccountSummaryDto;
import com.trading.autotradingbot.entity.enums.TradingMode;
import com.trading.autotradingbot.repository.*;
import com.trading.autotradingbot.service.BotManagementService;
import com.trading.autotradingbot.service.DashboardService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final AccountRepository accountRepository;
    private final TradeRepository tradeRepository;
    private final PortfolioRepository portfolioRepository;
    private final SnapshotRepository snapshotRepository;
    private final BarDataRepository barDataRepository;
    private final BotManagementService botManagementService;

    // Constructor Injection (All Repositories and Services)
    public DashboardServiceImpl(AccountRepository accountRepository, TradeRepository tradeRepository,
                                PortfolioRepository portfolioRepository, SnapshotRepository snapshotRepository,
                                BarDataRepository barDataRepository, BotManagementService botManagementService) {
        this.accountRepository = accountRepository;
        this.tradeRepository = tradeRepository;
        this.portfolioRepository = portfolioRepository;
        this.snapshotRepository = snapshotRepository;
        this.barDataRepository = barDataRepository;
        this.botManagementService = botManagementService;
    }

    @Override
    public AccountSummaryDto getAccountSummary() {
        Long accountId = getActiveAccountId();
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found."));

        // 1. Calculate PnL (Returns ZERO if no trades exist)
        BigDecimal totalPnL = tradeRepository.getTotalRealizedProfitLoss(accountId);

        AccountSummaryDto summary = new AccountSummaryDto();
        summary.setInitialCapital(account.getStartBalance());
        summary.setCurrentBalance(account.getCurrentBalance());
        summary.setCurrentPortfolioValue(account.getCurrentPortfolioValue());

        summary.setTotalProfitLoss(totalPnL != null ? totalPnL : BigDecimal.ZERO);

        return summary;
    }

    private Long getActiveAccountId() {
        TradingMode mode = botManagementService.getConfig().getTradingMode();
        // LIVE is ID 1, BACKTEST is ID 2
        return (mode == TradingMode.TRADING) ? 1L : 2L;
    }


    @Override
    public List<AccountSnapshot> getAccountPerformance() {
        return snapshotRepository.findAllByAccountId(getActiveAccountId());
    }

    @Override
    public List<Trade> getTradeHistory() {
        return tradeRepository.findAllByAccountId(getActiveAccountId());
    }

    @Override
    public List<PortfolioHolding> getCurrentHoldings() {
        return portfolioRepository.findAllByAccountId(getActiveAccountId());
    }

    @Override
    public List<BarData> getMarketChartData(String interval) {
        BotConfig config = botManagementService.getConfig();
        return barDataRepository.findAllBySymbolAndInterval(
                config.getSelectedSymbol(), interval
        );
    }
}