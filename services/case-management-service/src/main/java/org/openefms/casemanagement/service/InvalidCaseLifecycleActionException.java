package org.openefms.casemanagement.service;

import java.util.List;

public class InvalidCaseLifecycleActionException extends RuntimeException {
    private final List<String> errors;

    public InvalidCaseLifecycleActionException(List<String> errors) {
        super(String.join("; ", errors));
        this.errors = List.copyOf(errors);
    }

    public List<String> errors() {
        return errors;
    }
}
