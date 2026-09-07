package org.openefms.common.models.audit;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Shared audit trail contract for security, operational, and investigation events.
 */
public record AuditEventRecord(
        UUID auditEventId,
        String tenantId,
        String actorId,
        AuditAction action,
        String resourceType,
        String resourceId,
        String summary,
        OffsetDateTime occurredAt,
        Map<String, String> attributes
) {
    public AuditEventRecord {
        Objects.requireNonNull(auditEventId, "auditEventId must not be null");
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        Objects.requireNonNull(action, "action must not be null");
        Objects.requireNonNull(resourceType, "resourceType must not be null");
        Objects.requireNonNull(resourceId, "resourceId must not be null");
        Objects.requireNonNull(occurredAt, "occurredAt must not be null");
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }
}
