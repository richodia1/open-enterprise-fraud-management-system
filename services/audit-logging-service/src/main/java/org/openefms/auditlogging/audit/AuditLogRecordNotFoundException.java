package org.openefms.auditlogging.audit;

import java.util.UUID;

class AuditLogRecordNotFoundException extends RuntimeException {
    AuditLogRecordNotFoundException(UUID auditEventId) {
        super("Audit event record was not found: " + auditEventId);
    }
}
