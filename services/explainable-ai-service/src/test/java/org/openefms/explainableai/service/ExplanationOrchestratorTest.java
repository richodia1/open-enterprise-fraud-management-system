package org.openefms.explainableai.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.openefms.common.events.EventEnvelope;
import org.openefms.common.events.EventMetadata;
import org.openefms.common.events.EventTypes;
import org.openefms.common.events.fraud.FraudDecisionProducedEvent;
import org.openefms.common.events.rabbitmq.RabbitMqExchanges;
import org.openefms.common.events.rabbitmq.RabbitMqRoutingKeys;
import org.openefms.common.models.feedback.TransactionFraudFeedbackResponse;
import org.openefms.common.models.fraud.DecisionMode;
import org.openefms.common.models.fraud.DecisionOutcome;
import org.openefms.common.models.fraud.FraudDecisionResponse;
import org.openefms.common.models.fraud.RecommendedTransactionAction;
import org.openefms.common.models.fraud.RiskBand;
import org.openefms.common.models.fraud.RiskScore;
import org.openefms.common.models.fraud.RuleHit;
import org.openefms.explainableai.messaging.ExplainableAiRabbitMqProperties;
import org.openefms.explainableai.messaging.ExplanationMessage;

class ExplanationOrchestratorTest {
    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-09-11T10:00:00Z"), ZoneOffset.UTC);
    private static final UUID SOURCE_EVENT_ID = UUID.fromString("fb40ff28-86cc-4b6c-a6df-b538d5155d2e");
    private static final UUID DECISION_ID = UUID.fromString("325dc0f5-775d-4858-89be-1992e52fe2f0");
    private static final UUID TRANSACTION_ID = UUID.fromString("118f3bd4-b208-44ff-b1ff-447c78d80d02");

    @Test
    void preparesExplanationEventFromFraudDecisionEvent() {
        ExplanationOrchestrator orchestrator = new ExplanationOrchestrator(
                new DecisionSupportExplanationService(),
                new ExplainableAiRabbitMqProperties(RabbitMqExchanges.DOMAIN_EVENTS, RabbitMqExchanges.DEAD_LETTER),
                FIXED_CLOCK);

        ExplanationMessage message = orchestrator.explain(fraudDecisionEvent());

        assertEquals(RabbitMqExchanges.DOMAIN_EVENTS, message.exchangeName());
        assertEquals(RabbitMqRoutingKeys.EXPLANATION_PREPARED, message.routingKey());
        assertEquals(EventTypes.EXPLANATION_PREPARED, message.envelope().metadata().eventType());
        assertEquals("explainable-ai-service", message.envelope().metadata().sourceService());
        assertEquals("tenant-a", message.envelope().metadata().tenantId());
        assertEquals("correlation-1", message.envelope().metadata().correlationId());
        assertEquals(SOURCE_EVENT_ID.toString(), message.envelope().metadata().causationId());
        assertEquals(OffsetDateTime.parse("2026-09-11T10:00:00Z"), message.envelope().metadata().occurredAt());
        assertEquals(DECISION_ID, message.envelope().payload().explanation().decisionId());
        assertEquals(TRANSACTION_ID, message.envelope().payload().explanation().transactionId());
        assertEquals(
                RecommendedTransactionAction.HOLD_FOR_MANUAL_REVIEW.name(),
                message.envelope().payload().explanation().attributes().get("recommendedAction"));
        assertEquals("RULE", message.envelope().payload().explanation().evidence().getFirst().evidenceType());
    }

    @Test
    void preparesTransactionFeedbackWithDecisionAndExplanationDetails() {
        ExplanationOrchestrator orchestrator = new ExplanationOrchestrator(
                new DecisionSupportExplanationService(),
                new ExplainableAiRabbitMqProperties(RabbitMqExchanges.DOMAIN_EVENTS, RabbitMqExchanges.DEAD_LETTER),
                FIXED_CLOCK);

        TransactionFraudFeedbackResponse feedback = orchestrator.feedback(decision());

        assertEquals(TRANSACTION_ID, feedback.transactionId());
        assertEquals(DECISION_ID, feedback.decisionId());
        assertEquals("tenant-a", feedback.tenantId());
        assertEquals(DecisionOutcome.REVIEW, feedback.outcome());
        assertEquals(RecommendedTransactionAction.HOLD_FOR_MANUAL_REVIEW, feedback.recommendedAction());
        assertEquals(new BigDecimal("0.42"), feedback.riskScore().value());
        assertEquals("Decision REVIEW with MEDIUM risk score 0.42. Recommended action: HOLD_FOR_MANUAL_REVIEW. "
                        + "Key evidence: High value amount.",
                feedback.explanationSummary());
        assertEquals("RULE", feedback.evidence().getFirst().evidenceType());
        assertEquals("0.42", feedback.attributes().get("riskScore"));
        assertEquals(OffsetDateTime.parse("2026-09-11T10:00:00Z"), feedback.explainedAt());
    }

    private EventEnvelope<FraudDecisionProducedEvent> fraudDecisionEvent() {
        return new EventEnvelope<>(
                new EventMetadata(
                        SOURCE_EVENT_ID,
                        EventTypes.FRAUD_DECISION_PRODUCED,
                        1,
                        OffsetDateTime.parse("2026-09-11T09:00:01Z"),
                        "fraud-detection-service",
                        "tenant-a",
                        "correlation-1",
                        "7e37875e-7443-47d4-931c-df8ce0f10cab",
                        "trace-1",
                        Map.of("causedByEventType", EventTypes.TRANSACTION_RECEIVED)),
                new FraudDecisionProducedEvent(decision()));
    }

    private FraudDecisionResponse decision() {
        return new FraudDecisionResponse(
                DECISION_ID,
                TRANSACTION_ID,
                "tenant-a",
                DecisionMode.RULES,
                DecisionOutcome.REVIEW,
                new RiskScore(new BigDecimal("0.42"), RiskBand.MEDIUM, "Rule score crossed review threshold."),
                List.of(new RuleHit(
                        "HIGH_VALUE_AMOUNT",
                        "High value amount",
                        RiskBand.MEDIUM,
                        new BigDecimal("0.25"),
                        "The amount exceeded the high-value review threshold.",
                        Map.of("threshold", "10000.00"))),
                List.of(),
                OffsetDateTime.parse("2026-09-11T09:00:00Z"),
                Map.of());
    }
}
