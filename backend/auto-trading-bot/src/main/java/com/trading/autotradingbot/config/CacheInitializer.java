package com.trading.autotradingbot.config;

import com.trading.autotradingbot.repository.BarDataRepository;
import com.trading.autotradingbot.service.AccountResetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

import static com.trading.autotradingbot.common.AccountConstants.DEFAULT_CAPITAL;
import static com.trading.autotradingbot.common.AccountConstants.LIVE_ACCOUNT_ID;

@Component
public class CacheInitializer {
    private static final Logger log = LoggerFactory.getLogger(CacheInitializer.class);

    private final BarDataRepository barDataRepository;
    private final AccountResetService accountResetService;

    public CacheInitializer(BarDataRepository barDataRepository, AccountResetService accountResetService) {
        this.barDataRepository = barDataRepository;
        this.accountResetService = accountResetService;
    }

    /**
     * Executes immediately after the bean is constructed.
     * Wipes the historical data cache clean.
     */
    @PostConstruct
    public void clearCacheOnStartup() {
        log.warn("Cache Initializer active");

        try {
            barDataRepository.deleteAll();
            log.warn("Cache cleared successfully.");

            accountResetService.resetAllAccountData(LIVE_ACCOUNT_ID, DEFAULT_CAPITAL);
            log.warn("LIVE Account (ID {}) reset to starting capital: ${}", LIVE_ACCOUNT_ID, DEFAULT_CAPITAL);
        } catch (Exception e) {
            log.error("Failed to clear cache table. Database connection likely failed.", e);
        }
    }
}