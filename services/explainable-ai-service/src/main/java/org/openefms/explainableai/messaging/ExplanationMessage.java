package org.openefms.explainableai.messaging;

import java.util.Objects;
import org.openefms.common.events.EventEnvelope;

public record ExplanationMessage<T>(
        String exchangeName,
        String routingKey,
        EventEnvelope<T> envelope
) {
    public ExplanationMessage {
        if (exchangeName == null || exchangeName.isBlank()) {
            throw new IllegalArgumentException("exchangeName must not be blank");
        }
        if (routingKey == null || routingKey.isBlank()) {
            throw new IllegalArgumentException("routingKey must not be blank");
        }
        Objects.requireNonNull(envelope, "envelope must not be null");
    }
}
