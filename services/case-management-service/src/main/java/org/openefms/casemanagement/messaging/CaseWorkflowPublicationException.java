package org.openefms.casemanagement.messaging;

public class CaseWorkflowPublicationException extends RuntimeException {
    private final String eventType;
    private final String exchangeName;
    private final String routingKey;

    public CaseWorkflowPublicationException(DomainEventMessage<?> message, Throwable cause) {
        super("Unable to publish case workflow event to the configured event transport.", cause);
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
