package org.openefms.explainableai.explanation;

import java.util.List;

class InvalidFraudDecisionProducedEventException extends RuntimeException {
    InvalidFraudDecisionProducedEventException(List<String> errors) {
        super("Invalid fraud.decision.produced event: " + String.join("; ", errors));
    }
}
