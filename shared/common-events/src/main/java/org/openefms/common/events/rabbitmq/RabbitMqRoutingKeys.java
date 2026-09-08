package org.openefms.common.events.rabbitmq;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import org.openefms.common.events.EventTypes;

/**
 * Routing keys mirror event names. Versioning belongs in EventMetadata.eventVersion.
 */
public final class RabbitMqRoutingKeys {
    public static final String ALERT_CREATED = EventTypes.ALERT_CREATED;
    public static final String AUDIT_EVENT_RECORDED = EventTypes.AUDIT_EVENT_RECORDED;
    public static final String CASE_CREATED = EventTypes.CASE_CREATED;
    public static final String EXPLANATION_PREPARED = EventTypes.EXPLANATION_PREPARED;
    public static final String FRAUD_DECISION_PRODUCED = EventTypes.FRAUD_DECISION_PRODUCED;
    public static final String TRANSACTION_RECEIVED = EventTypes.TRANSACTION_RECEIVED;

    private static final Map<String, String> ROUTING_KEYS_BY_EVENT_TYPE = routingKeysByEventType();

    private RabbitMqRoutingKeys() {
    }

    public static Map<String, String> byEventType() {
        return ROUTING_KEYS_BY_EVENT_TYPE;
    }

    public static String forEventType(String eventType) {
        String routingKey = ROUTING_KEYS_BY_EVENT_TYPE.get(
                Objects.requireNonNull(eventType, "eventType must not be null"));
        if (routingKey == null) {
            throw new IllegalArgumentException("No RabbitMQ routing key registered for event type: " + eventType);
        }
        return routingKey;
    }

    private static Map<String, String> routingKeysByEventType() {
        Map<String, String> routingKeys = new LinkedHashMap<>();
        routingKeys.put(EventTypes.TRANSACTION_RECEIVED, TRANSACTION_RECEIVED);
        routingKeys.put(EventTypes.FRAUD_DECISION_PRODUCED, FRAUD_DECISION_PRODUCED);
        routingKeys.put(EventTypes.EXPLANATION_PREPARED, EXPLANATION_PREPARED);
        routingKeys.put(EventTypes.ALERT_CREATED, ALERT_CREATED);
        routingKeys.put(EventTypes.CASE_CREATED, CASE_CREATED);
        routingKeys.put(EventTypes.AUDIT_EVENT_RECORDED, AUDIT_EVENT_RECORDED);
        return Collections.unmodifiableMap(routingKeys);
    }
}
