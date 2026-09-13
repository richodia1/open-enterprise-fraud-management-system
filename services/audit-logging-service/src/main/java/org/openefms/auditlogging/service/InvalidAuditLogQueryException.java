package org.openefms.auditlogging.service;

import java.util.List;

public class InvalidAuditLogQueryException extends RuntimeException {
    private final List<String> errors;

    public InvalidAuditLogQueryException(List<String> errors) {
        super("Invalid audit log query: " + String.join("; ", errors));
        this.errors = List.copyOf(errors);
    }

    public List<String> errors() {
        return errors;
    }
}
