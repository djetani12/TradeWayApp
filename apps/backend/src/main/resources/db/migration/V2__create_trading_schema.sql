CREATE TABLE IF NOT EXISTS trade_orders (
    id BIGSERIAL PRIMARY KEY,
    customer_account_id BIGINT NOT NULL,
    instrument_id BIGINT NOT NULL,
    side VARCHAR(16) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    quantity INTEGER NOT NULL,
    filled_quantity INTEGER NOT NULL DEFAULT 0,
    remaining_quantity INTEGER NOT NULL,
    limit_price DECIMAL(19,4),
    average_fill_price DECIMAL(19,4),
    client_order_id VARCHAR(128) UNIQUE,
    reason VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_trade_orders_account FOREIGN KEY (customer_account_id) REFERENCES customer_accounts (id) ON DELETE CASCADE,
    CONSTRAINT fk_trade_orders_instrument FOREIGN KEY (instrument_id) REFERENCES instruments (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS trade_positions (
    id BIGSERIAL PRIMARY KEY,
    customer_account_id BIGINT NOT NULL,
    instrument_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 0,
    average_cost DECIMAL(19,4) NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_trade_positions_account FOREIGN KEY (customer_account_id) REFERENCES customer_accounts (id) ON DELETE CASCADE,
    CONSTRAINT fk_trade_positions_instrument FOREIGN KEY (instrument_id) REFERENCES instruments (id) ON DELETE CASCADE,
    CONSTRAINT uk_trade_positions_account_instrument UNIQUE (customer_account_id, instrument_id)
);

CREATE TABLE IF NOT EXISTS trade_fills (
    id BIGSERIAL PRIMARY KEY,
    trade_order_id BIGINT NOT NULL,
    customer_account_id BIGINT NOT NULL,
    instrument_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL,
    execution_price DECIMAL(19,4) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_trade_fills_order FOREIGN KEY (trade_order_id) REFERENCES trade_orders (id) ON DELETE CASCADE,
    CONSTRAINT fk_trade_fills_account FOREIGN KEY (customer_account_id) REFERENCES customer_accounts (id) ON DELETE CASCADE,
    CONSTRAINT fk_trade_fills_instrument FOREIGN KEY (instrument_id) REFERENCES instruments (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS execution_audit (
    id BIGSERIAL PRIMARY KEY,
    customer_account_id BIGINT,
    event_type VARCHAR(64) NOT NULL,
    reference_id VARCHAR(128),
    details VARCHAR(500),
    occurred_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_execution_audit_account FOREIGN KEY (customer_account_id) REFERENCES customer_accounts (id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_trade_orders_account ON trade_orders(customer_account_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_trade_orders_status ON trade_orders(status, created_at ASC);
CREATE INDEX IF NOT EXISTS idx_trade_positions_account ON trade_positions(customer_account_id, instrument_id);
CREATE INDEX IF NOT EXISTS idx_trade_fills_account ON trade_fills(customer_account_id, created_at DESC);
