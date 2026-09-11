package org.openefms.auditlogging.audit;

import org.openefms.common.models.audit.AuditEventRecord;

interface AuditEventSink {
    void record(AuditEventRecord auditEvent);
}
