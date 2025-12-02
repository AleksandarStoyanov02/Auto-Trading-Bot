package com.trading.autotradingbot.repository;

import com.trading.autotradingbot.entity.BotConfig;
import com.trading.autotradingbot.entity.enums.BotStatus;
import com.trading.autotradingbot.entity.enums.TradingMode;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class BotConfigRepository {

    private final JdbcTemplate jdbcTemplate;

    public BotConfigRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<BotConfig> configMapper = (rs, rowNum) -> BotConfig.builder()
            .id(rs.getInt("id"))
            .tradingMode(TradingMode.valueOf(rs.getString("trading_mode")))
            .status(BotStatus.valueOf(rs.getString("status")))
            .selectedSymbol(rs.getString("selected_symbol"))
            .isInitialized(rs.getBoolean("is_initialized"))
            .build();

    public BotConfig getConfig() {
        String sql = "SELECT * FROM bot_config WHERE id = 1";
        return jdbcTemplate.queryForObject(sql, configMapper);
    }

    /**
     * Updates only the selected trading symbol.
     * Used by BotManagementServiceImpl.changeSymbol().
     */
    public void updateSymbol(String symbol) {
        String sql = "UPDATE bot_config SET selected_symbol = ? WHERE id = 1";
        jdbcTemplate.update(sql, symbol);
    }

    /**
     * Updates only the trading mode (TRAINING or TRADING).
     * Used by BotManagementServiceImpl.switchMode().
     */
    public void updateMode(TradingMode mode) {
        String sql = "UPDATE bot_config SET trading_mode = ? WHERE id = 1";
        jdbcTemplate.update(sql, mode.name());
    }

    /**
     * Updates only the bot status (RUNNING, PAUSED, IDLE).
     * Used by BotManagementServiceImpl.setStatus().
     */
    public void updateStatus(BotStatus status) {
        String sql = "UPDATE bot_config SET status = ? WHERE id = 1";
        jdbcTemplate.update(sql, status.name());
    }
}