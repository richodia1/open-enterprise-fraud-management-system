package org.openefms.auditlogging.audit;

import org.openefms.common.models.audit.AuditEventRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
class LoggingAuditEventSink implements AuditEventSink {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingAuditEventSink.class);

    @Override
    public void record(AuditEventRecord auditEvent) {
        LOGGER.info(
                "Recorded audit event action={} resourceType={} resourceId={} tenantId={}",
                auditEvent.action(),
                auditEvent.resourceType(),
                auditEvent.resourceId(),
                auditEvent.tenantId());
    }
}
