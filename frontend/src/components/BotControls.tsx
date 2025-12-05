import React, { useState } from 'react';
import type { BotConfigDto, TradingMode } from '../types/dto/index';
import { startBot, stopBot, resetBotData, updateBotConfig } from '../api/botApi';

interface BotControlsProps {
    currentConfig: BotConfigDto;
    currentInterval: string;
    onIntervalChange: (newInterval: string) => void;
}

const BotControls: React.FC<BotControlsProps> = ({ currentConfig, currentInterval, onIntervalChange }) => {
    const [selectedSymbol, setSelectedSymbol] = useState(currentConfig.selectedSymbol);
    const [selectedMode, setSelectedMode] = useState<TradingMode>(currentConfig.tradingMode);

    const isRunning = currentConfig.status === 'RUNNING';
    const isTraining = currentConfig.tradingMode === 'TRAINING';

    const handleConfigUpdate = async () => {
        if (isRunning) {
            alert('Cannot change configuration while Bot is running. Please pause the bot first.');
            return;
        }

        try {
            const newConfig: BotConfigDto = {
                selectedSymbol: selectedSymbol,
                tradingMode: selectedMode,
                status: currentConfig.status,
            };
            
            await updateBotConfig(newConfig);
            alert(`Configuration updated successfully! New Mode: ${newConfig.tradingMode}`);
            
        } catch (error) {
            console.error('Config Update Failed:', error);
            alert('Failed to update configuration.');
        }
    };

    const handleStartStop = async () => {
        try {
            if (isRunning) {
                await stopBot();
                alert('Bot stopped.');
            } else {
                await startBot(currentInterval); 
                alert(`Bot starting in ${currentConfig.tradingMode} mode...`);
            }
        } catch (error) {
            console.error('Control Action Failed:', error);
            alert('Failed to execute command. Check console.');
        }
    };

    const handleReset = async () => {
        if (confirm('Are you sure you want to reset all backtest data?')) {
            try {
                await resetBotData();
                alert('Backtest data reset successfully!');
            } catch (error) {
                console.error('Reset Failed:', error);
                alert('Failed to reset data.');
            }
        }
    };

    return (
        <div className="flex flex-col lg:flex-row items-start lg:items-center justify-between p-4 mb-6 bg-white rounded-lg shadow space-y-4 lg:space-y-0">
            
            {/* Configuration Panel */}
            <div className="flex flex-col md:flex-row items-center space-y-2 md:space-y-0 md:space-x-4">
                
                {/* Mode Selector (Unchanged) */}
                <div className="flex flex-col">
                    <label className="text-xs font-semibold text-gray-500 mb-1">Mode</label>
                    <select
                        value={selectedMode}
                        onChange={(e) => setSelectedMode(e.target.value as TradingMode)}
                        disabled={isRunning}
                        className="p-2 border rounded-lg bg-gray-50 disabled:bg-gray-200"
                    >
                        <option value="TRADING">LIVE TRADING</option>
                        <option value="TRAINING">BACKTEST (Training)</option>
                    </select>
                </div>

                {/* Symbol Selector (Unchanged) */}
                <div className="flex flex-col">
                    <label className="text-xs font-semibold text-gray-500 mb-1">Asset</label>
                    <select
                        value={selectedSymbol}
                        onChange={(e) => setSelectedSymbol(e.target.value)}
                        disabled={isRunning}
                        className="p-2 border rounded-lg bg-gray-50 disabled:bg-gray-200"
                    >
                        <option value="BTCUSDT">BTCUSDT</option>
                        <option value="ETHUSDT">ETHUSDT</option>
                    </select>
                </div>

                {/* Chart Interval Selector (Using destructured props) */}
                <div className="flex flex-col">
                    <label className="text-xs font-semibold text-gray-500 mb-1">Chart Interval</label>
                    <select
                        value={currentInterval} // Prop from App.tsx
                        onChange={(e) => onIntervalChange(e.target.value)} // Setter from App.tsx
                        disabled={isRunning}
                        className="p-2 border rounded-lg bg-gray-50 disabled:bg-gray-200"
                    >
                        <option value="1m">1 Minute</option>
                        <option value="5m">5 Minutes</option>
                        <option value="1h">1 Hour (Default)</option>
                        <option value="4h">4 Hours</option>
                        <option value="1d">1 Day</option>
                    </select>
                </div>
                
                {/* Apply Button (Unchanged) */}
                <button
                    onClick={handleConfigUpdate}
                    disabled={isRunning}
                    className="mt-4 md:mt-auto px-4 py-2 bg-indigo-500 text-white rounded-lg hover:bg-indigo-600 disabled:bg-indigo-300"
                >
                    Apply Config
                </button>
            </div>
            
            {/* Control Buttons (Start/Stop/Reset) */}
            <div className="flex space-x-3">
                <button
                    onClick={handleStartStop}
                    className={`px-4 py-2 text-white font-bold rounded-lg transition-colors 
                                ${isRunning ? 'bg-red-600 hover:bg-red-700' : 'bg-green-600 hover:bg-green-700'}`}
                >
                    {isRunning ? '⏸️ PAUSE BOT' : '▶️ START BOT'}
                </button>
                
                {/* Reset button logic (Unchanged) */}
                {isTraining && (
                    <button
                        onClick={handleReset}
                        disabled={isRunning}
                        className={`px-4 py-2 text-white rounded-lg transition-colors 
                                    ${isRunning ? 'bg-yellow-300' : 'bg-yellow-600 hover:bg-yellow-700'}`}
                    >
                        🔄 RESET DATA
                    </button>
                )}
            </div>
        </div>
    );
};

export default BotControls;