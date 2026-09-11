package org.openefms.frauddetection.decision;

import org.openefms.common.models.fraud.DecisionMode;

public class UnsupportedFraudDecisionModeException extends RuntimeException {
    private final DecisionMode mode;

    public UnsupportedFraudDecisionModeException(DecisionMode mode) {
        super("Fraud decision mode " + mode + " is configured, but no runtime is available for that mode.");
        this.mode = mode;
    }

    public DecisionMode mode() {
        return mode;
    }
}
