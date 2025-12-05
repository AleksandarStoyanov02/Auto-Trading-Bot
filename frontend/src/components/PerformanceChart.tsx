import React from 'react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer, ReferenceLine } from 'recharts';
import type { AccountSnapshot } from '../types/dto';

interface PerformanceChartProps {
    performance: AccountSnapshot[];
}


const prepareChartData = (snapshots: AccountSnapshot[]) => {
    if (!snapshots || snapshots.length === 0) return [];

    const initialCapital = parseFloat(snapshots[0].totalBalance); 

    return snapshots.map(snapshot => ({
        time: new Date(snapshot.timestamp).toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' }),
        
        equity: parseFloat(snapshot.totalBalance),
        cash: parseFloat(snapshot.cashBalance),
        
        gain: parseFloat(snapshot.totalBalance) - initialCapital,
        
        baseline: initialCapital 
    }));
};

const PerformanceChart: React.FC<PerformanceChartProps> = ({ performance }) => {
    const chartData = prepareChartData(performance);

    if (chartData.length === 0) {
        return <div className="p-10 text-center text-gray-500">Run a backtest first to generate historical data.</div>;
    }
    
    const finalGain = chartData[chartData.length - 1].gain;
    const lineColor = finalGain >= 0 ? '#10B981' : '#EF4444';

    return (
        <ResponsiveContainer width="100%" height={400}>
            <LineChart
                data={chartData}
                margin={{ top: 5, right: 30, left: 20, bottom: 5 }}
            >
                <CartesianGrid strokeDasharray="3 3" stroke="#e0e0e0" />
                
                <XAxis 
                    dataKey="time" 
                    hide={true} 
                    padding={{ left: 0, right: 0 }} 
                />
                
                <YAxis 
                       type="number"
                       domain={['auto', 'auto']} 
                       tickCount={6} 
                       tickFormatter={(value) => `$${Number(value).toFixed(0)}`} 
                />
                
                <Tooltip 
                    formatter={(value, name) => [`$${Number(value).toFixed(2)}`, name === 'equity' ? 'Total Equity' : 'Baseline']} 
                    labelFormatter={(label) => `Time: ${label}`}
                />
                
                <Legend />

                <ReferenceLine y={chartData[0].baseline} stroke="#999" strokeDasharray="5 5" label="Start" />

                <Line 
                    type="monotone" 
                    dataKey="equity" 
                    stroke={lineColor} 
                    strokeWidth={2} 
                    dot={false}
                    name="Portfolio Value"
                />
            </LineChart>
        </ResponsiveContainer>
    );
};

export default PerformanceChart;