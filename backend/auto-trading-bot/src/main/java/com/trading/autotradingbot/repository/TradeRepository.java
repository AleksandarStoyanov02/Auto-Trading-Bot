package com.trading.autotradingbot.repository;

import com.trading.autotradingbot.entity.Trade;
import com.trading.autotradingbot.entity.enums.TradeAction;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public class TradeRepository {

    private final JdbcTemplate jdbcTemplate;

    public TradeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Trade> tradeMapper = (rs, rowNum) -> Trade.builder()
            .id(rs.getLong("id"))
            .accountId(rs.getLong("account_id"))
            .timestamp(rs.getTimestamp("timestamp").toLocalDateTime())
            .symbol(rs.getString("symbol"))
            .action(TradeAction.valueOf(rs.getString("action")))
            .quantity(rs.getBigDecimal("quantity"))
            .price(rs.getBigDecimal("price"))
            .fee(rs.getBigDecimal("fee"))
            .profitLoss(rs.getBigDecimal("profit_loss"))
            .finalBalance(rs.getBigDecimal("final_balance"))
            .strategyName(rs.getString("strategy_name"))
            .build();

    public void save(Trade trade) {
        String sql = """
                INSERT INTO trade_history
                (account_id, timestamp, symbol, action, quantity, price, fee, profit_loss, final_balance, strategy_name)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        jdbcTemplate.update(sql,
                trade.getAccountId(),
                trade.getTimestamp(),
                trade.getSymbol(),
                trade.getAction().name(),
                trade.getQuantity(),
                trade.getPrice(),
                trade.getFee(),
                trade.getProfitLoss(),
                trade.getFinalBalance(),
                trade.getStrategyName()
        );
    }

    public List<Trade> findAllByAccountId(Long accountId) {
        String sql = "SELECT * FROM trade_history WHERE account_id = ? ORDER BY timestamp DESC";
        return jdbcTemplate.query(sql, tradeMapper, accountId);
    }

    public void deleteAllByAccountId(Long accountId) {
        String sql = "DELETE FROM trade_history WHERE account_id = ?";
        jdbcTemplate.update(sql, accountId);
    }

    public BigDecimal getTotalRealizedProfitLoss(Long accountId) {
        String sql = "SELECT COALESCE(SUM(profit_loss), 0) FROM trade_history WHERE account_id = ?";
        return jdbcTemplate.queryForObject(sql, BigDecimal.class, accountId);
    }
}