export interface AccountSnapshot {
    id: number;
    timestamp: string;
    totalBalance: string; // Total Equity
    cashBalance: string;
    cryptoBalance: string; // Value of crypto holdings at that time
}