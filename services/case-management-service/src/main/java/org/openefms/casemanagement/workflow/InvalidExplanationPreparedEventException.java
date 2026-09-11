package org.openefms.casemanagement.workflow;

import java.util.List;

class InvalidExplanationPreparedEventException extends RuntimeException {
    InvalidExplanationPreparedEventException(List<String> errors) {
        super("Invalid explanation.prepared event: " + String.join("; ", errors));
    }
}
