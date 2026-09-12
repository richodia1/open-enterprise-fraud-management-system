package org.openefms.auditlogging.audit;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataAuditLogRepository extends JpaRepository<AuditLogEntity, UUID> {
    List<AuditLogEntity> findByTenantIdOrderByOccurredAtDesc(String tenantId);

    List<AuditLogEntity> findByCorrelationIdOrderByOccurredAtDesc(String correlationId);

    List<AuditLogEntity> findByTenantIdAndResourceTypeAndResourceIdOrderByOccurredAtDesc(
            String tenantId,
            String resourceType,
            String resourceId);
}
