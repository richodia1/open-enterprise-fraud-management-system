package org.openefms.common.events.rabbitmq;

import java.util.List;

/**
 * Initial RabbitMQ topology shared by services that publish or consume domain events.
 */
public final class RabbitMqTopology {
    private static final List<RabbitMqBinding> DOMAIN_EVENT_BINDINGS = List.of(
            RabbitMqBinding.domainEvent(
                    RabbitMqQueues.FRAUD_DETECTION_TRANSACTION_RECEIVED,
                    RabbitMqRoutingKeys.TRANSACTION_RECEIVED),
            RabbitMqBinding.domainEvent(
                    RabbitMqQueues.EXPLAINABLE_AI_FRAUD_DECISION_PRODUCED,
                    RabbitMqRoutingKeys.FRAUD_DECISION_PRODUCED),
            RabbitMqBinding.domainEvent(
                    RabbitMqQueues.CASE_MANAGEMENT_EXPLANATION_PREPARED,
                    RabbitMqRoutingKeys.EXPLANATION_PREPARED),
            RabbitMqBinding.domainEvent(
                    RabbitMqQueues.TRANSACTION_CORE_FRAUD_FEEDBACK_READY,
                    RabbitMqRoutingKeys.FRAUD_FEEDBACK_READY),
            RabbitMqBinding.domainEvent(
                    RabbitMqQueues.NOTIFICATION_MANAGEMENT_ALERT_CREATED,
                    RabbitMqRoutingKeys.ALERT_CREATED),
            RabbitMqBinding.domainEvent(
                    RabbitMqQueues.NOTIFICATION_MANAGEMENT_CASE_CREATED,
                    RabbitMqRoutingKeys.CASE_CREATED),
            RabbitMqBinding.domainEvent(
                    RabbitMqQueues.AUDIT_LOGGING_AUDIT_EVENT_RECORDED,
                    RabbitMqRoutingKeys.AUDIT_EVENT_RECORDED)
    );

    private RabbitMqTopology() {
    }

    public static List<RabbitMqBinding> domainEventBindings() {
        return DOMAIN_EVENT_BINDINGS;
    }

    public static String routingKeyFor(String eventType) {
        return RabbitMqRoutingKeys.forEventType(eventType);
    }
}
