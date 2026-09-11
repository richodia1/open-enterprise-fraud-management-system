package org.openefms.frauddetection.transaction;

import org.openefms.frauddetection.decision.FraudDecisionMessage;

interface FraudDecisionMessageSink {
    void accept(FraudDecisionMessage message);
}
