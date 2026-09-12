CREATE TABLE fraud_alerts (
    alert_id UUID PRIMARY KEY,
    decision_id UUID NOT NULL,
    transaction_id UUID NOT NULL,
    tenant_id VARCHAR(128) NOT NULL,
    priority VARCHAR(32) NOT NULL,
    risk_band VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    title VARCHAR(256) NOT NULL,
    summary TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    event_type VARCHAR(128),
    event_id UUID,
    correlation_id VARCHAR(128),
    causation_id VARCHAR(128),
    trace_id VARCHAR(128),
    attributes_text TEXT NOT NULL DEFAULT '',
    CONSTRAINT fraud_alerts_priority_check CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'URGENT')),
    CONSTRAINT fraud_alerts_risk_band_check CHECK (risk_band IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    CONSTRAINT fraud_alerts_status_check CHECK (status IN ('OPEN'))
);

CREATE TABLE fraud_cases (
    case_id UUID PRIMARY KEY,
    alert_id UUID NOT NULL REFERENCES fraud_alerts (alert_id),
    transaction_id UUID NOT NULL,
    decision_id UUID NOT NULL,
    tenant_id VARCHAR(128) NOT NULL,
    priority VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    title VARCHAR(256) NOT NULL,
    summary TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    event_type VARCHAR(128),
    event_id UUID,
    correlation_id VARCHAR(128),
    causation_id VARCHAR(128),
    trace_id VARCHAR(128),
    attributes_text TEXT NOT NULL DEFAULT '',
    CONSTRAINT fraud_cases_priority_check CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'URGENT')),
    CONSTRAINT fraud_cases_status_check CHECK (status IN ('OPEN'))
);

CREATE INDEX idx_fraud_alerts_tenant_created_at
    ON fraud_alerts (tenant_id, created_at DESC);

CREATE INDEX idx_fraud_alerts_transaction_id
    ON fraud_alerts (transaction_id);

CREATE INDEX idx_fraud_alerts_decision_id
    ON fraud_alerts (decision_id);

CREATE INDEX idx_fraud_alerts_event_id
    ON fraud_alerts (event_id);

CREATE INDEX idx_fraud_alerts_correlation_id
    ON fraud_alerts (correlation_id);

CREATE INDEX idx_fraud_cases_tenant_created_at
    ON fraud_cases (tenant_id, created_at DESC);

CREATE INDEX idx_fraud_cases_transaction_id
    ON fraud_cases (transaction_id);

CREATE INDEX idx_fraud_cases_decision_id
    ON fraud_cases (decision_id);

CREATE INDEX idx_fraud_cases_alert_id
    ON fraud_cases (alert_id);

CREATE INDEX idx_fraud_cases_event_id
    ON fraud_cases (event_id);

CREATE INDEX idx_fraud_cases_correlation_id
    ON fraud_cases (correlation_id);
