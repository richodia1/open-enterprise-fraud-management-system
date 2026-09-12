package org.openefms.auditlogging.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.openefms.common.models.audit.AuditAction;

class AuditLogQueryServiceTest {
    private static final UUID AUDIT_EVENT_ID = UUID.fromString("c1c522e6-6f99-46a4-9795-96971ba894ff");

    @Test
    void readsAuditEventsByTenantCorrelationAndResource() {
        CapturingAuditLogRepository repository = new CapturingAuditLogRepository();
        repository.save(auditLogRecord());
        AuditLogQueryService queryService = new AuditLogQueryService(repository);

        List<AuditLogRecord> byTenant = queryService.auditEventsForTenant(" tenant-a ");
        List<AuditLogRecord> byCorrelation = queryService.auditEventsForCorrelation("correlation-1");
        List<AuditLogRecord> byResource = queryService.auditEventsForResource("tenant-a", "case", "case-1");

        assertEquals(AUDIT_EVENT_ID, queryService.auditEvent(AUDIT_EVENT_ID).auditEventId());
        assertEquals(1, byTenant.size());
        assertEquals(1, byCorrelation.size());
        assertEquals(1, byResource.size());
    }

    @Test
    void rejectsBlankQueries() {
        AuditLogQueryService queryService = new AuditLogQueryService(new CapturingAuditLogRepository());

        assertThrows(InvalidAuditLogQueryException.class, () -> queryService.auditEventsForTenant(" "));
        assertThrows(InvalidAuditLogQueryException.class, () -> queryService.auditEventsForCorrelation(null));
        assertThrows(InvalidAuditLogQueryException.class, () -> queryService.auditEventsForResource("tenant-a", "", "case-1"));
    }

    @Test
    void reportsMissingAuditEventRecords() {
        AuditLogQueryService queryService = new AuditLogQueryService(new CapturingAuditLogRepository());

        assertThrows(AuditLogRecordNotFoundException.class, () -> queryService.auditEvent(AUDIT_EVENT_ID));
    }

    private AuditLogRecord auditLogRecord() {
        return new AuditLogRecord(
                AUDIT_EVENT_ID,
                "tenant-a",
                "system:case-management-service",
                AuditAction.CASE_CREATED,
                "case",
                "case-1",
                "Fraud investigation case created.",
                OffsetDateTime.parse("2026-09-12T10:00:00Z"),
                "audit.event.recorded",
                UUID.fromString("44b52b93-8baa-47d0-8177-219314f1cf76"),
                "correlation-1",
                "causation-1",
                "trace-1",
                "case-management-service",
                OffsetDateTime.parse("2026-09-12T10:00:05Z"),
                Map.of("priority", "MEDIUM"));
    }

    private static final class CapturingAuditLogRepository implements AuditLogRepository {
        private final List<AuditLogRecord> records = new ArrayList<>();

        @Override
        public AuditLogRecord save(AuditLogRecord record) {
            records.add(record);
            return record;
        }

        @Override
        public Optional<AuditLogRecord> findByAuditEventId(UUID auditEventId) {
            return records.stream()
                    .filter(record -> record.auditEventId().equals(auditEventId))
                    .findFirst();
        }

        @Override
        public List<AuditLogRecord> findByTenantId(String tenantId) {
            return records.stream()
                    .filter(record -> record.tenantId().equals(tenantId))
                    .toList();
        }

        @Override
        public List<AuditLogRecord> findByCorrelationId(String correlationId) {
            return records.stream()
                    .filter(record -> record.correlationId().equals(correlationId))
                    .toList();
        }

        @Override
        public List<AuditLogRecord> findByResource(String tenantId, String resourceType, String resourceId) {
            return records.stream()
                    .filter(record -> record.tenantId().equals(tenantId))
                    .filter(record -> record.resourceType().equals(resourceType))
                    .filter(record -> record.resourceId().equals(resourceId))
                    .toList();
        }
    }
}
