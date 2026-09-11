package org.openefms.explainableai.explanation;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.openefms.common.events.EventEnvelope;
import org.openefms.common.events.EventMetadata;
import org.openefms.common.events.EventTypes;
import org.openefms.common.events.explanation.ExplanationPreparedEvent;
import org.openefms.common.events.fraud.FraudDecisionProducedEvent;
import org.openefms.common.events.rabbitmq.RabbitMqTopology;
import org.openefms.common.models.explanation.ExplanationRequest;
import org.openefms.common.models.explanation.ExplanationResponse;
import org.openefms.common.models.fraud.FraudDecisionResponse;
import org.openefms.explainableai.messaging.ExplainableAiRabbitMqProperties;
import org.springframework.stereotype.Service;

@Service
class ExplanationOrchestrator {
    private static final int EXPLANATION_PREPARED_EVENT_VERSION = 1;
    private static final String SOURCE_SERVICE = "explainable-ai-service";

    private final DecisionSupportExplanationService explanationService;
    private final ExplainableAiRabbitMqProperties rabbitMqProperties;
    private final Clock clock;

    ExplanationOrchestrator(
            DecisionSupportExplanationService explanationService,
            ExplainableAiRabbitMqProperties rabbitMqProperties,
            Clock clock) {
        this.explanationService = explanationService;
        this.rabbitMqProperties = rabbitMqProperties;
        this.clock = clock;
    }

    ExplanationMessage explain(EventEnvelope<FraudDecisionProducedEvent> fraudDecisionEvent) {
        Objects.requireNonNull(fraudDecisionEvent, "fraudDecisionEvent must not be null");
        FraudDecisionResponse decision = fraudDecisionEvent.payload().decision();
        OffsetDateTime explainedAt = OffsetDateTime.now(clock);
        ExplanationResponse explanation = explanationService.explain(request(decision), explainedAt);
        EventEnvelope<ExplanationPreparedEvent> envelope = new EventEnvelope<>(
                explanationMetadata(fraudDecisionEvent.metadata(), explainedAt),
                new ExplanationPreparedEvent(explanation));

        return new ExplanationMessage(
                rabbitMqProperties.exchange(),
                RabbitMqTopology.routingKeyFor(EventTypes.EXPLANATION_PREPARED),
                envelope);
    }

    private ExplanationRequest request(FraudDecisionResponse decision) {
        Map<String, String> context = new LinkedHashMap<>(decision.attributes());
        context.put("decisionMode", decision.mode().name());
        context.put("decisionOutcome", decision.outcome().name());
        context.put("riskBand", decision.riskScore().band().name());
        context.put("riskScore", decision.riskScore().value().toPlainString());
        if (decision.riskScore().reason() != null && !decision.riskScore().reason().isBlank()) {
            context.put("riskReason", decision.riskScore().reason());
        }

        return new ExplanationRequest(
                UUID.randomUUID(),
                decision.decisionId(),
                decision.transactionId(),
                decision.tenantId(),
                decision.ruleHits(),
                decision.modelSignals(),
                context);
    }

    private EventMetadata explanationMetadata(EventMetadata sourceMetadata, OffsetDateTime explainedAt) {
        Map<String, String> attributes = new LinkedHashMap<>();
        attributes.put("causedByEventType", sourceMetadata.eventType());

        return new EventMetadata(
                UUID.randomUUID(),
                EventTypes.EXPLANATION_PREPARED,
                EXPLANATION_PREPARED_EVENT_VERSION,
                explainedAt,
                SOURCE_SERVICE,
                sourceMetadata.tenantId(),
                sourceMetadata.correlationId(),
                sourceMetadata.eventId().toString(),
                sourceMetadata.traceId(),
                attributes);
    }
}
