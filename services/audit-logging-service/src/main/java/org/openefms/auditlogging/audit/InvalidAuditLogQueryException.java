package org.openefms.auditlogging.audit;

import java.util.List;

class InvalidAuditLogQueryException extends RuntimeException {
    private final List<String> errors;

    InvalidAuditLogQueryException(List<String> errors) {
        super("Invalid audit log query: " + String.join("; ", errors));
        this.errors = List.copyOf(errors);
    }

    List<String> errors() {
        return errors;
    }
}
