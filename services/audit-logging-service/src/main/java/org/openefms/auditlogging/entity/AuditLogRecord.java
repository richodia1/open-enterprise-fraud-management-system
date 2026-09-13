package org.openefms.auditlogging.entity;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.openefms.common.models.audit.AuditAction;

public record AuditLogRecord(
        UUID auditEventId,
        String tenantId,
        String actorId,
        AuditAction action,
        String resourceType,
        String resourceId,
        String summary,
        OffsetDateTime occurredAt,
        String eventType,
        UUID eventId,
        String correlationId,
        String causationId,
        String traceId,
        String sourceService,
        OffsetDateTime eventOccurredAt,
        Map<String, String> attributes
) {
    public AuditLogRecord {
        Objects.requireNonNull(auditEventId, "auditEventId must not be null");
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        Objects.requireNonNull(action, "action must not be null");
        Objects.requireNonNull(resourceType, "resourceType must not be null");
        Objects.requireNonNull(resourceId, "resourceId must not be null");
        Objects.requireNonNull(occurredAt, "occurredAt must not be null");
        actorId = actorId == null ? "" : actorId;
        summary = summary == null ? "" : summary;
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }
}
