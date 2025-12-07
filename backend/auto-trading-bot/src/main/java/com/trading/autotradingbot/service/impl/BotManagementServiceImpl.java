package com.trading.autotradingbot.service.impl;

import com.trading.autotradingbot.entity.BotConfig;
import com.trading.autotradingbot.entity.enums.BotStatus;
import com.trading.autotradingbot.entity.enums.TradingMode;
import com.trading.autotradingbot.repository.BotConfigRepository;
import com.trading.autotradingbot.service.BotManagementService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BotManagementServiceImpl implements BotManagementService {

    private final BotConfigRepository botConfigRepository;

    public BotManagementServiceImpl(BotConfigRepository botConfigRepository) {
        this.botConfigRepository = botConfigRepository;
    }

    private void checkNotRunning() {
        if (botConfigRepository.getConfig().getStatus() == BotStatus.RUNNING) {
            throw new IllegalStateException("Cannot change configuration while Bot is RUNNING. Stop it first.");
        }
    }

    @Override
    public BotConfig getConfig() {
        return botConfigRepository.getConfig();
    }

    @Override
    @Transactional
    public void switchMode(TradingMode mode) {
        checkNotRunning();
        botConfigRepository.updateMode(mode);
    }

    @Override
    @Transactional
    public void changeSymbol(String symbol) {
        checkNotRunning();
        botConfigRepository.updateSymbol(symbol);
    }

    @Override
    @Transactional
    public void setStatus(BotStatus status) {
        botConfigRepository.updateStatus(status);
    }
}