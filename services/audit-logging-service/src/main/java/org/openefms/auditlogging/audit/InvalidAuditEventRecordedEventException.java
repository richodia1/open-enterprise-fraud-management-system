package org.openefms.auditlogging.audit;

import java.util.List;

class InvalidAuditEventRecordedEventException extends RuntimeException {
    InvalidAuditEventRecordedEventException(List<String> errors) {
        super("Invalid audit.event.recorded event: " + String.join("; ", errors));
    }
}
