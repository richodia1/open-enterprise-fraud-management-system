package org.openefms.casemanagement.messaging;

import org.openefms.casemanagement.workflow.DomainEventMessage;

class CaseWorkflowPublicationException extends RuntimeException {
    private final String eventType;
    private final String exchangeName;
    private final String routingKey;

    CaseWorkflowPublicationException(DomainEventMessage<?> message, Throwable cause) {
        super("Unable to publish case workflow event to RabbitMQ.", cause);
        this.eventType = message.envelope().metadata().eventType();
        this.exchangeName = message.exchangeName();
        this.routingKey = message.routingKey();
    }

    String eventType() {
        return eventType;
    }

    String exchangeName() {
        return exchangeName;
    }

    String routingKey() {
        return routingKey;
    }
}
