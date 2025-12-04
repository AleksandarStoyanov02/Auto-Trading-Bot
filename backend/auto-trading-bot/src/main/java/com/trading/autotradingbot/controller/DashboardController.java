package com.trading.autotradingbot.controller;

import com.trading.autotradingbot.dto.AccountSummaryDto;
import com.trading.autotradingbot.entity.*;
import com.trading.autotradingbot.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class DashboardController {
    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/account/summary")
    public ResponseEntity<AccountSummaryDto> getAccountSummary() {
        AccountSummaryDto summary = dashboardService.getAccountSummary();
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/account/performance")
    public ResponseEntity<List<AccountSnapshot>> getAccountPerformance() {
        List<AccountSnapshot> snapshots = dashboardService.getAccountPerformance();
        return ResponseEntity.ok(snapshots);
    }

    @GetMapping("/trade/history")
    public ResponseEntity<List<Trade>> getTradeHistory() {
        List<Trade> trades = dashboardService.getTradeHistory();
        return ResponseEntity.ok(trades);
    }

    @GetMapping("/trade/holdings")
    public ResponseEntity<List<PortfolioHolding>> getCurrentHoldings() {
        List<PortfolioHolding> holdings = dashboardService.getCurrentHoldings();
        return ResponseEntity.ok(holdings);
    }

    @GetMapping("/market/chart")
    public ResponseEntity<List<BarData>> getMarketChartData(@RequestParam String interval) {
        List<BarData> chartData = dashboardService.getMarketChartData(interval);
        return ResponseEntity.ok(chartData);
    }
}