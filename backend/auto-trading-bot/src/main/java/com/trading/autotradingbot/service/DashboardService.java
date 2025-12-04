package com.trading.autotradingbot.service;

import com.trading.autotradingbot.dto.AccountSummaryDto;
import com.trading.autotradingbot.entity.AccountSnapshot;
import com.trading.autotradingbot.entity.BarData;
import com.trading.autotradingbot.entity.PortfolioHolding;
import com.trading.autotradingbot.entity.Trade;

import java.util.List;

public interface DashboardService {
    AccountSummaryDto getAccountSummary();
    List<AccountSnapshot> getAccountPerformance();
    List<Trade> getTradeHistory();
    List<PortfolioHolding> getCurrentHoldings();
    List<BarData> getMarketChartData(String interval);
}