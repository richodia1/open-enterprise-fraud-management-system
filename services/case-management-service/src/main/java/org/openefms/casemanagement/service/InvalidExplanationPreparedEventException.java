package org.openefms.casemanagement.service;

import java.util.List;

public class InvalidExplanationPreparedEventException extends RuntimeException {
    public InvalidExplanationPreparedEventException(List<String> errors) {
        super("Invalid explanation.prepared event: " + String.join("; ", errors));
    }
}
