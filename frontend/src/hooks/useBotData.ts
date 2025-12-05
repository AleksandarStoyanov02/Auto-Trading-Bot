// frontend/src/hooks/useBotData.ts

import { useState, useEffect } from 'react';
import { fetchBotStatus } from '../api/botApi';
import {  fetchAccountSummary, fetchTradeHistory, fetchCurrentHoldings, fetchAccountPerformance, fetchMarketChartData } from '../api/dataApi';
import type { BotConfigDto, AccountSummaryDto, Trade, Holding, AccountSnapshot, BarData } from '../types/dto/index';

const POLL_INTERVAL = 2000; 

export const useBotData = (interval: string) => {
    const [config, setConfig] = useState<BotConfigDto | null>(null);
    const [summary, setSummary] = useState<AccountSummaryDto | null>(null);
    const [history, setHistory] = useState<Trade[]>([]);
    const [holdings, setHoldings] = useState<Holding[]>([]);
    const [marketData, setMarketData] = useState<BarData[]>([]);
    
    const [performance, setPerformance] = useState<AccountSnapshot[]>([]); 
    
    const [loading, setLoading] = useState(true);

    const fetchData = async () => {
        let fetchedConfig: BotConfigDto | null = null;

        try {
            fetchedConfig = await fetchBotStatus();
            setConfig(fetchedConfig);

        } catch (error) {
            console.error("Critical: Failed to fetch configuration/status.", error);
            if (loading) setLoading(false);
            return;
        }

        try {
            const [summaryData, historyData, holdingsData, performanceData, marketData] = await Promise.all([
                fetchAccountSummary(), 
                fetchTradeHistory(), 
                fetchCurrentHoldings(),
                fetchAccountPerformance(),
                fetchMarketChartData(interval)
            ]);

            // 3. Set all secondary state, now that we know config is safe
            setSummary(summaryData);
            setHistory(historyData);
            setHoldings(holdingsData);
            setPerformance(performanceData);
            setMarketData(marketData);

        } catch (error) {
            console.warn("Partial Load Warning: Some dashboard data failed to load.", error);
            
        } finally {
            if (loading) setLoading(false);
        }
    };

    useEffect(() => {
        fetchData();
        const intervalId = setInterval(fetchData, POLL_INTERVAL);
        return () => clearInterval(intervalId);
    }, [interval]); 
    
    return { config, summary, history, holdings, performance, loading, marketData };
};