package org.openefms.ingestionmanagement.transaction;

public class TransactionEventPublicationException extends RuntimeException {
    private final String eventType;
    private final String exchangeName;
    private final String routingKey;

    public TransactionEventPublicationException(TransactionIngestionMessage message, Throwable cause) {
        super("Unable to publish transaction event to RabbitMQ.", cause);
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
