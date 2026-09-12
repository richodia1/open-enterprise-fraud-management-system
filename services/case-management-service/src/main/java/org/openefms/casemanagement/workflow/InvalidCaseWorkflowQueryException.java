package org.openefms.casemanagement.workflow;

import java.util.List;

class InvalidCaseWorkflowQueryException extends RuntimeException {
    private final List<String> errors;

    InvalidCaseWorkflowQueryException(List<String> errors) {
        super("Invalid case workflow query: " + String.join("; ", errors));
        this.errors = List.copyOf(errors);
    }

    List<String> errors() {
        return errors;
    }
}
