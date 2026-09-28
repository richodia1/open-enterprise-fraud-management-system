package org.openefms.common.events.kafka;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import org.openefms.common.events.EventTypes;

/**
 * Kafka topic names for OEFMS integration events.
 */
public final class KafkaTopics {
    public static final String TRANSACTION_RECEIVED = "oefms.transaction.received";
    public static final String FRAUD_DECISION_PRODUCED = "oefms.fraud.decision.produced";
    public static final String EXPLANATION_PREPARED = "oefms.explanation.prepared";
    public static final String FRAUD_FEEDBACK_READY = "oefms.fraud.feedback.ready";
    public static final String ALERT_CREATED = "oefms.alert.created";
    public static final String CASE_CREATED = "oefms.case.created";
    public static final String AUDIT_EVENT_RECORDED = "oefms.audit.event.recorded";

    private static final Map<String, String> TOPICS_BY_EVENT_TYPE = topicsByEventType();

    private KafkaTopics() {
    }

    public static Map<String, String> byEventType() {
        return TOPICS_BY_EVENT_TYPE;
    }

    public static String forEventType(String eventType) {
        String topic = TOPICS_BY_EVENT_TYPE.get(Objects.requireNonNull(eventType, "eventType must not be null"));
        if (topic == null) {
            throw new IllegalArgumentException("No Kafka topic registered for event type: " + eventType);
        }
        return topic;
    }

    private static Map<String, String> topicsByEventType() {
        Map<String, String> topics = new LinkedHashMap<>();
        topics.put(EventTypes.TRANSACTION_RECEIVED, TRANSACTION_RECEIVED);
        topics.put(EventTypes.FRAUD_DECISION_PRODUCED, FRAUD_DECISION_PRODUCED);
        topics.put(EventTypes.EXPLANATION_PREPARED, EXPLANATION_PREPARED);
        topics.put(EventTypes.FRAUD_FEEDBACK_READY, FRAUD_FEEDBACK_READY);
        topics.put(EventTypes.ALERT_CREATED, ALERT_CREATED);
        topics.put(EventTypes.CASE_CREATED, CASE_CREATED);
        topics.put(EventTypes.AUDIT_EVENT_RECORDED, AUDIT_EVENT_RECORDED);
        return Collections.unmodifiableMap(topics);
    }
}
