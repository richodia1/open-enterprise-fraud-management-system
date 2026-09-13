package org.openefms.auditlogging.service;

public class AuditLogPersistenceException extends RuntimeException {
    public AuditLogPersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
