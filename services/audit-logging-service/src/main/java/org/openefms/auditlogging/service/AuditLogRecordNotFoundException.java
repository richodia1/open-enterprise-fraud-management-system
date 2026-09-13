package org.openefms.auditlogging.service;

import java.util.UUID;

public class AuditLogRecordNotFoundException extends RuntimeException {
    public AuditLogRecordNotFoundException(UUID auditEventId) {
        super("Audit event record was not found: " + auditEventId);
    }
}
