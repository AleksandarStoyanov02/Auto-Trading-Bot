package com.trading.autotradingbot.repository;

import com.trading.autotradingbot.entity.BarData;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public class BarDataRepository {

    private final JdbcTemplate jdbcTemplate;

    public BarDataRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<BarData> barDataMapper = (rs, rowNum) -> BarData.builder()
            .id(rs.getLong("id"))
            .symbol(rs.getString("symbol"))
            .openTime(rs.getTimestamp("open_time").toLocalDateTime())
            .openPrice(rs.getBigDecimal("open_price"))
            .highPrice(rs.getBigDecimal("high_price"))
            .lowPrice(rs.getBigDecimal("low_price"))
            .closePrice(rs.getBigDecimal("close_price"))
            .volume(rs.getBigDecimal("volume"))
            .interval(rs.getString("interval"))
            .build();

    /**
     * Retrieves cached historical data for a symbol and interval.
     */
    public List<BarData> findAllBySymbolAndInterval(String symbol, String interval) {
        String sql = "SELECT * FROM bar_data_cache WHERE symbol = ? AND \"interval\" = ? ORDER BY open_time ASC";
        return jdbcTemplate.query(sql, barDataMapper, symbol, interval);
    }

    /**
     * Checks if the cache for a given symbol/interval is empty.
     */
    public boolean isCacheEmpty(String symbol, String interval) {
        String sql = "SELECT COUNT(*) FROM bar_data_cache WHERE symbol = ? AND \"interval\" = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, symbol, interval);
        return count == null || count == 0;
    }

    /**
     * Batch inserts historical data into the cache.
     */
    public void saveAll(List<BarData> bars) {
        String sql = """
                INSERT INTO bar_data_cache (symbol, open_time, open_price, high_price, low_price, close_price, volume, "interval")
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (symbol, open_time, "interval") DO NOTHING;
                """;

        jdbcTemplate.batchUpdate(sql, bars, bars.size(), (ps, bar) -> {
            ps.setString(1, bar.getSymbol());
            ps.setTimestamp(2, Timestamp.valueOf(bar.getOpenTime()));
            ps.setBigDecimal(3, bar.getOpenPrice());
            ps.setBigDecimal(4, bar.getHighPrice());
            ps.setBigDecimal(5, bar.getLowPrice());
            ps.setBigDecimal(6, bar.getClosePrice());
            ps.setBigDecimal(7, bar.getVolume());
            ps.setString(8, bar.getInterval());
        });
    }
}