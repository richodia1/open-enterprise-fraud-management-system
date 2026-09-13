ALTER TABLE fraud_cases
    ADD COLUMN assignee_id VARCHAR(128),
    ADD COLUMN assigned_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN updated_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN closed_at TIMESTAMP WITH TIME ZONE;

UPDATE fraud_cases
SET updated_at = created_at
WHERE updated_at IS NULL;

ALTER TABLE fraud_cases
    ALTER COLUMN updated_at SET NOT NULL,
    ALTER COLUMN updated_at SET DEFAULT CURRENT_TIMESTAMP,
    DROP CONSTRAINT fraud_cases_status_check,
    ADD CONSTRAINT fraud_cases_status_check
        CHECK (status IN ('OPEN', 'IN_REVIEW', 'ESCALATED', 'FRAUD_CONFIRMED', 'FALSE_POSITIVE', 'CLOSED'));

ALTER TABLE fraud_alerts
    DROP CONSTRAINT fraud_alerts_status_check,
    ADD CONSTRAINT fraud_alerts_status_check
        CHECK (status IN ('OPEN', 'ACKNOWLEDGED', 'CLOSED'));

CREATE TABLE fraud_case_actions (
    case_action_id UUID PRIMARY KEY,
    case_id UUID NOT NULL REFERENCES fraud_cases (case_id),
    tenant_id VARCHAR(128) NOT NULL,
    action_type VARCHAR(64) NOT NULL,
    actor_id VARCHAR(128) NOT NULL,
    note TEXT NOT NULL DEFAULT '',
    from_status VARCHAR(32),
    to_status VARCHAR(32),
    assignee_id VARCHAR(128),
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    attributes_text TEXT NOT NULL DEFAULT '',
    CONSTRAINT fraud_case_actions_type_check CHECK (action_type IN ('ASSIGNED', 'STATUS_CHANGED')),
    CONSTRAINT fraud_case_actions_from_status_check
        CHECK (from_status IS NULL OR from_status IN ('OPEN', 'IN_REVIEW', 'ESCALATED', 'FRAUD_CONFIRMED', 'FALSE_POSITIVE', 'CLOSED')),
    CONSTRAINT fraud_case_actions_to_status_check
        CHECK (to_status IS NULL OR to_status IN ('OPEN', 'IN_REVIEW', 'ESCALATED', 'FRAUD_CONFIRMED', 'FALSE_POSITIVE', 'CLOSED'))
);

CREATE INDEX idx_fraud_case_actions_case_occurred_at
    ON fraud_case_actions (case_id, occurred_at ASC);

CREATE INDEX idx_fraud_case_actions_tenant_occurred_at
    ON fraud_case_actions (tenant_id, occurred_at DESC);
