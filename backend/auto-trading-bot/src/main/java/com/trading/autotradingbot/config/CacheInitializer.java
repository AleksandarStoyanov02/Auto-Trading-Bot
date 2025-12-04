package com.trading.autotradingbot.config;

import com.trading.autotradingbot.repository.BarDataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class CacheInitializer {

    private static final Logger log = LoggerFactory.getLogger(CacheInitializer.class);

    private final BarDataRepository barDataRepository;

    public CacheInitializer(BarDataRepository barDataRepository) {
        this.barDataRepository = barDataRepository;
    }

    /**
     * Executes immediately after the bean is constructed.
     * Wipes the historical data cache clean.
     */
    @PostConstruct
    public void clearCacheOnStartup() {
        log.warn("Clearing all historical data from bar_data_cache...");

        try {
            barDataRepository.deleteAll();
            log.warn("Cache cleared successfully.");
        } catch (Exception e) {
            log.error("Failed to clear cache table. Database connection likely failed.", e);
        }
    }
}