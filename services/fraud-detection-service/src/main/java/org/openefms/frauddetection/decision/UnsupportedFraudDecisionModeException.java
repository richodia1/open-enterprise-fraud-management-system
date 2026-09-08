package org.openefms.frauddetection.decision;

import org.openefms.common.models.fraud.DecisionMode;

public class UnsupportedFraudDecisionModeException extends RuntimeException {
    private final DecisionMode mode;

    public UnsupportedFraudDecisionModeException(DecisionMode mode) {
        super("Fraud decision mode " + mode + " is configured, but only RULES mode is active in Phase 1.");
        this.mode = mode;
    }

    public DecisionMode mode() {
        return mode;
    }
}
