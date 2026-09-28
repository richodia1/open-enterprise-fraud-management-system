package org.openefms.common.events;

/**
 * Initial event type names for Phase 1 contracts.
 */
public final class EventTypes {
    public static final String ALERT_CREATED = "alert.created";
    public static final String AUDIT_EVENT_RECORDED = "audit.event.recorded";
    public static final String CASE_CREATED = "case.created";
    public static final String EXPLANATION_PREPARED = "explanation.prepared";
    public static final String FRAUD_DECISION_PRODUCED = "fraud.decision.produced";
    public static final String FRAUD_FEEDBACK_READY = "fraud.feedback.ready";
    public static final String TRANSACTION_RECEIVED = "transaction.received";

    private EventTypes() {
    }
}
