package org.openefms.ingestionmanagement.messaging;

import java.util.Objects;
import org.openefms.common.events.EventEnvelope;
import org.openefms.common.events.transaction.TransactionReceivedEvent;

/**
 * Transport-ready ingestion message for the next RabbitMQ publisher branch.
 */
public record TransactionIngestionMessage(
        String exchangeName,
        String routingKey,
        EventEnvelope<TransactionReceivedEvent> envelope
) {
    public TransactionIngestionMessage {
        if (exchangeName == null || exchangeName.isBlank()) {
            throw new IllegalArgumentException("exchangeName must not be blank");
        }
        if (routingKey == null || routingKey.isBlank()) {
            throw new IllegalArgumentException("routingKey must not be blank");
        }
        Objects.requireNonNull(envelope, "envelope must not be null");
    }
}
