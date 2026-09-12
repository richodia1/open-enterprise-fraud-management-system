CREATE TABLE audit_events (
    audit_event_id UUID PRIMARY KEY,
    tenant_id VARCHAR(128) NOT NULL,
    actor_id VARCHAR(128) NOT NULL,
    action VARCHAR(64) NOT NULL,
    resource_type VARCHAR(128) NOT NULL,
    resource_id VARCHAR(128) NOT NULL,
    summary TEXT NOT NULL,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    event_type VARCHAR(128),
    event_id UUID,
    correlation_id VARCHAR(128),
    causation_id VARCHAR(128),
    trace_id VARCHAR(128),
    source_service VARCHAR(128),
    event_occurred_at TIMESTAMP WITH TIME ZONE,
    attributes_text TEXT NOT NULL DEFAULT '',
    CONSTRAINT audit_events_action_check CHECK (
        action IN (
            'ALERT_CREATED',
            'CASE_CREATED',
            'CREATED',
            'DECISION_PRODUCED',
            'DELETED',
            'EXPLANATION_PREPARED',
            'LOGIN',
            'LOGOUT',
            'STATUS_CHANGED',
            'UPDATED',
            'VIEWED'
        )
    )
);

CREATE INDEX idx_audit_events_tenant_occurred_at
    ON audit_events (tenant_id, occurred_at DESC);

CREATE INDEX idx_audit_events_resource_occurred_at
    ON audit_events (tenant_id, resource_type, resource_id, occurred_at DESC);

CREATE INDEX idx_audit_events_correlation_id
    ON audit_events (correlation_id);

CREATE INDEX idx_audit_events_event_id
    ON audit_events (event_id);

CREATE INDEX idx_audit_events_trace_id
    ON audit_events (trace_id);
