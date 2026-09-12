package org.openefms.auditlogging.audit;

class AuditLogPersistenceException extends RuntimeException {
    AuditLogPersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
