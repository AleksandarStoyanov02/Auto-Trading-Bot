import React from 'react';
import type { AccountSummaryDto, Holding } from '../types/dto';
import { formatCurrency, formatPnL } from '../utils/formatters';

interface SummaryCardsProps {
    summary: AccountSummaryDto;
    holdings: Holding[];
}

const getCurrentHoldingQuantity = (holdings: Holding[]): { symbol: string; quantity: string } => {
    if (holdings.length > 0) {
        const holding = holdings[0];
        return { 
            symbol: holding.symbol, 
            quantity: parseFloat(holding.quantity).toFixed(5) 
        };
    }
    return { symbol: 'USDT', quantity: '0.00' };
};

const SummaryCards: React.FC<SummaryCardsProps> = ({ summary, holdings }) => {
    const { totalProfitLoss, currentPortfolioValue, currentBalance } = summary;
    const { symbol, quantity } = getCurrentHoldingQuantity(holdings);

    const pnlDisplay = formatPnL(totalProfitLoss);
    
    const isHoldingCrypto = parseFloat(quantity) > 0;

    const dataCards = [
        {
            title: "Total Portfolio Equity",
            value: formatCurrency(currentPortfolioValue),
            unit: 'USD',
            color: 'text-indigo-600',
            description: `Initial Capital: $${formatCurrency(summary.initialCapital)}`,
        },
        {
            title: "Total Realized PnL",
            value: pnlDisplay.text,
            unit: 'USD',
            color: pnlDisplay.color,
            description: 'Net profit/loss since inception (after fees).',
        },
        {
            title: "Available Cash Balance",
            value: formatCurrency(currentBalance),
            unit: 'USDT',
            color: 'text-gray-700',
            description: 'Cash available for new BUY trades.',
        },
        {
            title: `Current Holdings (${symbol})`,
            value: isHoldingCrypto ? quantity : formatCurrency(currentBalance),
            unit: isHoldingCrypto ? symbol : 'USDT',
            color: isHoldingCrypto ? 'bg-yellow-100 text-yellow-800' : 'text-gray-500',
            description: isHoldingCrypto ? `Avg Buy Price: $${formatCurrency(holdings[0].avgBuyPrice)}` : 'No open position (100% cash).',
        },
    ];

    return (
        <>
            {dataCards.map((card, index) => (
                <div key={index} className="bg-white p-5 rounded-xl shadow-lg border-l-4 border-indigo-400">
                    <p className="text-sm font-medium text-gray-500">{card.title}</p>
                    <div className="mt-1 flex items-center justify-between">
                        <p className={`text-3xl font-extrabold ${card.color}`}>
                            {card.value}
                        </p>
                        <span className={`px-2 py-1 text-xs font-semibold rounded-full ${card.color.replace('text-', 'bg-').replace('-600', '-100').replace('text-gray', 'bg-gray')}`}>
                            {card.unit}
                        </span>
                    </div>
                    <p className="mt-2 text-xs text-gray-400 truncate">{card.description}</p>
                </div>
            ))}
        </>
    );
};

export default SummaryCards;