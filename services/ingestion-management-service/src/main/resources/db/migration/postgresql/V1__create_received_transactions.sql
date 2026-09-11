CREATE TABLE received_transactions (
    transaction_id UUID PRIMARY KEY,
    external_reference VARCHAR(128),
    tenant_id VARCHAR(128) NOT NULL,
    transaction_type VARCHAR(64) NOT NULL,
    channel VARCHAR(64) NOT NULL,
    direction VARCHAR(64) NOT NULL,
    amount_value NUMERIC(19, 4) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    originator_party_reference VARCHAR(128),
    originator_customer_reference VARCHAR(128),
    originator_account_reference VARCHAR(128),
    originator_account_number_hash VARCHAR(256),
    originator_institution_code VARCHAR(64),
    originator_country_code VARCHAR(2),
    beneficiary_party_reference VARCHAR(128),
    beneficiary_customer_reference VARCHAR(128),
    beneficiary_account_reference VARCHAR(128),
    beneficiary_account_number_hash VARCHAR(256),
    beneficiary_institution_code VARCHAR(64),
    beneficiary_country_code VARCHAR(2),
    initiated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    accepted_at TIMESTAMP WITH TIME ZONE NOT NULL,
    status VARCHAR(32) NOT NULL,
    event_type VARCHAR(128) NOT NULL,
    event_id UUID NOT NULL,
    correlation_id VARCHAR(128) NOT NULL,
    trace_id VARCHAR(128),
    published_at TIMESTAMP WITH TIME ZONE,
    attributes_text TEXT NOT NULL DEFAULT '',
    CONSTRAINT received_transactions_status_check CHECK (status IN ('RECEIVED', 'PUBLISHED'))
);

CREATE INDEX idx_received_transactions_tenant_accepted_at
    ON received_transactions (tenant_id, accepted_at DESC);

CREATE INDEX idx_received_transactions_external_reference
    ON received_transactions (external_reference);

CREATE INDEX idx_received_transactions_correlation_id
    ON received_transactions (correlation_id);
