package org.openefms.explainableai.explanation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
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
import org.openefms.common.models.fraud.DecisionMode;
import org.openefms.common.models.fraud.DecisionOutcome;
import org.openefms.common.models.fraud.FraudDecisionResponse;
import org.openefms.common.models.fraud.RiskBand;
import org.openefms.common.models.fraud.RiskScore;
import org.openefms.explainableai.messaging.ExplainableAiRabbitMqProperties;

class FraudDecisionProducedEventListenerTest {
    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-09-11T10:00:00Z"), ZoneOffset.UTC);
    private static final UUID SOURCE_EVENT_ID = UUID.fromString("fb40ff28-86cc-4b6c-a6df-b538d5155d2e");

    @Test
    void consumesFraudDecisionEventAndHandsExplanationMessageToSink() {
        CapturingExplanationMessageSink sink = new CapturingExplanationMessageSink();
        FraudDecisionProducedEventListener listener = new FraudDecisionProducedEventListener(orchestrator(), sink);

        listener.handle(fraudDecisionEvent(EventTypes.FRAUD_DECISION_PRODUCED, 1));

        assertEquals(1, sink.messages.size());
        ExplanationMessage message = sink.messages.getFirst();
        assertEquals(RabbitMqRoutingKeys.EXPLANATION_PREPARED, message.routingKey());
        assertEquals(EventTypes.EXPLANATION_PREPARED, message.envelope().metadata().eventType());
        assertEquals("correlation-1", message.envelope().metadata().correlationId());
        assertEquals(SOURCE_EVENT_ID.toString(), message.envelope().metadata().causationId());
    }

    @Test
    void rejectsEventsWithUnexpectedType() {
        CapturingExplanationMessageSink sink = new CapturingExplanationMessageSink();
        FraudDecisionProducedEventListener listener = new FraudDecisionProducedEventListener(orchestrator(), sink);

        InvalidFraudDecisionProducedEventException exception = assertThrows(
                InvalidFraudDecisionProducedEventException.class,
                () -> listener.handle(fraudDecisionEvent(EventTypes.TRANSACTION_RECEIVED, 1)));

        assertTrue(exception.getMessage().contains("eventType must be fraud.decision.produced"));
        assertTrue(sink.messages.isEmpty());
    }

    @Test
    void rejectsEventsWithUnsupportedVersion() {
        CapturingExplanationMessageSink sink = new CapturingExplanationMessageSink();
        FraudDecisionProducedEventListener listener = new FraudDecisionProducedEventListener(orchestrator(), sink);

        InvalidFraudDecisionProducedEventException exception = assertThrows(
                InvalidFraudDecisionProducedEventException.class,
                () -> listener.handle(fraudDecisionEvent(EventTypes.FRAUD_DECISION_PRODUCED, 2)));

        assertTrue(exception.getMessage().contains("eventVersion must be 1"));
        assertTrue(sink.messages.isEmpty());
    }

    private ExplanationOrchestrator orchestrator() {
        return new ExplanationOrchestrator(
                new DecisionSupportExplanationService(),
                new ExplainableAiRabbitMqProperties(RabbitMqExchanges.DOMAIN_EVENTS, RabbitMqExchanges.DEAD_LETTER),
                FIXED_CLOCK);
    }

    private EventEnvelope<FraudDecisionProducedEvent> fraudDecisionEvent(String eventType, int eventVersion) {
        return new EventEnvelope<>(
                new EventMetadata(
                        SOURCE_EVENT_ID,
                        eventType,
                        eventVersion,
                        OffsetDateTime.parse("2026-09-11T09:00:01Z"),
                        "fraud-detection-service",
                        "tenant-a",
                        "correlation-1",
                        "7e37875e-7443-47d4-931c-df8ce0f10cab",
                        "trace-1",
                        Map.of()),
                new FraudDecisionProducedEvent(decision()));
    }

    private FraudDecisionResponse decision() {
        return new FraudDecisionResponse(
                UUID.fromString("325dc0f5-775d-4858-89be-1992e52fe2f0"),
                UUID.fromString("118f3bd4-b208-44ff-b1ff-447c78d80d02"),
                "tenant-a",
                DecisionMode.RULES,
                DecisionOutcome.APPROVE,
                new RiskScore(new BigDecimal("0.00"), RiskBand.LOW, "No rule score."),
                List.of(),
                List.of(),
                OffsetDateTime.parse("2026-09-11T09:00:00Z"),
                Map.of());
    }

    private static final class CapturingExplanationMessageSink implements ExplanationMessageSink {
        private final List<ExplanationMessage> messages = new ArrayList<>();

        @Override
        public void accept(ExplanationMessage message) {
            messages.add(message);
        }
    }
}
