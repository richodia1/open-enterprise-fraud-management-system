package org.openefms.auditlogging.audit;

import org.openefms.common.events.EventMetadata;
import org.openefms.common.models.audit.AuditEventRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
class PersistingAuditEventSink implements AuditEventSink {
    private static final Logger LOGGER = LoggerFactory.getLogger(PersistingAuditEventSink.class);

    private final AuditLogRepository repository;
    private final AuditLogRecordMapper mapper;

    PersistingAuditEventSink(AuditLogRepository repository, AuditLogRecordMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public void record(AuditEventRecord auditEvent, EventMetadata metadata) {
        AuditLogRecord savedRecord = repository.save(mapper.fromAuditEvent(auditEvent, metadata));
        LOGGER.info(
                "Persisted audit event auditEventId={} action={} resourceType={} resourceId={} tenantId={} correlationId={}",
                savedRecord.auditEventId(),
                savedRecord.action(),
                savedRecord.resourceType(),
                savedRecord.resourceId(),
                savedRecord.tenantId(),
                savedRecord.correlationId());
    }
}
