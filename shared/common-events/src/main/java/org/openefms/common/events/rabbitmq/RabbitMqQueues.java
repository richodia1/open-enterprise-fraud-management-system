package org.openefms.common.events.rabbitmq;

/**
 * Initial queue names owned by event-consuming services.
 */
public final class RabbitMqQueues {
    public static final String FRAUD_DETECTION_TRANSACTION_RECEIVED = "oefms.fraud-detection.transaction-received";
    public static final String EXPLAINABLE_AI_FRAUD_DECISION_PRODUCED = "oefms.explainable-ai.fraud-decision-produced";
    public static final String CASE_MANAGEMENT_EXPLANATION_PREPARED = "oefms.case-management.explanation-prepared";
    public static final String NOTIFICATION_MANAGEMENT_ALERT_CREATED = "oefms.notification-management.alert-created";
    public static final String NOTIFICATION_MANAGEMENT_CASE_CREATED = "oefms.notification-management.case-created";
    public static final String AUDIT_LOGGING_AUDIT_EVENT_RECORDED = "oefms.audit-logging.audit-event-recorded";
    public static final String TRANSACTION_CORE_FRAUD_FEEDBACK_READY = "oefms.transaction-core.fraud-feedback-ready";
    public static final String DEAD_LETTER = "oefms.dead-letter";

    private RabbitMqQueues() {
    }
}
