package org.openefms.explainableai.service;

import java.util.List;

public class InvalidFraudDecisionProducedEventException extends RuntimeException {
    public InvalidFraudDecisionProducedEventException(List<String> errors) {
        super("Invalid fraud.decision.produced event: " + String.join("; ", errors));
    }
}
