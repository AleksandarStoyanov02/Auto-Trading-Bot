package com.trading.autotradingbot.service.impl;

import com.trading.autotradingbot.entity.Account;
import com.trading.autotradingbot.entity.PortfolioHolding;
import com.trading.autotradingbot.entity.Trade;
import com.trading.autotradingbot.entity.enums.TradeAction;
import com.trading.autotradingbot.repository.AccountRepository;
import com.trading.autotradingbot.repository.PortfolioRepository;
import com.trading.autotradingbot.repository.TradeRepository;
import com.trading.autotradingbot.service.OrderExecutionHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AllInOrderExecutionService implements OrderExecutionHandler {

    private final AccountRepository accountRepository;
    private final PortfolioRepository portfolioRepository;
    private final TradeRepository tradeRepository;

    private static final BigDecimal FEE_RATE = new BigDecimal("0.001"); // 0.1% flat fee for now
    private static final BigDecimal BUY_ALLOCATION_FACTOR = new BigDecimal("0.999"); // 99.9% allocation for fee buffer
    private static final int SCALE = 8; // Financial precision scale

    public AllInOrderExecutionService(
            AccountRepository accountRepository,
            PortfolioRepository portfolioRepository,
            TradeRepository tradeRepository) {
        this.accountRepository = accountRepository;
        this.portfolioRepository = portfolioRepository;
        this.tradeRepository = tradeRepository;
    }

    /**
     * Executes a simulated market BUY order.
     * Strategy: All-in (99.9% of available cash).
     */
    @Override
    @Transactional
    public void executeBuy(Long accountId, String symbol, BigDecimal price, String strategyName) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalStateException("Account not found."));

        BigDecimal cashAvailable = account.getCurrentBalance();

        BigDecimal amountToSpend = cashAvailable.multiply(BUY_ALLOCATION_FACTOR).setScale(SCALE, RoundingMode.DOWN);

        if (amountToSpend.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Insufficient funds: Cash available is less than the minimum spendable amount.");
        }

        BigDecimal quantity = amountToSpend.divide(price, SCALE, RoundingMode.DOWN);
        BigDecimal fee = amountToSpend.multiply(FEE_RATE).setScale(SCALE, RoundingMode.HALF_UP);
        BigDecimal totalSpent = amountToSpend.add(fee);

        BigDecimal newCashBalance = cashAvailable.subtract(totalSpent);
        BigDecimal newPortfolioValue = account.getCurrentPortfolioValue().add(amountToSpend);

        Optional<PortfolioHolding> existingHoldingOpt = portfolioRepository.findByIdAndSymbol(accountId, symbol);

        BigDecimal finalQuantity;
        BigDecimal finalAvgPrice;

        if (existingHoldingOpt.isPresent()) {
            PortfolioHolding existing = existingHoldingOpt.get();
            BigDecimal oldQty = existing.getQuantity();
            BigDecimal oldAvgPrice = existing.getAvgBuyPrice();

            BigDecimal totalCostOld = oldQty.multiply(oldAvgPrice);
            BigDecimal totalCostNew = quantity.multiply(price);

            finalQuantity = oldQty.add(quantity);

            finalAvgPrice = totalCostOld.add(totalCostNew)
                    .divide(finalQuantity, SCALE, RoundingMode.HALF_UP);

        } else {
            finalQuantity = quantity;
            finalAvgPrice = price.setScale(SCALE, RoundingMode.HALF_UP);
        }

        accountRepository.updateBalance(accountId, newCashBalance, newPortfolioValue);
        portfolioRepository.save(accountId, symbol, finalQuantity, finalAvgPrice);

        Trade trade = Trade.builder()
                .accountId(accountId)
                .timestamp(LocalDateTime.now())
                .symbol(symbol)
                .action(TradeAction.BUY)
                .quantity(quantity)
                .price(price)
                .fee(fee)
                .profitLoss(BigDecimal.ZERO)
                .finalBalance(newCashBalance)
                .strategyName(strategyName)
                .build();
        tradeRepository.save(trade);
    }

    /**
     * Executes a simulated market SELL order.
     * Strategy: Sells 100% of the current position.
     */
    @Override
    @Transactional
    public void executeSell(Long accountId, String symbol, BigDecimal price, String strategyName) {
        PortfolioHolding holding = portfolioRepository.findByIdAndSymbol(accountId, symbol)
                .orElseThrow(() -> new IllegalStateException("Cannot SELL: No holdings found for " + symbol));

        BigDecimal quantityToSell = holding.getQuantity();

        if (quantityToSell.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Cannot SELL zero quantity.");
        }

        BigDecimal avgBuyPrice = holding.getAvgBuyPrice();

        BigDecimal totalRevenue = price.multiply(quantityToSell).setScale(SCALE, RoundingMode.HALF_UP);
        BigDecimal fee = totalRevenue.multiply(FEE_RATE).setScale(SCALE, RoundingMode.HALF_UP);

        // PnL = (Revenue - Cost Basis) - Fee
        BigDecimal costBasis = avgBuyPrice.multiply(quantityToSell).setScale(SCALE, RoundingMode.HALF_UP);
        BigDecimal profitLoss = totalRevenue.subtract(costBasis).subtract(fee).setScale(SCALE, RoundingMode.HALF_UP);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalStateException("Account not found."));

        BigDecimal newCashBalance = account.getCurrentBalance().add(totalRevenue).subtract(fee);

        accountRepository.updateBalance(accountId, newCashBalance, newCashBalance);
        portfolioRepository.delete(accountId, symbol);

        Trade trade = Trade.builder()
                .accountId(accountId)
                .timestamp(LocalDateTime.now())
                .symbol(symbol)
                .action(TradeAction.SELL)
                .quantity(quantityToSell)
                .price(price)
                .fee(fee)
                .profitLoss(profitLoss)
                .finalBalance(newCashBalance)
                .strategyName(strategyName)
                .build();
        tradeRepository.save(trade);
    }
}