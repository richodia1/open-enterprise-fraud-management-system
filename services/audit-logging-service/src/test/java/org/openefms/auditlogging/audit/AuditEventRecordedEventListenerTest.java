package org.openefms.auditlogging.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.openefms.common.events.EventEnvelope;
import org.openefms.common.events.EventMetadata;
import org.openefms.common.events.EventTypes;
import org.openefms.common.events.audit.AuditEventRecordedEvent;
import org.openefms.common.models.audit.AuditAction;
import org.openefms.common.models.audit.AuditEventRecord;

class AuditEventRecordedEventListenerTest {
    private static final UUID EVENT_ID = UUID.fromString("44b52b93-8baa-47d0-8177-219314f1cf76");

    @Test
    void consumesAuditEventAndRecordsIt() {
        CapturingAuditEventSink sink = new CapturingAuditEventSink();
        AuditEventRecordedEventListener listener = new AuditEventRecordedEventListener(sink);

        listener.handle(auditEvent(EventTypes.AUDIT_EVENT_RECORDED, 1));

        assertEquals(1, sink.auditEvents.size());
        assertEquals(AuditAction.CASE_CREATED, sink.auditEvents.getFirst().action());
        assertEquals("case", sink.auditEvents.getFirst().resourceType());
        assertEquals(EVENT_ID, sink.metadata.getFirst().eventId());
        assertEquals("correlation-1", sink.metadata.getFirst().correlationId());
    }

    @Test
    void rejectsEventsWithUnexpectedType() {
        CapturingAuditEventSink sink = new CapturingAuditEventSink();
        AuditEventRecordedEventListener listener = new AuditEventRecordedEventListener(sink);

        InvalidAuditEventRecordedEventException exception = assertThrows(
                InvalidAuditEventRecordedEventException.class,
                () -> listener.handle(auditEvent(EventTypes.CASE_CREATED, 1)));

        assertTrue(exception.getMessage().contains("eventType must be audit.event.recorded"));
        assertTrue(sink.auditEvents.isEmpty());
    }

    @Test
    void rejectsEventsWithUnsupportedVersion() {
        CapturingAuditEventSink sink = new CapturingAuditEventSink();
        AuditEventRecordedEventListener listener = new AuditEventRecordedEventListener(sink);

        InvalidAuditEventRecordedEventException exception = assertThrows(
                InvalidAuditEventRecordedEventException.class,
                () -> listener.handle(auditEvent(EventTypes.AUDIT_EVENT_RECORDED, 2)));

        assertTrue(exception.getMessage().contains("eventVersion must be 1"));
        assertTrue(sink.auditEvents.isEmpty());
    }

    private EventEnvelope<AuditEventRecordedEvent> auditEvent(String eventType, int eventVersion) {
        return new EventEnvelope<>(
                new EventMetadata(
                        EVENT_ID,
                        eventType,
                        eventVersion,
                        OffsetDateTime.parse("2026-09-11T11:00:01Z"),
                        "case-management-service",
                        "tenant-a",
                        "correlation-1",
                        "8fef9128-d7c1-49dd-bb03-c98f5105abef",
                        "trace-1",
                        Map.of()),
                new AuditEventRecordedEvent(auditRecord()));
    }

    private AuditEventRecord auditRecord() {
        return new AuditEventRecord(
                UUID.fromString("c1c522e6-6f99-46a4-9795-96971ba894ff"),
                "tenant-a",
                "system:case-management-service",
                AuditAction.CASE_CREATED,
                "case",
                "61ab44d5-4b27-43d6-9f67-4de7f162d584",
                "Fraud investigation case created.",
                OffsetDateTime.parse("2026-09-11T11:00:00Z"),
                Map.of("priority", "MEDIUM"));
    }

    private static final class CapturingAuditEventSink implements AuditEventSink {
        private final List<AuditEventRecord> auditEvents = new ArrayList<>();
        private final List<EventMetadata> metadata = new ArrayList<>();

        @Override
        public void record(AuditEventRecord auditEvent, EventMetadata metadata) {
            auditEvents.add(auditEvent);
            this.metadata.add(metadata);
        }
    }
}
