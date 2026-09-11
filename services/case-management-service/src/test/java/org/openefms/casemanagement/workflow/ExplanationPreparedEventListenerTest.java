package org.openefms.casemanagement.workflow;

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
import org.openefms.casemanagement.messaging.CaseManagementRabbitMqProperties;
import org.openefms.common.events.EventEnvelope;
import org.openefms.common.events.EventMetadata;
import org.openefms.common.events.EventTypes;
import org.openefms.common.events.explanation.ExplanationPreparedEvent;
import org.openefms.common.events.rabbitmq.RabbitMqExchanges;
import org.openefms.common.models.explanation.EvidenceItem;
import org.openefms.common.models.explanation.ExplanationResponse;

class ExplanationPreparedEventListenerTest {
    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-09-11T11:00:00Z"), ZoneOffset.UTC);
    private static final UUID SOURCE_EVENT_ID = UUID.fromString("a638bf8a-2c9c-4e43-aae3-caf77d8497ac");

    @Test
    void consumesExplanationEventAndPublishesWorkflowMessages() {
        CapturingDomainEventMessageSink sink = new CapturingDomainEventMessageSink();
        ExplanationPreparedEventListener listener = new ExplanationPreparedEventListener(orchestrator(), sink);

        listener.handle(explanationEvent(EventTypes.EXPLANATION_PREPARED, 1));

        assertEquals(4, sink.messages.size());
    }

    @Test
    void rejectsEventsWithUnexpectedType() {
        CapturingDomainEventMessageSink sink = new CapturingDomainEventMessageSink();
        ExplanationPreparedEventListener listener = new ExplanationPreparedEventListener(orchestrator(), sink);

        InvalidExplanationPreparedEventException exception = assertThrows(
                InvalidExplanationPreparedEventException.class,
                () -> listener.handle(explanationEvent(EventTypes.FRAUD_DECISION_PRODUCED, 1)));

        assertTrue(exception.getMessage().contains("eventType must be explanation.prepared"));
        assertTrue(sink.messages.isEmpty());
    }

    @Test
    void rejectsEventsWithUnsupportedVersion() {
        CapturingDomainEventMessageSink sink = new CapturingDomainEventMessageSink();
        ExplanationPreparedEventListener listener = new ExplanationPreparedEventListener(orchestrator(), sink);

        InvalidExplanationPreparedEventException exception = assertThrows(
                InvalidExplanationPreparedEventException.class,
                () -> listener.handle(explanationEvent(EventTypes.EXPLANATION_PREPARED, 2)));

        assertTrue(exception.getMessage().contains("eventVersion must be 1"));
        assertTrue(sink.messages.isEmpty());
    }

    private CaseWorkflowOrchestrator orchestrator() {
        return new CaseWorkflowOrchestrator(
                new CaseManagementRabbitMqProperties(RabbitMqExchanges.DOMAIN_EVENTS, RabbitMqExchanges.DEAD_LETTER),
                FIXED_CLOCK);
    }

    private EventEnvelope<ExplanationPreparedEvent> explanationEvent(String eventType, int eventVersion) {
        return new EventEnvelope<>(
                new EventMetadata(
                        SOURCE_EVENT_ID,
                        eventType,
                        eventVersion,
                        OffsetDateTime.parse("2026-09-11T10:00:01Z"),
                        "explainable-ai-service",
                        "tenant-a",
                        "correlation-1",
                        null,
                        "trace-1",
                        Map.of()),
                new ExplanationPreparedEvent(explanation()));
    }

    private ExplanationResponse explanation() {
        return new ExplanationResponse(
                UUID.fromString("d88154d9-5246-43ec-bb65-fcdfd38f64a3"),
                UUID.fromString("325dc0f5-775d-4858-89be-1992e52fe2f0"),
                UUID.fromString("118f3bd4-b208-44ff-b1ff-447c78d80d02"),
                "tenant-a",
                "Decision REVIEW with MEDIUM risk score 0.60. Key evidence: High value amount.",
                List.of(new EvidenceItem(
                        "RULE",
                        "High value amount",
                        "The amount exceeded the high-value review threshold.",
                        new BigDecimal("0.25"),
                        Map.of("ruleCode", "HIGH_VALUE_AMOUNT"))),
                OffsetDateTime.parse("2026-09-11T10:00:00Z"),
                Map.of(
                        "decisionOutcome", "REVIEW",
                        "riskBand", "MEDIUM",
                        "riskScore", "0.60"));
    }

    private static final class CapturingDomainEventMessageSink implements DomainEventMessageSink {
        private final List<DomainEventMessage<?>> messages = new ArrayList<>();

        @Override
        public void accept(DomainEventMessage<?> message) {
            messages.add(message);
        }
    }
}
