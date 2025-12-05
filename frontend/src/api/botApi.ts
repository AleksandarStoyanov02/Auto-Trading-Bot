import type { BotConfigDto } from '../types/dto/BotConfigDto';

const BASE_URL = '/api/bot';

export const updateBotConfig = async (config: BotConfigDto): Promise<void> => {
    // POST /api/bot/config Body: JSON object with Configuration (symbol, mode, status)
    const response = await fetch(`${BASE_URL}/config`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(config), 
    });
    
    if (!response.ok) {
        throw new Error(`Failed to update configuration. Status: ${response.status}`);
    }
};

export const fetchBotStatus = async (): Promise<BotConfigDto> => {
    // GET /api/bot/status
    const response = await fetch(`${BASE_URL}/status`);
    if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
    }
    return response.json();
};

export const startBot = async (interval: string): Promise<void> => {
    // POST /api/bot/start?interval={1m,5m,1h, etc.}
    const response = await fetch(`${BASE_URL}/start?interval=${interval}`, {
        method: 'POST',
    });
    if (!response.ok) {
        throw new Error(`Failed to start bot. Status: ${response.status}`);
    }
};

export const stopBot = async (): Promise<void> => {
    // POST /api/bot/stop
    const response = await fetch(`${BASE_URL}/stop`, {
        method: 'POST',
    });
    if (!response.ok) {
        throw new Error(`Failed to stop bot. Status: ${response.status}`);
    }
};

export const resetBotData = async (): Promise<void> => {
    // POST /api/bot/reset
    const response = await fetch(`${BASE_URL}/reset`, {
        method: 'POST',
    });
    if (!response.ok) {
        throw new Error(`Failed to reset data. Status: ${response.status}`);
    }
};