CREATE TABLE outbox_events (
                               id BIGSERIAL PRIMARY KEY,
                               aggregate_type VARCHAR(255) NOT NULL,
                               aggregate_id VARCHAR(255) NOT NULL,
                               event_type VARCHAR(255) NOT NULL,
                               payload TEXT NOT NULL,
                               processed BOOLEAN NOT NULL DEFAULT FALSE,
                               created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               processed_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_outbox_unprocessed ON outbox_events (processed, created_at);