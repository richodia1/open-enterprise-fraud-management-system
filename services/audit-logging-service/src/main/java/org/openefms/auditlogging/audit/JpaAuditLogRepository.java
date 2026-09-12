package org.openefms.auditlogging.audit;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
class JpaAuditLogRepository implements AuditLogRepository {
    private final SpringDataAuditLogRepository repository;
    private final AuditLogRecordMapper mapper;

    JpaAuditLogRepository(SpringDataAuditLogRepository repository, AuditLogRecordMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public AuditLogRecord save(AuditLogRecord record) {
        return mapper.toRecord(repository.save(mapper.toEntity(record)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AuditLogRecord> findByAuditEventId(UUID auditEventId) {
        return repository.findById(auditEventId).map(mapper::toRecord);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogRecord> findByTenantId(String tenantId) {
        return repository.findByTenantIdOrderByOccurredAtDesc(tenantId).stream()
                .map(mapper::toRecord)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogRecord> findByCorrelationId(String correlationId) {
        return repository.findByCorrelationIdOrderByOccurredAtDesc(correlationId).stream()
                .map(mapper::toRecord)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogRecord> findByResource(String tenantId, String resourceType, String resourceId) {
        return repository.findByTenantIdAndResourceTypeAndResourceIdOrderByOccurredAtDesc(
                        tenantId,
                        resourceType,
                        resourceId)
                .stream()
                .map(mapper::toRecord)
                .toList();
    }
}
