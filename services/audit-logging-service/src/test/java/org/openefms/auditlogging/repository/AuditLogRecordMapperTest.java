package org.openefms.auditlogging.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.openefms.auditlogging.entity.AuditLogRecord;
import org.openefms.common.events.EventMetadata;
import org.openefms.common.events.EventTypes;
import org.openefms.common.models.audit.AuditAction;
import org.openefms.common.models.audit.AuditEventRecord;

class AuditLogRecordMapperTest {
    private static final UUID AUDIT_EVENT_ID = UUID.fromString("c1c522e6-6f99-46a4-9795-96971ba894ff");
    private static final UUID EVENT_ID = UUID.fromString("44b52b93-8baa-47d0-8177-219314f1cf76");

    private final AuditLogRecordMapper mapper = new AuditLogRecordMapper();

    @Test
    void mapsAuditEventWithEnvelopeMetadata() {
        AuditLogRecord record = mapper.fromAuditEvent(auditEvent(), metadata());

        assertEquals(AUDIT_EVENT_ID, record.auditEventId());
        assertEquals("tenant-a", record.tenantId());
        assertEquals(AuditAction.CASE_CREATED, record.action());
        assertEquals(EventTypes.AUDIT_EVENT_RECORDED, record.eventType());
        assertEquals(EVENT_ID, record.eventId());
        assertEquals("correlation-1", record.correlationId());
        assertEquals("audit-logging-service", record.sourceService());
        assertEquals(OffsetDateTime.parse("2026-09-12T10:00:05Z"), record.eventOccurredAt());
    }

    @Test
    void mapsAuditLogRecordThroughEntity() {
        AuditLogRecord source = mapper.fromAuditEvent(auditEvent(), metadata());

        AuditLogRecord roundTripped = mapper.toRecord(mapper.toEntity(source));

        assertEquals(source.auditEventId(), roundTripped.auditEventId());
        assertEquals(source.tenantId(), roundTripped.tenantId());
        assertEquals(source.actorId(), roundTripped.actorId());
        assertEquals(source.action(), roundTripped.action());
        assertEquals(source.resourceType(), roundTripped.resourceType());
        assertEquals(source.resourceId(), roundTripped.resourceId());
        assertEquals(source.summary(), roundTripped.summary());
        assertEquals(source.occurredAt(), roundTripped.occurredAt());
        assertEquals(source.eventId(), roundTripped.eventId());
        assertEquals(source.attributes(), roundTripped.attributes());
    }

    private AuditEventRecord auditEvent() {
        return new AuditEventRecord(
                AUDIT_EVENT_ID,
                "tenant-a",
                "system:case-management-service",
                AuditAction.CASE_CREATED,
                "case",
                "61ab44d5-4b27-43d6-9f67-4de7f162d584",
                "Fraud investigation case created.",
                OffsetDateTime.parse("2026-09-12T10:00:00Z"),
                Map.of("priority", "MEDIUM", "riskBand", "HIGH"));
    }

    private EventMetadata metadata() {
        return new EventMetadata(
                EVENT_ID,
                EventTypes.AUDIT_EVENT_RECORDED,
                1,
                OffsetDateTime.parse("2026-09-12T10:00:05Z"),
                "audit-logging-service",
                "tenant-a",
                "correlation-1",
                "causation-1",
                "trace-1",
                Map.of());
    }
}
