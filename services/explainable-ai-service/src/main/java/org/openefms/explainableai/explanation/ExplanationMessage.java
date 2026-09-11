package org.openefms.explainableai.explanation;

import java.util.Objects;
import org.openefms.common.events.EventEnvelope;
import org.openefms.common.events.explanation.ExplanationPreparedEvent;

public record ExplanationMessage(
        String exchangeName,
        String routingKey,
        EventEnvelope<ExplanationPreparedEvent> envelope
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
