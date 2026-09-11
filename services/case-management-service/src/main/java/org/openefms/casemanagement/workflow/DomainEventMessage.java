package org.openefms.casemanagement.workflow;

import java.util.Objects;
import org.openefms.common.events.EventEnvelope;

public record DomainEventMessage<T>(
        String exchangeName,
        String routingKey,
        EventEnvelope<T> envelope
) {
    public DomainEventMessage {
        if (exchangeName == null || exchangeName.isBlank()) {
            throw new IllegalArgumentException("exchangeName must not be blank");
        }
        if (routingKey == null || routingKey.isBlank()) {
            throw new IllegalArgumentException("routingKey must not be blank");
        }
        Objects.requireNonNull(envelope, "envelope must not be null");
    }
}
