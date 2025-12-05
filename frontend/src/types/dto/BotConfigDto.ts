// frontend/src/types/dto/BotConfigDto.ts

// Union types for enforced string values (mirroring Java Enums)
export type TradingMode = 'TRADING' | 'TRAINING';
export type BotStatus = 'RUNNING' | 'PAUSED' | 'IDLE';

export interface BotConfigDto {
    selectedSymbol: string;
    tradingMode: TradingMode;
    status: BotStatus;
}