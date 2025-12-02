-- 1. ACCOUNT TABLE
CREATE TABLE account (
                         id BIGSERIAL PRIMARY KEY,
                         start_balance NUMERIC(20, 8) NOT NULL,
                         current_balance NUMERIC(20, 8) NOT NULL,
                         current_portfolio_value NUMERIC(20, 8) NOT NULL,
                         creation_timestamp TIMESTAMP WITHOUT TIME ZONE NOT NULL,
                         last_update_timestamp TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

ALTER TABLE account ADD COLUMN account_type VARCHAR(20) NOT NULL CHECK (account_type IN ('LIVE', 'BACKTEST'));

-- Constraint: Ensure you only ever have ONE 'LIVE' and ONE 'BACKTEST' account
CREATE UNIQUE INDEX idx_account_type ON account(account_type);

-- 2. PORTFOLIO
CREATE TABLE portfolio_holding (
                                   account_id BIGINT NOT NULL REFERENCES account(id),
                                   symbol VARCHAR(10) NOT NULL,
                                   quantity NUMERIC(20, 8) NOT NULL DEFAULT 0,
                                   PRIMARY KEY (account_id, symbol),
                                   CONSTRAINT positive_quantity CHECK (quantity >= 0)
);

-- 3. TRADE HISTORY TABLE
CREATE TABLE trade_history (
                               id BIGSERIAL PRIMARY KEY,
                               account_id BIGINT NOT NULL REFERENCES account(id),
                               timestamp TIMESTAMP WITHOUT TIME ZONE NOT NULL,
                               symbol VARCHAR(10) NOT NULL,
                               action VARCHAR(4) NOT NULL CHECK (action IN ('BUY', 'SELL')),
							   quantity NUMERIC(20, 8) NOT NULL,
                               price NUMERIC(20, 8) NOT NULL,
                               fee NUMERIC(10, 8) NOT NULL DEFAULT 0,
                               profit_loss NUMERIC(20, 8) NOT NULL DEFAULT 0,
                               final_balance NUMERIC(20, 8) NOT NULL,
							   strategy_name VARCHAR(40) NOT NULL
);

-- 4. BAR DATA CACHE (For backtesting history)
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

-- 5. ACCOUNT HISTORY SNAPSHOTS (For the Portfolio Value Chart)
CREATE TABLE account_snapshot (
                                  id BIGSERIAL PRIMARY KEY,
                                  account_id BIGINT NOT NULL REFERENCES account(id),
                                  timestamp TIMESTAMP WITHOUT TIME ZONE NOT NULL,
                                  total_balance NUMERIC(20, 8) NOT NULL, -- Cash + Crypto Value
                                  cash_balance NUMERIC(20, 8) NOT NULL,
                                  crypto_balance NUMERIC(20, 8) NOT NULL
);

-- 6. BOT CONFIGURATION (Persist state across restarts)
CREATE TABLE bot_config (
                            id SERIAL PRIMARY KEY,
                            trading_mode VARCHAR(20) NOT NULL CHECK (trading_mode IN ('TRAINING', 'TRADING')),
                            status VARCHAR(20) NOT NULL CHECK (status IN ('RUNNING', 'PAUSED', 'IDLE')),
                            selected_symbol VARCHAR(10) NOT NULL DEFAULT 'BTCUSDT',
                            is_initialized BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_account_snapshot_time ON account_snapshot (account_id, timestamp);
CREATE UNIQUE INDEX idx_bar_data_cache_symbol_time ON bar_data_cache (symbol, open_time, "interval");

-- 1. LIVE Account (Persistent)
INSERT INTO account (id, start_balance, current_balance, current_portfolio_value, creation_timestamp, last_update_timestamp, account_type)
VALUES (1, 10000.00, 10000.00, 10000.00, NOW(), NOW(), 'LIVE');

-- 2. BACKTEST Account (Reusable Scratchpad)
INSERT INTO account (id, start_balance, current_balance, current_portfolio_value, creation_timestamp, last_update_timestamp, account_type)
VALUES (2, 10000.00, 10000.00, 10000.00, NOW(), NOW(), 'BACKTEST');

-- Default Bot Config
INSERT INTO bot_config (trading_mode, status, selected_symbol, is_initialized)
VALUES ('TRADING', 'IDLE', 'BTCUSDT', FALSE);