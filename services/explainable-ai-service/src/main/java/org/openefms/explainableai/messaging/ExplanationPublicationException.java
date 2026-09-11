package org.openefms.explainableai.messaging;

import org.openefms.explainableai.explanation.ExplanationMessage;

class ExplanationPublicationException extends RuntimeException {
    private final String eventType;
    private final String exchangeName;
    private final String routingKey;

    ExplanationPublicationException(ExplanationMessage message, Throwable cause) {
        super("Unable to publish explanation event to RabbitMQ.", cause);
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
