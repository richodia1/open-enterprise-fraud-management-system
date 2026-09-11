package org.openefms.frauddetection.transaction;

import org.openefms.frauddetection.decision.FraudDecisionMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
class LoggingFraudDecisionMessageSink implements FraudDecisionMessageSink {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingFraudDecisionMessageSink.class);

    @Override
    public void accept(FraudDecisionMessage message) {
        var decision = message.envelope().payload().decision();
        LOGGER.info(
                "Prepared fraud decision event transactionId={} outcome={} riskBand={} correlationId={}",
                decision.transactionId(),
                decision.outcome(),
                decision.riskScore().band(),
                message.envelope().metadata().correlationId());
    }
}
