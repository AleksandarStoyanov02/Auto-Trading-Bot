import React from 'react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import type { BarData } from '../types/dto';

interface MarketChartProps {
    chartData: BarData[];
    symbol: string;
}


const prepareMarketChartData = (barData: BarData[]) => {
    if (!barData || barData.length === 0) return [];

    return barData.map(bar => ({
        // FIX 1: Return the full date/time string (ISO format) for context
        time: bar.openTime, 
        // Convert string prices to numbers for charting
        price: parseFloat(bar.closePrice),
    }));
};

const MarketChart: React.FC<MarketChartProps> = ({ chartData, symbol }) => {
    const data = prepareMarketChartData(chartData);

    if (data.length === 0) {
        return <div className="p-8 text-center text-gray-500">No market data cached. Run a backtest or check API connection.</div>;
    }

    return (
        <div className="bg-white p-6 rounded-lg shadow-xl">
            <h3 className="text-xl font-semibold mb-3">Market Price Context ({symbol})</h3>
            <ResponsiveContainer width="100%" height={300}>
                <LineChart
                    data={data}
                    margin={{ top: 10, right: 30, left: 20, bottom: 5 }}
                >
                    <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
                    
                    <XAxis 
                        dataKey="time"
                        tickFormatter={(isoTime) => new Date(isoTime).toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' })}
                    />
                    
                    <YAxis 
                        domain={['auto', 'auto']}
                        tickCount={6}
                        tickFormatter={(value) => `$${Number(value).toFixed(0)}`}
                    />

                    <Tooltip 
                        formatter={(value) => [`$${Number(value).toFixed(2)}`, 'Close Price']} 
                        labelFormatter={(label) => new Date(label).toLocaleString()}
                    />
                    
                    <Line 
                        type="monotone" 
                        dataKey="price" 
                        stroke="#4F46E5" 
                        strokeWidth={2} 
                        dot={false}
                        name="Price"
                    />
                </LineChart>
            </ResponsiveContainer>
        </div>
    );
};

export default MarketChart;