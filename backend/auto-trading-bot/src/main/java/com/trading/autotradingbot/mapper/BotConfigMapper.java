package com.trading.autotradingbot.mapper;

import com.trading.autotradingbot.entity.BotConfig;
import com.trading.autotradingbot.dto.BotConfigDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BotConfigMapper {
    BotConfigDto toDto(BotConfig entity);
    BotConfig toEntity(BotConfigDto dto);
}