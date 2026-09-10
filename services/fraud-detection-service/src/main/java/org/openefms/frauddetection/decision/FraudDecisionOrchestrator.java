package org.openefms.frauddetection.decision;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.openefms.common.events.EventEnvelope;
import org.openefms.common.events.EventMetadata;
import org.openefms.common.events.EventTypes;
import org.openefms.common.events.fraud.FraudDecisionProducedEvent;
import org.openefms.common.events.rabbitmq.RabbitMqExchanges;
import org.openefms.common.events.rabbitmq.RabbitMqTopology;
import org.openefms.common.events.transaction.TransactionReceivedEvent;
import org.openefms.common.models.fraud.DecisionMode;
import org.openefms.common.models.fraud.FraudDecisionResponse;
import org.openefms.common.models.transaction.TransactionIngestionRequest;
import org.springframework.stereotype.Service;

@Service
public class FraudDecisionOrchestrator {
    private static final int FRAUD_DECISION_EVENT_VERSION = 1;
    private static final String SOURCE_SERVICE = "fraud-detection-service";

    private final FraudDecisionProperties properties;
    private final RuleBasedFraudDecisionEngine ruleBasedEngine;
    private final Clock clock;

    public FraudDecisionOrchestrator(
            FraudDecisionProperties properties,
            RuleBasedFraudDecisionEngine ruleBasedEngine,
            Clock clock) {
        this.properties = properties;
        this.ruleBasedEngine = ruleBasedEngine;
        this.clock = clock;
    }

    public FraudDecisionResponse decide(TransactionIngestionRequest transaction) {
        Objects.requireNonNull(transaction, "transaction must not be null");
        DecisionMode activeMode = properties.activeMode();
        if (activeMode != DecisionMode.RULES) {
            throw new UnsupportedFraudDecisionModeException(activeMode);
        }
        return ruleBasedEngine.decide(transaction, properties.rules(), properties.ml(), clock);
    }

    public FraudDecisionMessage decide(EventEnvelope<TransactionReceivedEvent> transactionEvent) {
        Objects.requireNonNull(transactionEvent, "transactionEvent must not be null");
        FraudDecisionResponse decision = decide(transactionEvent.payload().transaction());
        String routingKey = RabbitMqTopology.routingKeyFor(EventTypes.FRAUD_DECISION_PRODUCED);
        EventEnvelope<FraudDecisionProducedEvent> envelope = new EventEnvelope<>(
                decisionMetadata(transactionEvent.metadata()),
                new FraudDecisionProducedEvent(decision));

        return new FraudDecisionMessage(RabbitMqExchanges.DOMAIN_EVENTS, routingKey, envelope);
    }

    private EventMetadata decisionMetadata(EventMetadata sourceMetadata) {
        Map<String, String> attributes = new LinkedHashMap<>();
        attributes.put("causedByEventType", sourceMetadata.eventType());

        return new EventMetadata(
                UUID.randomUUID(),
                EventTypes.FRAUD_DECISION_PRODUCED,
                FRAUD_DECISION_EVENT_VERSION,
                OffsetDateTime.now(clock),
                SOURCE_SERVICE,
                sourceMetadata.tenantId(),
                sourceMetadata.correlationId(),
                sourceMetadata.eventId().toString(),
                sourceMetadata.traceId(),
                attributes);
    }
}
