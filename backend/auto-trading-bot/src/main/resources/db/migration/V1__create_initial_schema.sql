-- Ensure all financial fields use NUMERIC for exact precision.
-- The scale (8) is used to support high-precision crypto values.

-- 1. ACCOUNT TABLE
CREATE TABLE account (
                         id BIGSERIAL PRIMARY KEY,
                         start_balance NUMERIC(20, 8) NOT NULL,
                         current_balance NUMERIC(20, 8) NOT NULL,
                         current_portfolio_value NUMERIC(20, 8) NOT NULL,
                         creation_timestamp TIMESTAMP WITHOUT TIME ZONE NOT NULL,
                         last_update_timestamp TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

-- 2. TRADE HISTORY TABLE
CREATE TABLE trade_history (
                               id BIGSERIAL PRIMARY KEY,
                               account_id INTEGER NOT NULL REFERENCES account(id),
                               timestamp TIMESTAMP WITHOUT TIME ZONE NOT NULL,
                               symbol VARCHAR(10) NOT NULL,
                               action VARCHAR(4) NOT NULL, -- 'BUY' or 'SELL'
                               quantity NUMERIC(20, 8) NOT NULL,
                               price NUMERIC(20, 8) NOT NULL,
                               fee NUMERIC(10, 8) NOT NULL DEFAULT 0,
                               profit_loss NUMERIC(20, 8) NOT NULL DEFAULT 0,
                               final_balance NUMERIC(20, 8) NOT NULL
);

-- 3. BAR DATA CACHE (For backtesting history)
CREATE TABLE bar_data_cache (
                                id BIGSERIAL PRIMARY KEY,
                                symbol VARCHAR(10) NOT NULL,
                                open_time TIMESTAMP WITHOUT TIME ZONE NOT NULL,
                                open_price NUMERIC(20, 8) NOT NULL,
                                high_price NUMERIC(20, 8) NOT NULL,
                                low_price NUMERIC(20, 8) NOT NULL,
                                close_price NUMERIC(20, 8) NOT NULL,
                                volume NUMERIC(20, 8) NOT NULL,
                                "interval" VARCHAR(5) NOT NULL
);

-- Index for fast retrieval of historical data during backtesting
CREATE UNIQUE INDEX idx_bar_data_cache_symbol_time ON bar_data_cache (symbol, open_time, "interval");

-- Initial setup: Insert the starting balance
INSERT INTO account (start_balance, current_balance, current_portfolio_value, creation_timestamp, last_update_timestamp)
VALUES (10000.00, 10000.00, 10000.00, NOW(), NOW());