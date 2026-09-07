package org.openefms.common.events.fraud;

import org.openefms.common.models.fraud.FraudDecisionResponse;

import java.util.Objects;

/**
 * Event emitted after the Fraud Detection Service produces a preliminary decision.
 */
public record FraudDecisionProducedEvent(
        FraudDecisionResponse decision
) {
    public FraudDecisionProducedEvent {
        Objects.requireNonNull(decision, "decision must not be null");
    }
}
