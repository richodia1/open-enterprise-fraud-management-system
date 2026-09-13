package org.openefms.frauddetection.service;

import java.util.UUID;

public class FraudDecisionRecordNotFoundException extends RuntimeException {
    public FraudDecisionRecordNotFoundException(UUID decisionId) {
        super("Fraud decision record was not found: " + decisionId);
    }
}
