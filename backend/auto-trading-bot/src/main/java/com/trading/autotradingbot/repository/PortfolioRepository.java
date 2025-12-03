package com.trading.autotradingbot.repository;

import com.trading.autotradingbot.entity.PortfolioHolding;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public class PortfolioRepository {

    private final JdbcTemplate jdbcTemplate;

    public PortfolioRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<PortfolioHolding> holdingMapper = (rs, rowNum) -> PortfolioHolding.builder()
            .accountId(rs.getLong("account_id"))
            .symbol(rs.getString("symbol"))
            .quantity(rs.getBigDecimal("quantity"))
            .avgBuyPrice(rs.getBigDecimal("avg_buy_price"))
            .build();

    public Optional<PortfolioHolding> findByIdAndSymbol(Long accountId, String symbol) {
        String sql = "SELECT * FROM portfolio_holding WHERE account_id = ? AND symbol = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, holdingMapper, accountId, symbol));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<PortfolioHolding> findAllByAccountId(Long accountId) {
        String sql = "SELECT * FROM portfolio_holding WHERE account_id = ?";
        return jdbcTemplate.query(sql, holdingMapper, accountId);
    }

    // Upsert (Insert or Update) logic
    public void save(Long accountId, String symbol, BigDecimal quantity, BigDecimal avgBuyPrice) {
        String sql = """
                INSERT INTO portfolio_holding (account_id, symbol, quantity, avg_buy_price)
                VALUES (?, ?, ?, ?)
                ON CONFLICT (account_id, symbol)
                DO UPDATE SET quantity = EXCLUDED.quantity,
                              avg_buy_price = EXCLUDED.avg_buy_price
                """;
        jdbcTemplate.update(sql, accountId, symbol, quantity, avgBuyPrice);
    }

    public void delete(Long accountId, String symbol) {
        String sql = "DELETE FROM portfolio_holding WHERE account_id = ? AND symbol = ?";
        jdbcTemplate.update(sql, accountId, symbol);
    }

    public void deleteAllByAccountId(Long accountId) {
        String sql = "DELETE FROM portfolio_holding WHERE account_id = ?";
        jdbcTemplate.update(sql, accountId);
    }
}