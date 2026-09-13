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

@Entity
@Table(name = "audit_events")
public class AuditLogEntity {
    @Id
    @Column(name = "audit_event_id", nullable = false)
    private UUID auditEventId;

    @Column(name = "tenant_id", nullable = false, length = 128)
    private String tenantId;

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

    @Column(name = "event_type", length = 128)
    private String eventType;

    @Column(name = "event_id")
    private UUID eventId;

    @Column(name = "correlation_id", length = 128)
    private String correlationId;

    @Column(name = "causation_id", length = 128)
    private String causationId;

    @Column(name = "trace_id", length = 128)
    private String traceId;

    @Column(name = "source_service", length = 128)
    private String sourceService;

    @Column(name = "event_occurred_at")
    private OffsetDateTime eventOccurredAt;

    @Column(name = "attributes_text", nullable = false, columnDefinition = "text")
    private String attributesText;

    public AuditLogEntity() {
    }

    public UUID auditEventId() {
        return auditEventId;
    }

    public void auditEventId(UUID auditEventId) {
        this.auditEventId = auditEventId;
    }

    public String tenantId() {
        return tenantId;
    }

    public void tenantId(String tenantId) {
        this.tenantId = tenantId;
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

    public String eventType() {
        return eventType;
    }

    public void eventType(String eventType) {
        this.eventType = eventType;
    }

    public UUID eventId() {
        return eventId;
    }

    public void eventId(UUID eventId) {
        this.eventId = eventId;
    }

    public String correlationId() {
        return correlationId;
    }

    public void correlationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String causationId() {
        return causationId;
    }

    public void causationId(String causationId) {
        this.causationId = causationId;
    }

    public String traceId() {
        return traceId;
    }

    public void traceId(String traceId) {
        this.traceId = traceId;
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

    public String attributesText() {
        return attributesText;
    }

    public void attributesText(String attributesText) {
        this.attributesText = attributesText;
    }
}
