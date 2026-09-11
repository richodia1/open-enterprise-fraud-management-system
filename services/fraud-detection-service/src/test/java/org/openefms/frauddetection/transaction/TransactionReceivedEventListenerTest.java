package org.openefms.frauddetection.transaction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import org.openefms.common.events.rabbitmq.RabbitMqExchanges;
import org.openefms.common.events.rabbitmq.RabbitMqRoutingKeys;
import org.openefms.common.events.transaction.TransactionReceivedEvent;
import org.openefms.common.models.fraud.DecisionOutcome;
import org.openefms.common.models.transaction.TransactionAmount;
import org.openefms.common.models.transaction.TransactionChannel;
import org.openefms.common.models.transaction.TransactionDirection;
import org.openefms.common.models.transaction.TransactionIngestionRequest;
import org.openefms.common.models.transaction.TransactionParty;
import org.openefms.common.models.transaction.TransactionType;
import org.openefms.frauddetection.decision.FraudDecisionMessage;
import org.openefms.frauddetection.decision.FraudDecisionMessageSink;
import org.openefms.frauddetection.decision.FraudDecisionOrchestrator;
import org.openefms.frauddetection.decision.FraudDecisionProperties;
import org.openefms.frauddetection.decision.RuleBasedFraudDecisionEngine;
import org.openefms.frauddetection.messaging.FraudRabbitMqProperties;

class TransactionReceivedEventListenerTest {
    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-09-11T09:00:00Z"), ZoneOffset.UTC);
    private static final UUID TRANSACTION_ID = UUID.fromString("118f3bd4-b208-44ff-b1ff-447c78d80d02");
    private static final UUID SOURCE_EVENT_ID = UUID.fromString("7e37875e-7443-47d4-931c-df8ce0f10cab");

    @Test
    void consumesTransactionEventAndHandsDecisionMessageToSink() {
        CapturingFraudDecisionMessageSink sink = new CapturingFraudDecisionMessageSink();
        TransactionReceivedEventListener listener = new TransactionReceivedEventListener(rulesOrchestrator(), sink);

        listener.handle(transactionEvent(EventTypes.TRANSACTION_RECEIVED, 1, new BigDecimal("25.00"), Map.of()));

        assertEquals(1, sink.messages.size());
        FraudDecisionMessage message = sink.messages.getFirst();
        assertEquals(RabbitMqRoutingKeys.FRAUD_DECISION_PRODUCED, message.routingKey());
        assertEquals(EventTypes.FRAUD_DECISION_PRODUCED, message.envelope().metadata().eventType());
        assertEquals("correlation-1", message.envelope().metadata().correlationId());
        assertEquals(SOURCE_EVENT_ID.toString(), message.envelope().metadata().causationId());
        assertEquals(DecisionOutcome.APPROVE, message.envelope().payload().decision().outcome());
        assertEquals(TRANSACTION_ID, message.envelope().payload().decision().transactionId());
    }

    @Test
    void rejectsEventsWithUnexpectedType() {
        CapturingFraudDecisionMessageSink sink = new CapturingFraudDecisionMessageSink();
        TransactionReceivedEventListener listener = new TransactionReceivedEventListener(rulesOrchestrator(), sink);

        InvalidTransactionReceivedEventException exception = assertThrows(
                InvalidTransactionReceivedEventException.class,
                () -> listener.handle(transactionEvent(
                        EventTypes.FRAUD_DECISION_PRODUCED,
                        1,
                        new BigDecimal("25.00"),
                        Map.of())));

        assertTrue(exception.getMessage().contains("eventType must be transaction.received"));
        assertTrue(sink.messages.isEmpty());
    }

    @Test
    void rejectsEventsWithUnsupportedVersion() {
        CapturingFraudDecisionMessageSink sink = new CapturingFraudDecisionMessageSink();
        TransactionReceivedEventListener listener = new TransactionReceivedEventListener(rulesOrchestrator(), sink);

        InvalidTransactionReceivedEventException exception = assertThrows(
                InvalidTransactionReceivedEventException.class,
                () -> listener.handle(transactionEvent(
                        EventTypes.TRANSACTION_RECEIVED,
                        2,
                        new BigDecimal("25.00"),
                        Map.of())));

        assertTrue(exception.getMessage().contains("eventVersion must be 1"));
        assertTrue(sink.messages.isEmpty());
    }

    @Test
    void rejectsNullEvent() {
        CapturingFraudDecisionMessageSink sink = new CapturingFraudDecisionMessageSink();
        TransactionReceivedEventListener listener = new TransactionReceivedEventListener(rulesOrchestrator(), sink);

        InvalidTransactionReceivedEventException exception = assertThrows(
                InvalidTransactionReceivedEventException.class,
                () -> listener.handle(null));

        assertNotNull(exception.getMessage());
        assertTrue(sink.messages.isEmpty());
    }

    private FraudDecisionOrchestrator rulesOrchestrator() {
        return new FraudDecisionOrchestrator(
                new FraudDecisionProperties("rules", null, null),
                new FraudRabbitMqProperties(RabbitMqExchanges.DOMAIN_EVENTS, RabbitMqExchanges.DEAD_LETTER),
                new RuleBasedFraudDecisionEngine(),
                FIXED_CLOCK);
    }

    private EventEnvelope<TransactionReceivedEvent> transactionEvent(
            String eventType,
            int eventVersion,
            BigDecimal amount,
            Map<String, String> attributes) {
        return new EventEnvelope<>(
                new EventMetadata(
                        SOURCE_EVENT_ID,
                        eventType,
                        eventVersion,
                        OffsetDateTime.parse("2026-09-11T08:59:30Z"),
                        "ingestion-management-service",
                        "tenant-a",
                        "correlation-1",
                        null,
                        "trace-1",
                        Map.of()),
                new TransactionReceivedEvent(transaction(amount, attributes)));
    }

    private TransactionIngestionRequest transaction(BigDecimal amount, Map<String, String> attributes) {
        return new TransactionIngestionRequest(
                TRANSACTION_ID,
                "EXT-001",
                "tenant-a",
                TransactionType.TRANSFER,
                TransactionChannel.MOBILE,
                TransactionDirection.OUTBOUND,
                new TransactionAmount(amount, "GBP"),
                party("originator"),
                party("beneficiary"),
                OffsetDateTime.parse("2026-09-11T08:58:00Z"),
                attributes);
    }

    private TransactionParty party(String reference) {
        return new TransactionParty(reference, reference, reference, reference + "-hash", "BANK", "GB", Map.of());
    }

    private static final class CapturingFraudDecisionMessageSink implements FraudDecisionMessageSink {
        private final List<FraudDecisionMessage> messages = new ArrayList<>();

        @Override
        public void accept(FraudDecisionMessage message) {
            messages.add(message);
        }
    }
}
