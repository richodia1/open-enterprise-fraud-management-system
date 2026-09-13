package org.openefms.frauddetection.service;

import java.util.List;

public class InvalidFraudDecisionQueryException extends RuntimeException {
    private final List<String> errors;

    public InvalidFraudDecisionQueryException(List<String> errors) {
        super("Invalid fraud decision query: " + String.join("; ", errors));
        this.errors = List.copyOf(errors);
    }

    public List<String> errors() {
        return errors;
    }
}
