package org.openefms.common.events;

import java.util.Objects;

/**
 * Generic event envelope for broker and audit-friendly payload transport.
 */
public record EventEnvelope<T>(
        EventMetadata metadata,
        T payload
) {
    public EventEnvelope {
        Objects.requireNonNull(metadata, "metadata must not be null");
        Objects.requireNonNull(payload, "payload must not be null");
    }
}
