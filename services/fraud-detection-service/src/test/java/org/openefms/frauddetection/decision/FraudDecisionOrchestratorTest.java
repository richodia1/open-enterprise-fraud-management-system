package org.openefms.frauddetection.decision;

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
import java.util.Optional;
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
import org.openefms.common.models.ml.MlFeatureContribution;
import org.openefms.common.models.ml.MlRuntimeState;
import org.openefms.common.models.ml.MlRuntimeStatus;
import org.openefms.common.models.ml.MlScoreRequest;
import org.openefms.common.models.ml.MlScoreResponse;
import org.openefms.common.models.transaction.TransactionAmount;
import org.openefms.common.models.transaction.TransactionChannel;
import org.openefms.common.models.transaction.TransactionDirection;
import org.openefms.common.models.transaction.TransactionIngestionRequest;
import org.openefms.common.models.transaction.TransactionParty;
import org.openefms.common.models.transaction.TransactionType;
import org.openefms.frauddetection.ml.MlScoringClient;
import org.openefms.frauddetection.ml.MlScoringUnavailableException;
import org.openefms.frauddetection.messaging.FraudRabbitMqProperties;

class FraudDecisionOrchestratorTest {
    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-09-08T09:00:00Z"), ZoneOffset.UTC);
    private static final UUID TRANSACTION_ID = UUID.fromString("118f3bd4-b208-44ff-b1ff-447c78d80d02");
    private static final UUID SOURCE_EVENT_ID = UUID.fromString("7e37875e-7443-47d4-931c-df8ce0f10cab");

    @Test
    void approvesLowRiskTransactionsInRulesMode() {
        CapturingFraudDecisionRepository repository = new CapturingFraudDecisionRepository();
        FraudDecisionOrchestrator orchestrator = rulesOrchestrator(repository);

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

        assertEquals(1, repository.records.size());
        FraudDecisionRecord record = repository.records.getFirst();
        assertEquals(response.decisionId(), record.decisionId());
        assertEquals(TRANSACTION_ID, record.transactionId());
        assertEquals(DecisionMode.RULES, record.mode());
        assertEquals(DecisionOutcome.APPROVE, record.outcome());
        assertEquals("No rule hits.", record.ruleEvidenceSummary());
        assertEquals("No model signals.", record.modelSignalSummary());
        assertEquals(null, record.eventType());
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
    void rejectsMlModeWhenNoScoringRuntimeIsAvailable() {
        FraudDecisionOrchestrator orchestrator = new FraudDecisionOrchestrator(
                new FraudDecisionProperties("ml", null, null),
                rabbitMqProperties(),
                new RuleBasedFraudDecisionEngine(),
                unavailableMlScoringClient(),
                new CapturingFraudDecisionRepository(),
                new FraudDecisionRecordMapper(),
                FIXED_CLOCK);

        MlScoringUnavailableException exception = assertThrows(
                MlScoringUnavailableException.class,
                () -> orchestrator.decide(transaction(
                        new BigDecimal("25.00"),
                        TransactionChannel.MOBILE,
                        "GB",
                        "GB",
                        Map.of())));

        assertEquals("disabled", exception.provider());
        assertEquals("xgboost-baseline", exception.modelName());
    }

    @Test
    void usesMlScoringContractWhenRuntimeClientIsAvailable() {
        CapturingMlScoringClient scoringClient = new CapturingMlScoringClient(new MlScoreResponse(
                "xgboost-baseline",
                "2026.09.11",
                new BigDecimal("0.62"),
                RiskBand.MEDIUM,
                DecisionOutcome.REVIEW,
                List.of(new MlFeatureContribution(
                        "amount",
                        "25000.00",
                        new BigDecimal("0.31"),
                        "Amount contributed to the model score.")),
                OffsetDateTime.parse("2026-09-08T08:59:55Z"),
                Map.of("dataset", "synthetic-baseline")));
        FraudDecisionOrchestrator orchestrator = new FraudDecisionOrchestrator(
                new FraudDecisionProperties("ml", null, null),
                rabbitMqProperties(),
                new RuleBasedFraudDecisionEngine(),
                scoringClient,
                new CapturingFraudDecisionRepository(),
                new FraudDecisionRecordMapper(),
                FIXED_CLOCK);

        var response = orchestrator.decide(transaction(
                new BigDecimal("25000.00"),
                TransactionChannel.MOBILE,
                "GB",
                "GB",
                Map.of()));

        assertEquals(DecisionMode.ML, response.mode());
        assertEquals(DecisionOutcome.REVIEW, response.outcome());
        assertEquals(new BigDecimal("0.62"), response.riskScore().value());
        assertEquals(RiskBand.MEDIUM, response.riskScore().band());
        assertTrue(response.ruleHits().isEmpty());
        assertEquals(1, response.modelSignals().size());
        assertEquals("xgboost-baseline", response.modelSignals().getFirst().modelName());
        assertEquals("2026.09.11", response.modelSignals().getFirst().modelVersion());
        assertEquals("25000.00", scoringClient.lastRequest().features().get("amount"));
        assertEquals("GBP", scoringClient.lastRequest().features().get("currency"));
        assertEquals("synthetic-baseline", response.attributes().get("dataset"));
    }

    @Test
    void combinesRulesAndMlSignalsInHybridMode() {
        CapturingMlScoringClient scoringClient = new CapturingMlScoringClient(new MlScoreResponse(
                "xgboost-baseline",
                "2026.09.11",
                new BigDecimal("0.82"),
                RiskBand.HIGH,
                DecisionOutcome.BLOCK,
                List.of(new MlFeatureContribution(
                        "beneficiaryCountryCode",
                        "NG",
                        new BigDecimal("0.44"),
                        "Destination country contributed to the model score.")),
                OffsetDateTime.parse("2026-09-08T08:59:55Z"),
                Map.of()));
        FraudDecisionOrchestrator orchestrator = new FraudDecisionOrchestrator(
                new FraudDecisionProperties("hybrid", null, null),
                rabbitMqProperties(),
                new RuleBasedFraudDecisionEngine(),
                scoringClient,
                new CapturingFraudDecisionRepository(),
                new FraudDecisionRecordMapper(),
                FIXED_CLOCK);

        var response = orchestrator.decide(transaction(
                new BigDecimal("25000.00"),
                TransactionChannel.MOBILE,
                "GB",
                "NG",
                Map.of()));

        assertEquals(DecisionMode.HYBRID, response.mode());
        assertEquals(DecisionOutcome.BLOCK, response.outcome());
        assertEquals(new BigDecimal("0.82"), response.riskScore().value());
        assertEquals(RiskBand.HIGH, response.riskScore().band());
        assertTrue(ruleCodes(response.ruleHits()).contains("HIGH_VALUE_AMOUNT"));
        assertTrue(ruleCodes(response.ruleHits()).contains("CROSS_BORDER_PARTIES"));
        assertEquals(1, response.modelSignals().size());
        assertEquals("max-rule-or-ml-score", response.attributes().get("decisionStrategy"));
        assertEquals("0.50", response.attributes().get("ruleScore"));
        assertEquals("0.82", response.attributes().get("mlScore"));
    }

    @Test
    void createsTransportReadyFraudDecisionMessagesFromTransactionEvents() {
        CapturingFraudDecisionRepository repository = new CapturingFraudDecisionRepository();
        FraudDecisionOrchestrator orchestrator = rulesOrchestrator(repository);
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

        assertEquals(1, repository.records.size());
        FraudDecisionRecord record = repository.records.getFirst();
        assertEquals(message.envelope().payload().decision().decisionId(), record.decisionId());
        assertEquals(EventTypes.FRAUD_DECISION_PRODUCED, record.eventType());
        assertEquals(message.envelope().metadata().eventId(), record.eventId());
        assertEquals("correlation-1", record.correlationId());
        assertEquals(SOURCE_EVENT_ID.toString(), record.causationId());
    }

    @Test
    void readsPersistedFraudDecisionRecords() {
        CapturingFraudDecisionRepository repository = new CapturingFraudDecisionRepository();
        FraudDecisionOrchestrator orchestrator = rulesOrchestrator(repository);

        var response = orchestrator.decide(transaction(
                new BigDecimal("200000.00"),
                TransactionChannel.OTHER,
                "GB",
                "NG",
                Map.of("velocityCount", "12", "highRisk", "true")));

        FraudDecisionRecord record = orchestrator.decision(response.decisionId());
        List<FraudDecisionRecord> byTransaction = orchestrator.decisionsForTransaction(TRANSACTION_ID);
        List<FraudDecisionRecord> byTenant = orchestrator.decisionsForTenant(" tenant-a ");

        assertEquals(DecisionOutcome.ESCALATE, record.outcome());
        assertEquals(RiskBand.CRITICAL, record.riskBand());
        assertEquals(5, record.ruleHitCount());
        assertTrue(record.ruleEvidenceSummary().contains("CRITICAL_VALUE_AMOUNT"));
        assertEquals(1, byTransaction.size());
        assertEquals(1, byTenant.size());
    }

    @Test
    void reportsMissingDecisionRecords() {
        FraudDecisionOrchestrator orchestrator = rulesOrchestrator();

        assertThrows(
                FraudDecisionRecordNotFoundException.class,
                () -> orchestrator.decision(UUID.fromString("25a499ee-c908-44de-817f-fcbe090d6469")));
    }

    private FraudDecisionOrchestrator rulesOrchestrator() {
        return rulesOrchestrator(new CapturingFraudDecisionRepository());
    }

    private FraudDecisionOrchestrator rulesOrchestrator(FraudDecisionRepository repository) {
        return new FraudDecisionOrchestrator(
                new FraudDecisionProperties("rules", null, null),
                rabbitMqProperties(),
                new RuleBasedFraudDecisionEngine(),
                unavailableMlScoringClient(),
                repository,
                new FraudDecisionRecordMapper(),
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

    private MlScoringClient unavailableMlScoringClient() {
        return new MlScoringClient() {
            @Override
            public MlRuntimeStatus status() {
                return new MlRuntimeStatus(
                        "disabled",
                        "xgboost-baseline",
                        MlRuntimeState.DISABLED,
                        List.of(DecisionMode.ML, DecisionMode.HYBRID),
                        "ML scoring is disabled by configuration.",
                        OffsetDateTime.now(FIXED_CLOCK),
                        Map.of());
            }

            @Override
            public MlScoreResponse score(MlScoreRequest request) {
                throw new MlScoringUnavailableException(
                        "disabled",
                        "xgboost-baseline",
                        "ML scoring is disabled by configuration.");
            }
        };
    }

    private static final class CapturingMlScoringClient implements MlScoringClient {
        private final MlScoreResponse response;
        private MlScoreRequest lastRequest;

        private CapturingMlScoringClient(MlScoreResponse response) {
            this.response = response;
        }

        @Override
        public MlRuntimeStatus status() {
            return new MlRuntimeStatus(
                    response.modelName(),
                    response.modelName(),
                    MlRuntimeState.READY,
                    List.of(DecisionMode.ML, DecisionMode.HYBRID),
                    "Test scorer is ready.",
                    OffsetDateTime.now(FIXED_CLOCK),
                    Map.of());
        }

        @Override
        public MlScoreResponse score(MlScoreRequest request) {
            this.lastRequest = request;
            return response;
        }

        private MlScoreRequest lastRequest() {
            return lastRequest;
        }
    }

    private static final class CapturingFraudDecisionRepository implements FraudDecisionRepository {
        private final Map<UUID, FraudDecisionRecord> recordsById = new java.util.LinkedHashMap<>();
        private final List<FraudDecisionRecord> records = new ArrayList<>();

        @Override
        public FraudDecisionRecord save(FraudDecisionRecord record) {
            records.add(record);
            recordsById.put(record.decisionId(), record);
            return record;
        }

        @Override
        public Optional<FraudDecisionRecord> findByDecisionId(UUID decisionId) {
            return Optional.ofNullable(recordsById.get(decisionId));
        }

        @Override
        public List<FraudDecisionRecord> findByTransactionId(UUID transactionId) {
            return records.stream()
                    .filter(record -> record.transactionId().equals(transactionId))
                    .toList();
        }

        @Override
        public List<FraudDecisionRecord> findByTenantId(String tenantId) {
            return records.stream()
                    .filter(record -> record.tenantId().equals(tenantId))
                    .toList();
        }
    }
}
