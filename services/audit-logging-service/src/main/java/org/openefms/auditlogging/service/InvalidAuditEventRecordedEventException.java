package org.openefms.auditlogging.service;

import java.util.List;

public class InvalidAuditEventRecordedEventException extends RuntimeException {
    public InvalidAuditEventRecordedEventException(List<String> errors) {
        super("Invalid audit.event.recorded event: " + String.join("; ", errors));
    }
}
