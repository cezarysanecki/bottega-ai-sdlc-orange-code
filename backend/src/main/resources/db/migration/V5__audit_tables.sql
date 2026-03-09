CREATE TABLE IF NOT EXISTS audit_events (
    id SERIAL PRIMARY KEY,
    entity_name VARCHAR(128),
    entity_id VARCHAR(64),
    action VARCHAR(32),
    payload JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE TABLE IF NOT EXISTS api_calls (
    id SERIAL PRIMARY KEY,
    path VARCHAR(255),
    method VARCHAR(16),
    username VARCHAR(64),
    status INTEGER,
    payload TEXT,
    occurred_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);
