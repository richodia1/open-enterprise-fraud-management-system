package org.openefms.common.events;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Common metadata every integration event should carry.
 */
public record EventMetadata(
        UUID eventId,
        String eventType,
        int eventVersion,
        OffsetDateTime occurredAt,
        String sourceService,
        String tenantId,
        String correlationId,
        String causationId,
        String traceId,
        Map<String, String> attributes
) {
    public EventMetadata {
        Objects.requireNonNull(eventId, "eventId must not be null");
        Objects.requireNonNull(eventType, "eventType must not be null");
        Objects.requireNonNull(occurredAt, "occurredAt must not be null");
        Objects.requireNonNull(sourceService, "sourceService must not be null");
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }
}
