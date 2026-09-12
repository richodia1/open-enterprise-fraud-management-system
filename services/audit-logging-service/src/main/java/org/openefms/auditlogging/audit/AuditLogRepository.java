package org.openefms.auditlogging.audit;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface AuditLogRepository {
    AuditLogRecord save(AuditLogRecord record);

    Optional<AuditLogRecord> findByAuditEventId(UUID auditEventId);

    List<AuditLogRecord> findByTenantId(String tenantId);

    List<AuditLogRecord> findByCorrelationId(String correlationId);

    List<AuditLogRecord> findByResource(String tenantId, String resourceType, String resourceId);
}
