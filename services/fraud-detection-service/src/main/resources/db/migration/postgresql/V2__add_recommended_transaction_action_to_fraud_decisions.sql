ALTER TABLE fraud_decisions
    ADD COLUMN recommended_action VARCHAR(48);

UPDATE fraud_decisions
SET recommended_action = CASE outcome
    WHEN 'APPROVE' THEN 'PROCEED_TO_COMPLETION'
    WHEN 'REVIEW' THEN 'HOLD_FOR_MANUAL_REVIEW'
    WHEN 'BLOCK' THEN 'REJECT_TRANSACTION'
    WHEN 'ESCALATE' THEN 'ESCALATE_FOR_INVESTIGATION'
    ELSE 'HOLD_FOR_MANUAL_REVIEW'
END
WHERE recommended_action IS NULL;

ALTER TABLE fraud_decisions
    ALTER COLUMN recommended_action SET NOT NULL;

ALTER TABLE fraud_decisions
    ADD CONSTRAINT fraud_decisions_recommended_action_check
    CHECK (recommended_action IN (
        'PROCEED_TO_COMPLETION',
        'HOLD_FOR_MANUAL_REVIEW',
        'REJECT_TRANSACTION',
        'ESCALATE_FOR_INVESTIGATION'
    ));
