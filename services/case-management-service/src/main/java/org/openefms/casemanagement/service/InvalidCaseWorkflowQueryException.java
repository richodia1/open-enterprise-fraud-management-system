package org.openefms.casemanagement.service;

import java.util.List;

public class InvalidCaseWorkflowQueryException extends RuntimeException {
    private final List<String> errors;

    public InvalidCaseWorkflowQueryException(List<String> errors) {
        super("Invalid case workflow query: " + String.join("; ", errors));
        this.errors = List.copyOf(errors);
    }

    public List<String> errors() {
        return errors;
    }
}
