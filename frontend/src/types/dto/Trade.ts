export interface Trade {
    id: number;
    timestamp: string; // LocalDateTime
    symbol: string;
    action: 'BUY' | 'SELL';
    quantity: string;
    price: string;
    fee: string;
    profitLoss: string;
    finalBalance: string;
    strategyName: string;
}