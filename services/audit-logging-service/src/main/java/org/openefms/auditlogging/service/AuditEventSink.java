package org.openefms.auditlogging.service;

import org.openefms.common.events.EventMetadata;
import org.openefms.common.models.audit.AuditEventRecord;

public interface AuditEventSink {
    public void record(AuditEventRecord auditEvent, EventMetadata metadata);
}
