package org.openefms.auditlogging.repository;

import java.util.List;
import java.util.UUID;
import org.openefms.auditlogging.entity.AuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataAuditLogRepository extends JpaRepository<AuditLogEntity, UUID> {
    public List<AuditLogEntity> findByTenantIdOrderByOccurredAtDesc(String tenantId);

    public List<AuditLogEntity> findByCorrelationIdOrderByOccurredAtDesc(String correlationId);

    public List<AuditLogEntity> findByTenantIdAndResourceTypeAndResourceIdOrderByOccurredAtDesc(
            String tenantId,
            String resourceType,
            String resourceId);
}
