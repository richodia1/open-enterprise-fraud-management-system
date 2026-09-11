package org.openefms.frauddetection.decision;

import java.util.UUID;

class FraudDecisionRecordNotFoundException extends RuntimeException {
    FraudDecisionRecordNotFoundException(UUID decisionId) {
        super("Fraud decision record was not found: " + decisionId);
    }
}
