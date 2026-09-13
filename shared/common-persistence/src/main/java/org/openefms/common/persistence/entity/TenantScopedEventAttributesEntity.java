package org.openefms.common.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.util.UUID;

@MappedSuperclass
public abstract class TenantScopedEventAttributesEntity extends TenantScopedAttributesEntity {
    @Column(name = "event_type", length = 128)
    private String eventType;

    @Column(name = "event_id")
    private UUID eventId;

    @Column(name = "correlation_id", length = 128)
    private String correlationId;

    @Column(name = "trace_id", length = 128)
    private String traceId;

    protected TenantScopedEventAttributesEntity() {
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

    public String traceId() {
        return traceId;
    }

    public void traceId(String traceId) {
        this.traceId = traceId;
    }
}
