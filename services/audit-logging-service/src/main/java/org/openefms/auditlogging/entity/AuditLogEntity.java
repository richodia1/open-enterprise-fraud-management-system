package org.openefms.auditlogging.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.openefms.common.models.audit.AuditAction;
import org.openefms.common.persistence.entity.TenantScopedCausalEventAttributesEntity;

@Entity
@Table(name = "audit_events")
public class AuditLogEntity extends TenantScopedCausalEventAttributesEntity {
    @Id
    @Column(name = "audit_event_id", nullable = false)
    private UUID auditEventId;

    @Column(name = "actor_id", nullable = false, length = 128)
    private String actorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 64)
    private AuditAction action;

    @Column(name = "resource_type", nullable = false, length = 128)
    private String resourceType;

    @Column(name = "resource_id", nullable = false, length = 128)
    private String resourceId;

    @Column(name = "summary", nullable = false, columnDefinition = "text")
    private String summary;

    @Column(name = "occurred_at", nullable = false)
    private OffsetDateTime occurredAt;

    @Column(name = "source_service", length = 128)
    private String sourceService;

    @Column(name = "event_occurred_at")
    private OffsetDateTime eventOccurredAt;

    public AuditLogEntity() {
    }

    public UUID auditEventId() {
        return auditEventId;
    }

    public void auditEventId(UUID auditEventId) {
        this.auditEventId = auditEventId;
    }

    public String actorId() {
        return actorId;
    }

    public void actorId(String actorId) {
        this.actorId = actorId;
    }

    public AuditAction action() {
        return action;
    }

    public void action(AuditAction action) {
        this.action = action;
    }

    public String resourceType() {
        return resourceType;
    }

    public void resourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String resourceId() {
        return resourceId;
    }

    public void resourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String summary() {
        return summary;
    }

    public void summary(String summary) {
        this.summary = summary;
    }

    public OffsetDateTime occurredAt() {
        return occurredAt;
    }

    public void occurredAt(OffsetDateTime occurredAt) {
        this.occurredAt = occurredAt;
    }

    public String sourceService() {
        return sourceService;
    }

    public void sourceService(String sourceService) {
        this.sourceService = sourceService;
    }

    public OffsetDateTime eventOccurredAt() {
        return eventOccurredAt;
    }

    public void eventOccurredAt(OffsetDateTime eventOccurredAt) {
        this.eventOccurredAt = eventOccurredAt;
    }

}
