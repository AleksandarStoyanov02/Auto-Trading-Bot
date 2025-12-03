package com.trading.autotradingbot.repository;

import com.trading.autotradingbot.entity.AccountSnapshot;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public class SnapshotRepository {

    private final JdbcTemplate jdbcTemplate;

    public SnapshotRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<AccountSnapshot> snapshotRowMapper = (rs, rowNum) -> AccountSnapshot.builder()
            .id(rs.getLong("id"))
            .accountId(rs.getLong("account_id"))
            .timestamp(rs.getTimestamp("timestamp").toLocalDateTime())
            .totalBalance(rs.getBigDecimal("total_balance"))
            .cashBalance(rs.getBigDecimal("cash_balance"))
            .cryptoBalance(rs.getBigDecimal("crypto_balance"))
            .build();

    /**
     * Saves a snapshot of the account's equity and balance at a specific time.
     * Used by Live/Training services after major state changes.
     */
    public void save(AccountSnapshot snapshot) {
        String sql = """
                INSERT INTO account_snapshot (account_id, timestamp, total_balance, cash_balance, crypto_balance)
                VALUES (?, ?, ?, ?, ?)
                """;

        jdbcTemplate.update(sql,
                snapshot.getAccountId(),
                Timestamp.valueOf(snapshot.getTimestamp()),
                snapshot.getTotalBalance(),
                snapshot.getCashBalance(),
                snapshot.getCryptoBalance());
    }

    /**
     * Retrieves all snapshots for a given account for charting purposes.
     */
    public List<AccountSnapshot> findAllByAccountId(Long accountId) {
        String sql = "SELECT * FROM account_snapshot WHERE account_id = ? ORDER BY timestamp ASC";
        return jdbcTemplate.query(sql, snapshotRowMapper, accountId);
    }

    /**
     * Clears all snapshot history for a given account (used before backtesting).
     */
    public void deleteAllByAccountId(Long accountId) {
        String sql = "DELETE FROM account_snapshot WHERE account_id = ?";
        jdbcTemplate.update(sql, accountId);
    }
}