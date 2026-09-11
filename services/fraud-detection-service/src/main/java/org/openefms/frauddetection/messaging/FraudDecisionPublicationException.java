package org.openefms.frauddetection.messaging;

import org.openefms.frauddetection.decision.FraudDecisionMessage;

class FraudDecisionPublicationException extends RuntimeException {
    private final String eventType;
    private final String exchangeName;
    private final String routingKey;

    FraudDecisionPublicationException(FraudDecisionMessage message, Throwable cause) {
        super("Unable to publish fraud decision event to RabbitMQ.", cause);
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
