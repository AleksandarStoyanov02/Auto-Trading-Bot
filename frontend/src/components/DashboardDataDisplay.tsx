import React from 'react';

import type { 
    AccountSummaryDto, 
    Trade, 
    Holding, 
    AccountSnapshot, 
    TradingMode,
    BarData
} from '../types/dto'; 

import SummaryCards from './SummaryCards'; 
import PerformanceChart from './PerformanceChart';
import TradeHistoryTable from './TradeHistoryTable';
import MarketChart from './MarketChart';

interface DashboardDataDisplayProps {
    summary: AccountSummaryDto | null;
    history: Trade[];
    holdings: Holding[];
    performance: AccountSnapshot[];
    currentMode: TradingMode | undefined;
    marketData: BarData[]; 
    symbol: string | undefined;
}

const DashboardDataDisplay: React.FC<DashboardDataDisplayProps> = ({ 
    summary, 
    history, 
    holdings, 
    performance,
    currentMode,
    marketData,
    symbol
}) => {    
    if (!summary || !symbol) { 
        return <div className="p-4 text-center">Awaiting data synchronization...</div>;
    }

    return (
        <div className="container mx-auto p-4">
            <h2 className="text-2xl font-bold mb-4 text-indigo-700">
                {currentMode} Simulation Dashboard
            </h2>

            <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
                <SummaryCards summary={summary} holdings={holdings} />
            </div>

            <div className="mb-6">
                <MarketChart chartData={marketData} symbol={symbol} /> 
            </div>

            <div className="bg-white p-6 rounded-lg shadow-xl mb-6">
                <h3 className="text-xl font-semibold mb-3">Portfolio Equity Curve</h3>
                <PerformanceChart performance={performance} />
            </div>

            <div className="bg-white p-6 rounded-lg shadow-xl">
                <h3 className="text-xl font-semibold mb-3">Trade History ({history.length} Trades)</h3>
                <TradeHistoryTable history={history} />
            </div>
        </div>
    );
};

export default DashboardDataDisplay;