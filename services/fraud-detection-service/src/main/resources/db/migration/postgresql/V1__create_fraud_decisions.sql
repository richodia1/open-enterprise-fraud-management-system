CREATE TABLE fraud_decisions (
    decision_id UUID PRIMARY KEY,
    transaction_id UUID NOT NULL,
    tenant_id VARCHAR(128) NOT NULL,
    mode VARCHAR(32) NOT NULL,
    outcome VARCHAR(32) NOT NULL,
    risk_score_value NUMERIC(5, 4) NOT NULL,
    risk_band VARCHAR(32) NOT NULL,
    risk_reason TEXT NOT NULL,
    rule_hit_count INTEGER NOT NULL,
    model_signal_count INTEGER NOT NULL,
    rule_evidence_summary TEXT NOT NULL,
    model_signal_summary TEXT NOT NULL,
    decided_at TIMESTAMP WITH TIME ZONE NOT NULL,
    event_type VARCHAR(128),
    event_id UUID,
    correlation_id VARCHAR(128),
    causation_id VARCHAR(128),
    trace_id VARCHAR(128),
    attributes_text TEXT NOT NULL DEFAULT '',
    CONSTRAINT fraud_decisions_mode_check CHECK (mode IN ('RULES', 'ML', 'HYBRID')),
    CONSTRAINT fraud_decisions_outcome_check CHECK (outcome IN ('APPROVE', 'REVIEW', 'BLOCK', 'ESCALATE')),
    CONSTRAINT fraud_decisions_risk_band_check CHECK (risk_band IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    CONSTRAINT fraud_decisions_rule_hit_count_check CHECK (rule_hit_count >= 0),
    CONSTRAINT fraud_decisions_model_signal_count_check CHECK (model_signal_count >= 0)
);

CREATE INDEX idx_fraud_decisions_transaction_decided_at
    ON fraud_decisions (transaction_id, decided_at DESC);

CREATE INDEX idx_fraud_decisions_tenant_decided_at
    ON fraud_decisions (tenant_id, decided_at DESC);

CREATE INDEX idx_fraud_decisions_correlation_id
    ON fraud_decisions (correlation_id);

CREATE INDEX idx_fraud_decisions_event_id
    ON fraud_decisions (event_id);
