package org.openefms.common.events.rabbitmq;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.openefms.common.events.EventTypes;

class RabbitMqTopologyTest {
    @Test
    void routesEveryInitialEventTypeWithVersionlessKeys() {
        assertEquals(EventTypes.TRANSACTION_RECEIVED, RabbitMqTopology.routingKeyFor(EventTypes.TRANSACTION_RECEIVED));
        assertEquals(
                EventTypes.FRAUD_DECISION_PRODUCED,
                RabbitMqTopology.routingKeyFor(EventTypes.FRAUD_DECISION_PRODUCED));
        assertEquals(EventTypes.EXPLANATION_PREPARED, RabbitMqTopology.routingKeyFor(EventTypes.EXPLANATION_PREPARED));
        assertEquals(EventTypes.ALERT_CREATED, RabbitMqTopology.routingKeyFor(EventTypes.ALERT_CREATED));
        assertEquals(EventTypes.CASE_CREATED, RabbitMqTopology.routingKeyFor(EventTypes.CASE_CREATED));
        assertEquals(EventTypes.AUDIT_EVENT_RECORDED, RabbitMqTopology.routingKeyFor(EventTypes.AUDIT_EVENT_RECORDED));
    }

    @Test
    void declaresInitialConsumerBindings() {
        List<RabbitMqBinding> bindings = RabbitMqTopology.domainEventBindings();

        assertEquals(6, bindings.size());
        assertTrue(bindings.contains(RabbitMqBinding.domainEvent(
                RabbitMqQueues.FRAUD_DETECTION_TRANSACTION_RECEIVED,
                RabbitMqRoutingKeys.TRANSACTION_RECEIVED)));
        assertTrue(bindings.contains(RabbitMqBinding.domainEvent(
                RabbitMqQueues.EXPLAINABLE_AI_FRAUD_DECISION_PRODUCED,
                RabbitMqRoutingKeys.FRAUD_DECISION_PRODUCED)));
        assertTrue(bindings.contains(RabbitMqBinding.domainEvent(
                RabbitMqQueues.CASE_MANAGEMENT_EXPLANATION_PREPARED,
                RabbitMqRoutingKeys.EXPLANATION_PREPARED)));
        assertTrue(bindings.contains(RabbitMqBinding.domainEvent(
                RabbitMqQueues.NOTIFICATION_MANAGEMENT_ALERT_CREATED,
                RabbitMqRoutingKeys.ALERT_CREATED)));
        assertTrue(bindings.contains(RabbitMqBinding.domainEvent(
                RabbitMqQueues.NOTIFICATION_MANAGEMENT_CASE_CREATED,
                RabbitMqRoutingKeys.CASE_CREATED)));
        assertTrue(bindings.contains(RabbitMqBinding.domainEvent(
                RabbitMqQueues.AUDIT_LOGGING_AUDIT_EVENT_RECORDED,
                RabbitMqRoutingKeys.AUDIT_EVENT_RECORDED)));
    }

    @Test
    void rejectsUnknownEventTypes() {
        assertThrows(IllegalArgumentException.class, () -> RabbitMqTopology.routingKeyFor("unknown.event"));
    }
}
