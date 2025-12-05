import type { AccountSummaryDto, Trade, Holding, AccountSnapshot, BarData } from "../types/dto/index";

const BASE_URL = '/api';

const handleResponse = async (response: Response) => {
    if (!response.ok) {
        const errorBody = await response.json().catch(() => ({}));
        
        throw new Error(
            errorBody.message || 
            `HTTP Error ${response.status}: Failed to fetch data.`
        );
    }
    return response.json();
};

export const fetchAccountSummary = async (): Promise<AccountSummaryDto> => {
    // GET /api/account/summary
    const response = await fetch(`${BASE_URL}/account/summary`);
    return handleResponse(response);
};

export const fetchTradeHistory = async (): Promise<Trade[]> => {
    // GET /api/trade/history
    const response = await fetch(`${BASE_URL}/trade/history`);
    return handleResponse(response);
};

export const fetchCurrentHoldings = async (): Promise<Holding[]> => {
    // GET /api/trade/holdings
    const response = await fetch(`${BASE_URL}/trade/holdings`);
    return handleResponse(response);
};

export const fetchAccountPerformance = async (): Promise<AccountSnapshot[]> => {
    // GET /api/account/performance
    const response = await fetch(`${BASE_URL}/account/performance`);
    return handleResponse(response);
};

export const fetchMarketChartData = async (interval: string): Promise<BarData[]> => {
    // GET /api/market/chart?interval={1m, 5m, 1h, etc.}
    const response = await fetch(`${BASE_URL}/market/chart?interval=${interval}`);
    return handleResponse(response);
};