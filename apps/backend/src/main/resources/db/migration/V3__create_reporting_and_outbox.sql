CREATE TABLE IF NOT EXISTS outbox_events (
    id BIGSERIAL PRIMARY KEY,
    aggregate_type VARCHAR(64) NOT NULL,
    aggregate_id VARCHAR(128) NOT NULL,
    event_type VARCHAR(64) NOT NULL,
    event_hash VARCHAR(128) NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    retry_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP NULL,
    CONSTRAINT uk_outbox_event_identity UNIQUE (aggregate_type, aggregate_id, event_type, event_hash)
);

CREATE TABLE IF NOT EXISTS reporting_metrics (
    id BIGSERIAL PRIMARY KEY,
    metric_date DATE NOT NULL,
    metric_name VARCHAR(64) NOT NULL,
    metric_value DECIMAL(19,4) NOT NULL DEFAULT 0,
    dimension_value VARCHAR(64) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_reporting_metrics UNIQUE (metric_date, metric_name, dimension_value)
);

CREATE TABLE IF NOT EXISTS reporting_user_activity (
    id BIGSERIAL PRIMARY KEY,
    activity_date DATE NOT NULL,
    user_role VARCHAR(32) NOT NULL,
    active_users INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_reporting_user_activity UNIQUE (activity_date, user_role)
);

CREATE INDEX IF NOT EXISTS idx_outbox_status_created_at ON outbox_events(status, created_at ASC);
CREATE INDEX IF NOT EXISTS idx_reporting_metrics_date ON reporting_metrics(metric_date, metric_name);
