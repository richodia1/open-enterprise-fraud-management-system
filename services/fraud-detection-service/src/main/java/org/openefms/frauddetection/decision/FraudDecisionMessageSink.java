package org.openefms.frauddetection.decision;

public interface FraudDecisionMessageSink {
    void accept(FraudDecisionMessage message);
}
