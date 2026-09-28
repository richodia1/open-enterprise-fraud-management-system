package org.openefms.explainableai.service;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.openefms.common.events.EventEnvelope;
import org.openefms.common.events.EventMetadata;
import org.openefms.common.events.EventTypes;
import org.openefms.common.events.explanation.ExplanationPreparedEvent;
import org.openefms.common.events.feedback.FraudFeedbackReadyEvent;
import org.openefms.common.events.fraud.FraudDecisionProducedEvent;
import org.openefms.common.events.rabbitmq.RabbitMqTopology;
import org.openefms.common.models.explanation.ExplanationRequest;
import org.openefms.common.models.explanation.ExplanationResponse;
import org.openefms.common.models.feedback.TransactionFraudFeedbackResponse;
import org.openefms.common.models.fraud.FraudDecisionResponse;
import org.openefms.explainableai.messaging.ExplainableAiRabbitMqProperties;
import org.openefms.explainableai.messaging.ExplanationMessage;
import org.springframework.stereotype.Service;

@Service
public class ExplanationOrchestrator {
    private static final int EXPLANATION_PREPARED_EVENT_VERSION = 1;
    private static final int FRAUD_FEEDBACK_READY_EVENT_VERSION = 1;
    private static final String SOURCE_SERVICE = "explainable-ai-service";

    private final DecisionSupportExplanationService explanationService;
    private final ExplainableAiRabbitMqProperties rabbitMqProperties;
    private final Clock clock;

    public ExplanationOrchestrator(
            DecisionSupportExplanationService explanationService,
            ExplainableAiRabbitMqProperties rabbitMqProperties,
            Clock clock) {
        this.explanationService = explanationService;
        this.rabbitMqProperties = rabbitMqProperties;
        this.clock = clock;
    }

    public List<ExplanationMessage<?>> prepare(EventEnvelope<FraudDecisionProducedEvent> fraudDecisionEvent) {
        Objects.requireNonNull(fraudDecisionEvent, "fraudDecisionEvent must not be null");
        FraudDecisionResponse decision = fraudDecisionEvent.payload().decision();
        OffsetDateTime explainedAt = OffsetDateTime.now(clock);
        ExplanationResponse explanation = explain(decision, explainedAt);
        ExplanationMessage<ExplanationPreparedEvent> explanationMessage =
                explanationMessage(fraudDecisionEvent.metadata(), explanation, explainedAt);
        ExplanationMessage<FraudFeedbackReadyEvent> feedbackMessage = feedbackMessage(
                fraudDecisionEvent.metadata(),
                explanationMessage.envelope().metadata(),
                TransactionFraudFeedbackResponse.from(decision, explanation));

        return List.of(explanationMessage, feedbackMessage);
    }

    public ExplanationMessage<ExplanationPreparedEvent> explain(EventEnvelope<FraudDecisionProducedEvent> fraudDecisionEvent) {
        Objects.requireNonNull(fraudDecisionEvent, "fraudDecisionEvent must not be null");
        FraudDecisionResponse decision = fraudDecisionEvent.payload().decision();
        OffsetDateTime explainedAt = OffsetDateTime.now(clock);
        ExplanationResponse explanation = explain(decision, explainedAt);
        return explanationMessage(fraudDecisionEvent.metadata(), explanation, explainedAt);
    }

    private ExplanationMessage<ExplanationPreparedEvent> explanationMessage(
            EventMetadata sourceMetadata,
            ExplanationResponse explanation,
            OffsetDateTime explainedAt) {
        EventEnvelope<ExplanationPreparedEvent> envelope = new EventEnvelope<>(
                explanationMetadata(sourceMetadata, explainedAt),
                new ExplanationPreparedEvent(explanation));
        return new ExplanationMessage<>(
                rabbitMqProperties.exchange(),
                RabbitMqTopology.routingKeyFor(EventTypes.EXPLANATION_PREPARED),
                envelope);
    }

    private ExplanationMessage<FraudFeedbackReadyEvent> feedbackMessage(
            EventMetadata sourceMetadata,
            EventMetadata explanationMetadata,
            TransactionFraudFeedbackResponse feedback) {
        EventEnvelope<FraudFeedbackReadyEvent> envelope = new EventEnvelope<>(
                feedbackMetadata(sourceMetadata, explanationMetadata, feedback),
                new FraudFeedbackReadyEvent(feedback));
        return new ExplanationMessage<>(
                rabbitMqProperties.exchange(),
                RabbitMqTopology.routingKeyFor(EventTypes.FRAUD_FEEDBACK_READY),
                envelope);
    }

    public ExplanationResponse explain(FraudDecisionResponse decision) {
        return explain(decision, OffsetDateTime.now(clock));
    }

    public TransactionFraudFeedbackResponse feedback(FraudDecisionResponse decision) {
        ExplanationResponse explanation = explain(decision);
        return TransactionFraudFeedbackResponse.from(decision, explanation);
    }

    private ExplanationResponse explain(FraudDecisionResponse decision, OffsetDateTime explainedAt) {
        Objects.requireNonNull(decision, "decision must not be null");
        return explanationService.explain(request(decision), explainedAt);
    }

    private ExplanationRequest request(FraudDecisionResponse decision) {
        Map<String, String> context = new LinkedHashMap<>(decision.attributes());
        context.put("decisionMode", decision.mode().name());
        context.put("decisionOutcome", decision.outcome().name());
        context.put("recommendedAction", decision.recommendedAction().name());
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

    private EventMetadata feedbackMetadata(
            EventMetadata sourceMetadata,
            EventMetadata explanationMetadata,
            TransactionFraudFeedbackResponse feedback) {
        Map<String, String> attributes = new LinkedHashMap<>();
        attributes.put("causedByEventType", explanationMetadata.eventType());
        attributes.put("sourceDecisionEventId", sourceMetadata.eventId().toString());
        attributes.put("explanationEventId", explanationMetadata.eventId().toString());

        return new EventMetadata(
                UUID.randomUUID(),
                EventTypes.FRAUD_FEEDBACK_READY,
                FRAUD_FEEDBACK_READY_EVENT_VERSION,
                feedback.explainedAt(),
                SOURCE_SERVICE,
                sourceMetadata.tenantId(),
                sourceMetadata.correlationId(),
                explanationMetadata.eventId().toString(),
                sourceMetadata.traceId(),
                attributes);
    }
}
