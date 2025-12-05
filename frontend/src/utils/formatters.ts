export type BigDecimalString = string;

export const formatTimestamp = (isoString: string): string => {
    // Example: Convert "2024-01-01T12:00:00" to "Jan 1, 12:00 PM"
    return new Date(isoString).toLocaleString();
};

export const formatCurrency = (valueString: string | number): string => {
    const value = parseFloat(valueString.toString());
    return value.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
};

export const formatPnL = (valueString: string): { text: string; color: string } => {
    const value = parseFloat(valueString);
    const text = value.toFixed(2);
    let color = 'text-gray-900';
    if (value > 0) {
        color = 'text-green-600 font-semibold';
    } else if (value < 0) {
        color = 'text-red-600 font-semibold';
    }
    return { text, color };
};
