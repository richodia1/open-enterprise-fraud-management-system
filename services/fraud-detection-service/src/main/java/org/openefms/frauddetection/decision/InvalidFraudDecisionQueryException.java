package org.openefms.frauddetection.decision;

import java.util.List;

class InvalidFraudDecisionQueryException extends RuntimeException {
    private final List<String> errors;

    InvalidFraudDecisionQueryException(List<String> errors) {
        super("Invalid fraud decision query: " + String.join("; ", errors));
        this.errors = List.copyOf(errors);
    }

    List<String> errors() {
        return errors;
    }
}
