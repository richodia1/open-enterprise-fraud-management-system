package org.openefms.frauddetection.messaging;

public interface FraudDecisionMessageSink {
    public void accept(FraudDecisionMessage message);
}
