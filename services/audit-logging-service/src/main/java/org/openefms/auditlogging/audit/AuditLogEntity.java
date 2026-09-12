package org.openefms.auditlogging.audit;

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
class AuditLogEntity {
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

    protected AuditLogEntity() {
    }

    UUID auditEventId() {
        return auditEventId;
    }

    void auditEventId(UUID auditEventId) {
        this.auditEventId = auditEventId;
    }

    String tenantId() {
        return tenantId;
    }

    void tenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    String actorId() {
        return actorId;
    }

    void actorId(String actorId) {
        this.actorId = actorId;
    }

    AuditAction action() {
        return action;
    }

    void action(AuditAction action) {
        this.action = action;
    }

    String resourceType() {
        return resourceType;
    }

    void resourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    String resourceId() {
        return resourceId;
    }

    void resourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    String summary() {
        return summary;
    }

    void summary(String summary) {
        this.summary = summary;
    }

    OffsetDateTime occurredAt() {
        return occurredAt;
    }

    void occurredAt(OffsetDateTime occurredAt) {
        this.occurredAt = occurredAt;
    }

    String eventType() {
        return eventType;
    }

    void eventType(String eventType) {
        this.eventType = eventType;
    }

    UUID eventId() {
        return eventId;
    }

    void eventId(UUID eventId) {
        this.eventId = eventId;
    }

    String correlationId() {
        return correlationId;
    }

    void correlationId(String correlationId) {
        this.correlationId = correlationId;
    }

    String causationId() {
        return causationId;
    }

    void causationId(String causationId) {
        this.causationId = causationId;
    }

    String traceId() {
        return traceId;
    }

    void traceId(String traceId) {
        this.traceId = traceId;
    }

    String sourceService() {
        return sourceService;
    }

    void sourceService(String sourceService) {
        this.sourceService = sourceService;
    }

    OffsetDateTime eventOccurredAt() {
        return eventOccurredAt;
    }

    void eventOccurredAt(OffsetDateTime eventOccurredAt) {
        this.eventOccurredAt = eventOccurredAt;
    }

    String attributesText() {
        return attributesText;
    }

    void attributesText(String attributesText) {
        this.attributesText = attributesText;
    }
}
