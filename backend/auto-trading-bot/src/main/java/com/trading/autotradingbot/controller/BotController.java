package com.trading.autotradingbot.controller;

import com.trading.autotradingbot.entity.BotConfig;
import com.trading.autotradingbot.dto.BotConfigDto;
import com.trading.autotradingbot.entity.enums.BotStatus;
import com.trading.autotradingbot.entity.enums.TradingMode;
import com.trading.autotradingbot.mapper.BotConfigMapper;
import com.trading.autotradingbot.service.BotManagementService;
import com.trading.autotradingbot.service.impl.LiveTradingServiceImpl;
import com.trading.autotradingbot.service.TrainingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bot")
public class BotController {
    private static final Logger log = LoggerFactory.getLogger(BotController.class);

    private final BotManagementService botManagementService;
    private final TrainingService trainingService;
    private final BotConfigMapper botConfigMapper;
    private final LiveTradingServiceImpl liveTradingService;

    public BotController(BotManagementService botManagementService, TrainingService trainingService,
                         BotConfigMapper botConfigMapper, LiveTradingServiceImpl liveTradingService) {
        this.botManagementService = botManagementService;
        this.trainingService = trainingService;
        this.botConfigMapper = botConfigMapper;
        this.liveTradingService = liveTradingService;
    }

    @GetMapping("/status")
    public ResponseEntity<BotConfigDto> getBotStatus() {
        BotConfig config = botManagementService.getConfig();
        return ResponseEntity.ok(botConfigMapper.toDto(config));
    }

    @PostMapping("/config")
    public ResponseEntity<Void> updateBotConfig(@RequestBody BotConfigDto configDto) {
        botManagementService.changeSymbol(configDto.getSelectedSymbol());
        botManagementService.switchMode(configDto.getTradingMode());
        botManagementService.setStatus(configDto.getStatus());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/start")
    public ResponseEntity<Void> startBot(@RequestParam String interval) {
        BotConfig config = botManagementService.getConfig();

        if (config.getStatus() == BotStatus.RUNNING) {
            log.warn("Attempted to start bot, but status is already RUNNING.");
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        log.info("Starting bot in {} mode for symbol {} with interval {}.",
                config.getTradingMode(), config.getSelectedSymbol(), interval);

        if (config.getTradingMode() == TradingMode.TRADING) {
            liveTradingService.startLiveTrading(config.getSelectedSymbol(), interval);
        } else {
            trainingService.runBacktest(2L, config.getSelectedSymbol(), interval);
        }

        return ResponseEntity.accepted().build();
    }

    @PostMapping("/stop")
    public ResponseEntity<Void> stopBot() {
        botManagementService.setStatus(BotStatus.PAUSED);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset")
    public ResponseEntity<Void> resetBotData() {
        // Reset only Training account data
        trainingService.resetData(2L);
        return ResponseEntity.ok().build();
    }
}