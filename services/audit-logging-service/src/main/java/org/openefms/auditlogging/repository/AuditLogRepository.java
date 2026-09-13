package org.openefms.auditlogging.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.openefms.auditlogging.entity.AuditLogRecord;

public interface AuditLogRepository {
    public AuditLogRecord save(AuditLogRecord record);

    public Optional<AuditLogRecord> findByAuditEventId(UUID auditEventId);

    public List<AuditLogRecord> findByTenantId(String tenantId);

    public List<AuditLogRecord> findByCorrelationId(String correlationId);

    public List<AuditLogRecord> findByResource(String tenantId, String resourceType, String resourceId);
}
