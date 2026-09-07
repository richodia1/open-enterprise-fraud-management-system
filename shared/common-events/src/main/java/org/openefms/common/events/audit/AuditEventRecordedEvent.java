package org.openefms.common.events.audit;

import org.openefms.common.models.audit.AuditEventRecord;

import java.util.Objects;

/**
 * Event emitted when an audit record is captured for downstream consumers.
 */
public record AuditEventRecordedEvent(
        AuditEventRecord auditEvent
) {
    public AuditEventRecordedEvent {
        Objects.requireNonNull(auditEvent, "auditEvent must not be null");
    }
}
