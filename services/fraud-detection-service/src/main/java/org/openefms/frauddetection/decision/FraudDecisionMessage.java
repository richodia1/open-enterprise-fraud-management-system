package org.openefms.frauddetection.decision;

import java.util.Objects;
import org.openefms.common.events.EventEnvelope;
import org.openefms.common.events.fraud.FraudDecisionProducedEvent;

/**
 * Transport-ready decision message for RabbitMQ publication.
 */
public record FraudDecisionMessage(
        String exchangeName,
        String routingKey,
        EventEnvelope<FraudDecisionProducedEvent> envelope
) {
    public FraudDecisionMessage {
        if (exchangeName == null || exchangeName.isBlank()) {
            throw new IllegalArgumentException("exchangeName must not be blank");
        }
        if (routingKey == null || routingKey.isBlank()) {
            throw new IllegalArgumentException("routingKey must not be blank");
        }
        Objects.requireNonNull(envelope, "envelope must not be null");
    }
}
