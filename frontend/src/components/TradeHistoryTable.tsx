import React from 'react';
import type { Trade } from '../types/dto';
import { formatTimestamp, formatCurrency, formatPnL } from '../utils/formatters';

interface TradeHistoryTableProps {
    history: Trade[];
}

const TradeHistoryTable: React.FC<TradeHistoryTableProps> = ({ history }) => {

    if (!history || history.length === 0) {
        return <div className="p-4 text-gray-500">No trades recorded for this session.</div>;
    }

    const sortedHistory = [...history].sort((a, b) => new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime());

    return (
        <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
                <thead>
                    <tr className="bg-gray-50 text-gray-500 uppercase text-xs">
                        <th className="px-3 py-2 text-left">Date</th>
                        <th className="px-3 py-2 text-left">Symbol</th>
                        <th className="px-3 py-2 text-center">Action</th>
                        <th className="px-3 py-2 text-right">Quantity</th>
                        <th className="px-3 py-2 text-right">Price</th>
                        <th className="px-3 py-2 text-right">Fee</th>
                        <th className="px-3 py-2 text-right">Realized PnL</th>
                        <th className="px-3 py-2 text-right">Final Balance</th>
                    </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-100 text-sm">
                    {sortedHistory.map((trade) => {
                        const actionColor = trade.action === 'BUY' ? 'text-green-600 font-bold' : 'text-red-600 font-bold';
                        
                        return (
                            <tr key={trade.id} className="hover:bg-gray-50">
                                <td className="px-3 py-2 whitespace-nowrap">{formatTimestamp(trade.timestamp)}</td>
                                <td className="px-3 py-2 whitespace-nowrap">{trade.symbol}</td>
                                <td className={`px-3 py-2 text-center ${actionColor}`}>{trade.action}</td>
                                <td className="px-3 py-2 text-right">{parseFloat(trade.quantity).toFixed(5)}</td>
                                <td className="px-3 py-2 text-right">${formatCurrency(trade.price)}</td>
                                <td className="px-3 py-2 text-right">{formatCurrency(trade.fee)}</td>
                                
                                <td className={`px-3 py-2 text-right ${formatPnL(trade.profitLoss).color}`}>
                                    {formatPnL(trade.profitLoss).text}
                                </td>
                                
                                <td className="px-3 py-2 text-right">${formatCurrency(trade.finalBalance)}</td>
                            </tr>
                        );
                    })}
                </tbody>
            </table>
        </div>
    );
};

export default TradeHistoryTable;