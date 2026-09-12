package org.openefms.auditlogging.audit;

import org.openefms.common.events.EventMetadata;
import org.openefms.common.models.audit.AuditEventRecord;

interface AuditEventSink {
    void record(AuditEventRecord auditEvent, EventMetadata metadata);
}
