import type { BigDecimalString } from '../../common/types';

export interface AccountSummaryDto {
    currentBalance: BigDecimalString; // Current cash balance (USDT)
    currentPortfolioValue: BigDecimalString;
    initialCapital: BigDecimalString;
    totalProfitLoss: BigDecimalString;
}