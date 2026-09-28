package org.openefms.common.events.kafka;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.openefms.common.events.EventTypes;

class KafkaTopicsTest {
    @Test
    void mapsEveryWorkflowEventTypeToKafkaTopic() {
        assertEquals(KafkaTopics.TRANSACTION_RECEIVED, KafkaTopics.forEventType(EventTypes.TRANSACTION_RECEIVED));
        assertEquals(KafkaTopics.FRAUD_DECISION_PRODUCED, KafkaTopics.forEventType(EventTypes.FRAUD_DECISION_PRODUCED));
        assertEquals(KafkaTopics.EXPLANATION_PREPARED, KafkaTopics.forEventType(EventTypes.EXPLANATION_PREPARED));
        assertEquals(KafkaTopics.FRAUD_FEEDBACK_READY, KafkaTopics.forEventType(EventTypes.FRAUD_FEEDBACK_READY));
        assertEquals(KafkaTopics.ALERT_CREATED, KafkaTopics.forEventType(EventTypes.ALERT_CREATED));
        assertEquals(KafkaTopics.CASE_CREATED, KafkaTopics.forEventType(EventTypes.CASE_CREATED));
        assertEquals(KafkaTopics.AUDIT_EVENT_RECORDED, KafkaTopics.forEventType(EventTypes.AUDIT_EVENT_RECORDED));
    }

    @Test
    void exposesImmutableTopicMap() {
        Map<String, String> topics = KafkaTopics.byEventType();

        assertTrue(topics.containsKey(EventTypes.TRANSACTION_RECEIVED));
        assertThrows(UnsupportedOperationException.class, () -> topics.put("custom.event", "custom-topic"));
    }

    @Test
    void rejectsUnknownEventTypes() {
        assertThrows(IllegalArgumentException.class, () -> KafkaTopics.forEventType("unknown.event"));
    }
}
