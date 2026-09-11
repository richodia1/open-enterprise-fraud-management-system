package org.openefms.frauddetection.decision;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import org.openefms.common.events.rabbitmq.RabbitMqExchanges;
import org.openefms.common.events.rabbitmq.RabbitMqRoutingKeys;
import org.openefms.common.events.transaction.TransactionReceivedEvent;
import org.openefms.common.models.fraud.DecisionMode;
import org.openefms.common.models.fraud.DecisionOutcome;
import org.openefms.common.models.fraud.RiskBand;
import org.openefms.common.models.fraud.RuleHit;
import org.openefms.common.models.transaction.TransactionAmount;
import org.openefms.common.models.transaction.TransactionChannel;
import org.openefms.common.models.transaction.TransactionDirection;
import org.openefms.common.models.transaction.TransactionIngestionRequest;
import org.openefms.common.models.transaction.TransactionParty;
import org.openefms.common.models.transaction.TransactionType;
import org.openefms.frauddetection.messaging.FraudRabbitMqProperties;

class FraudDecisionOrchestratorTest {
    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-09-08T09:00:00Z"), ZoneOffset.UTC);
    private static final UUID TRANSACTION_ID = UUID.fromString("118f3bd4-b208-44ff-b1ff-447c78d80d02");
    private static final UUID SOURCE_EVENT_ID = UUID.fromString("7e37875e-7443-47d4-931c-df8ce0f10cab");

    @Test
    void approvesLowRiskTransactionsInRulesMode() {
        FraudDecisionOrchestrator orchestrator = rulesOrchestrator();

        var response = orchestrator.decide(transaction(
                new BigDecimal("25.00"),
                TransactionChannel.MOBILE,
                "GB",
                "GB",
                Map.of()));

        assertEquals(DecisionMode.RULES, response.mode());
        assertEquals(DecisionOutcome.APPROVE, response.outcome());
        assertEquals(new BigDecimal("0.00"), response.riskScore().value());
        assertEquals(RiskBand.LOW, response.riskScore().band());
        assertTrue(response.ruleHits().isEmpty());
        assertTrue(response.modelSignals().isEmpty());
        assertEquals(OffsetDateTime.parse("2026-09-08T09:00:00Z"), response.decidedAt());
    }

    @Test
    void escalatesTransactionsWithMultipleBaselineRuleHits() {
        FraudDecisionOrchestrator orchestrator = rulesOrchestrator();

        var response = orchestrator.decide(transaction(
                new BigDecimal("200000.00"),
                TransactionChannel.OTHER,
                "GB",
                "NG",
                Map.of("velocityCount", "12", "highRisk", "true")));

        assertEquals(DecisionOutcome.ESCALATE, response.outcome());
        assertEquals(new BigDecimal("1.00"), response.riskScore().value());
        assertEquals(RiskBand.CRITICAL, response.riskScore().band());
        assertEquals(5, response.ruleHits().size());
        assertTrue(ruleCodes(response.ruleHits()).contains("CRITICAL_VALUE_AMOUNT"));
        assertTrue(ruleCodes(response.ruleHits()).contains("VELOCITY_BLOCK_THRESHOLD"));
        assertTrue(ruleCodes(response.ruleHits()).contains("CROSS_BORDER_PARTIES"));
        assertTrue(ruleCodes(response.ruleHits()).contains("HIGH_RISK_HINT"));
        assertTrue(ruleCodes(response.ruleHits()).contains("UNKNOWN_TRANSACTION_CHANNEL"));
    }

    @Test
    void rejectsMlModeUntilModelServingIsImplemented() {
        FraudDecisionOrchestrator orchestrator = new FraudDecisionOrchestrator(
                new FraudDecisionProperties("ml", null, null),
                rabbitMqProperties(),
                new RuleBasedFraudDecisionEngine(),
                FIXED_CLOCK);

        UnsupportedFraudDecisionModeException exception = assertThrows(
                UnsupportedFraudDecisionModeException.class,
                () -> orchestrator.decide(transaction(
                        new BigDecimal("25.00"),
                        TransactionChannel.MOBILE,
                        "GB",
                        "GB",
                        Map.of())));

        assertEquals(DecisionMode.ML, exception.mode());
    }

    @Test
    void createsTransportReadyFraudDecisionMessagesFromTransactionEvents() {
        FraudDecisionOrchestrator orchestrator = rulesOrchestrator();
        EventEnvelope<TransactionReceivedEvent> sourceEvent = new EventEnvelope<>(
                new EventMetadata(
                        SOURCE_EVENT_ID,
                        EventTypes.TRANSACTION_RECEIVED,
                        1,
                        OffsetDateTime.parse("2026-09-08T08:59:30Z"),
                        "ingestion-management-service",
                        "tenant-a",
                        "correlation-1",
                        null,
                        "trace-1",
                        Map.of()),
                new TransactionReceivedEvent(transaction(
                        new BigDecimal("25.00"),
                        TransactionChannel.MOBILE,
                        "GB",
                        "GB",
                        Map.of())));

        FraudDecisionMessage message = orchestrator.decide(sourceEvent);

        assertEquals(RabbitMqExchanges.DOMAIN_EVENTS, message.exchangeName());
        assertEquals(RabbitMqRoutingKeys.FRAUD_DECISION_PRODUCED, message.routingKey());
        assertEquals(EventTypes.FRAUD_DECISION_PRODUCED, message.envelope().metadata().eventType());
        assertEquals("fraud-detection-service", message.envelope().metadata().sourceService());
        assertEquals("correlation-1", message.envelope().metadata().correlationId());
        assertEquals(SOURCE_EVENT_ID.toString(), message.envelope().metadata().causationId());
        assertEquals(DecisionOutcome.APPROVE, message.envelope().payload().decision().outcome());
    }

    private FraudDecisionOrchestrator rulesOrchestrator() {
        return new FraudDecisionOrchestrator(
                new FraudDecisionProperties("rules", null, null),
                rabbitMqProperties(),
                new RuleBasedFraudDecisionEngine(),
                FIXED_CLOCK);
    }

    private FraudRabbitMqProperties rabbitMqProperties() {
        return new FraudRabbitMqProperties(RabbitMqExchanges.DOMAIN_EVENTS, RabbitMqExchanges.DEAD_LETTER);
    }

    private TransactionIngestionRequest transaction(
            BigDecimal amount,
            TransactionChannel channel,
            String originatorCountry,
            String beneficiaryCountry,
            Map<String, String> attributes) {
        return new TransactionIngestionRequest(
                TRANSACTION_ID,
                "EXT-001",
                "tenant-a",
                TransactionType.TRANSFER,
                channel,
                TransactionDirection.OUTBOUND,
                new TransactionAmount(amount, "GBP"),
                party("originator", originatorCountry),
                party("beneficiary", beneficiaryCountry),
                OffsetDateTime.parse("2026-09-08T08:58:00Z"),
                attributes);
    }

    private TransactionParty party(String reference, String countryCode) {
        return new TransactionParty(reference, reference, reference, reference + "-hash", "BANK", countryCode, Map.of());
    }

    private List<String> ruleCodes(List<RuleHit> ruleHits) {
        return ruleHits.stream().map(RuleHit::ruleCode).toList();
    }
}
