package org.openefms.explainableai.messaging;

public class ExplanationPublicationException extends RuntimeException {
    private final String eventType;
    private final String exchangeName;
    private final String routingKey;

    public ExplanationPublicationException(ExplanationMessage message, Throwable cause) {
        super("Unable to publish explanation event to the configured event transport.", cause);
        this.eventType = message.envelope().metadata().eventType();
        this.exchangeName = message.exchangeName();
        this.routingKey = message.routingKey();
    }

    public String eventType() {
        return eventType;
    }

    public String exchangeName() {
        return exchangeName;
    }

    public String routingKey() {
        return routingKey;
    }
}
