package com.trading.autotradingbot.repository;

import com.trading.autotradingbot.entity.Account;
import com.trading.autotradingbot.entity.enums.AccountType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public class AccountRepository {

    private final JdbcTemplate jdbcTemplate;

    public AccountRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // MAPPER: Converts SQL ResultSet row -> Account Object
    private final RowMapper<Account> accountRowMapper = (rs, rowNum) -> Account.builder()
            .id(rs.getLong("id"))
            .startBalance(rs.getBigDecimal("start_balance"))
            .currentBalance(rs.getBigDecimal("current_balance"))
            .currentPortfolioValue(rs.getBigDecimal("current_portfolio_value"))
            .creationTimestamp(rs.getTimestamp("creation_timestamp").toLocalDateTime())
            .lastUpdateTimestamp(rs.getTimestamp("last_update_timestamp").toLocalDateTime())
            .accountType(AccountType.valueOf(rs.getString("account_type")))
            .build();

    public Optional<Account> findById(Long id) {
        String sql = "SELECT * FROM account WHERE id = ?";
        return jdbcTemplate.query(sql, accountRowMapper, id).stream().findFirst();
    }

    public void updatePortfolioValue(Long id, BigDecimal newPortfolioValue, LocalDateTime timestamp) {
        String sql = """
                UPDATE account
                SET current_portfolio_value = ?,
                    last_update_timestamp = ?
                WHERE id = ?
                """;
        jdbcTemplate.update(sql, newPortfolioValue, timestamp, id);
    }


    public void updateBalance(Long id, BigDecimal newBalance, BigDecimal newPortfolioValue) {
        String sql = """
                UPDATE account
                SET current_balance = ?,
                    current_portfolio_value = ?,
                    last_update_timestamp = ?
                WHERE id = ?
                """;
        jdbcTemplate.update(sql, newBalance, newPortfolioValue, LocalDateTime.now(), id);
    }

    public void resetAccount(Long id, BigDecimal startAmount) {
        String sql = """
                UPDATE account
                SET current_balance = ?,
                    current_portfolio_value = ?,
                    start_balance = ?,
                    last_update_timestamp = ?
                WHERE id = ?
                """;
        jdbcTemplate.update(sql, startAmount, startAmount, startAmount, LocalDateTime.now(), id);
    }

    public BigDecimal getAccountBalance(Long id) {
        String sql = "SELECT current_balance FROM account WHERE id = ?";

        return jdbcTemplate.queryForObject(sql, BigDecimal.class, id);
    }
}