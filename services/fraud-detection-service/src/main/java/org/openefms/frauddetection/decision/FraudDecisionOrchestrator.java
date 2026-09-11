package org.openefms.frauddetection.decision;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
import org.openefms.common.events.fraud.FraudDecisionProducedEvent;
import org.openefms.common.events.rabbitmq.RabbitMqTopology;
import org.openefms.common.events.transaction.TransactionReceivedEvent;
import org.openefms.common.models.fraud.DecisionMode;
import org.openefms.common.models.fraud.DecisionOutcome;
import org.openefms.common.models.fraud.FraudDecisionResponse;
import org.openefms.common.models.fraud.ModelSignal;
import org.openefms.common.models.fraud.RiskBand;
import org.openefms.common.models.fraud.RiskScore;
import org.openefms.common.models.ml.MlFeatureContribution;
import org.openefms.common.models.ml.MlRuntimeStatus;
import org.openefms.common.models.ml.MlScoreRequest;
import org.openefms.common.models.ml.MlScoreResponse;
import org.openefms.common.models.transaction.TransactionIngestionRequest;
import org.openefms.common.models.transaction.TransactionParty;
import org.openefms.frauddetection.ml.MlScoringClient;
import org.openefms.frauddetection.messaging.FraudRabbitMqProperties;
import org.springframework.stereotype.Service;

@Service
public class FraudDecisionOrchestrator {
    private static final int FRAUD_DECISION_EVENT_VERSION = 1;
    private static final String SOURCE_SERVICE = "fraud-detection-service";
    private static final BigDecimal ONE = BigDecimal.ONE.setScale(2, RoundingMode.HALF_UP);

    private final FraudDecisionProperties properties;
    private final FraudRabbitMqProperties rabbitMqProperties;
    private final RuleBasedFraudDecisionEngine ruleBasedEngine;
    private final MlScoringClient mlScoringClient;
    private final Clock clock;

    public FraudDecisionOrchestrator(
            FraudDecisionProperties properties,
            FraudRabbitMqProperties rabbitMqProperties,
            RuleBasedFraudDecisionEngine ruleBasedEngine,
            MlScoringClient mlScoringClient,
            Clock clock) {
        this.properties = properties;
        this.rabbitMqProperties = rabbitMqProperties;
        this.ruleBasedEngine = ruleBasedEngine;
        this.mlScoringClient = mlScoringClient;
        this.clock = clock;
    }

    public FraudDecisionResponse decide(TransactionIngestionRequest transaction) {
        Objects.requireNonNull(transaction, "transaction must not be null");
        DecisionMode activeMode = properties.activeMode();
        return switch (activeMode) {
            case RULES -> rulesDecision(transaction);
            case ML -> mlDecision(transaction);
            case HYBRID -> hybridDecision(transaction);
        };
    }

    public MlRuntimeStatus mlRuntimeStatus() {
        return mlScoringClient.status();
    }

    public FraudDecisionMessage decide(EventEnvelope<TransactionReceivedEvent> transactionEvent) {
        Objects.requireNonNull(transactionEvent, "transactionEvent must not be null");
        FraudDecisionResponse decision = decide(transactionEvent.payload().transaction());
        String routingKey = RabbitMqTopology.routingKeyFor(EventTypes.FRAUD_DECISION_PRODUCED);
        EventEnvelope<FraudDecisionProducedEvent> envelope = new EventEnvelope<>(
                decisionMetadata(transactionEvent.metadata()),
                new FraudDecisionProducedEvent(decision));

        return new FraudDecisionMessage(rabbitMqProperties.exchange(), routingKey, envelope);
    }

    private FraudDecisionResponse rulesDecision(TransactionIngestionRequest transaction) {
        return ruleBasedEngine.decide(transaction, properties.rules(), properties.ml(), clock);
    }

    private FraudDecisionResponse mlDecision(TransactionIngestionRequest transaction) {
        MlScoreResponse mlScore = mlScoringClient.score(mlRequest(transaction));
        BigDecimal score = normaliseScore(mlScore.score());

        return new FraudDecisionResponse(
                UUID.randomUUID(),
                transaction.transactionId(),
                transaction.tenantId(),
                DecisionMode.ML,
                mlScore.recommendedOutcome(),
                new RiskScore(
                        score,
                        mlScore.riskBand(),
                        "ML score from model " + mlScore.modelName() + " recommended " + mlScore.recommendedOutcome() + "."),
                List.of(),
                List.of(modelSignal(mlScore, score)),
                OffsetDateTime.now(clock),
                mlDecisionAttributes(mlScore, "ml-only"));
    }

    private FraudDecisionResponse hybridDecision(TransactionIngestionRequest transaction) {
        FraudDecisionResponse rulesDecision = rulesDecision(transaction);
        MlScoreResponse mlScore = mlScoringClient.score(mlRequest(transaction));
        BigDecimal ruleScore = normaliseScore(rulesDecision.riskScore().value());
        BigDecimal modelScore = normaliseScore(mlScore.score());
        BigDecimal combinedScore = ruleScore.max(modelScore).min(ONE).setScale(2, RoundingMode.HALF_UP);

        Map<String, String> attributes = mlDecisionAttributes(mlScore, "max-rule-or-ml-score");
        attributes.put("ruleScore", ruleScore.toPlainString());
        attributes.put("mlScore", modelScore.toPlainString());

        return new FraudDecisionResponse(
                UUID.randomUUID(),
                transaction.transactionId(),
                transaction.tenantId(),
                DecisionMode.HYBRID,
                higherOutcome(rulesDecision.outcome(), mlScore.recommendedOutcome()),
                new RiskScore(
                        combinedScore,
                        higherRiskBand(rulesDecision.riskScore().band(), mlScore.riskBand()),
                        "Hybrid decision used the higher posture from rules and ML scoring."),
                rulesDecision.ruleHits(),
                List.of(modelSignal(mlScore, modelScore)),
                OffsetDateTime.now(clock),
                attributes);
    }

    private MlScoreRequest mlRequest(TransactionIngestionRequest transaction) {
        Map<String, String> features = new LinkedHashMap<>();
        features.putAll(transaction.attributes());
        features.put("transactionType", transaction.transactionType().name());
        features.put("channel", transaction.channel().name());
        features.put("direction", transaction.direction().name());
        features.put("amount", transaction.amount().value().toPlainString());
        features.put("currency", transaction.amount().currency());
        putPartyFeatures(features, "originator", transaction.originator());
        putPartyFeatures(features, "beneficiary", transaction.beneficiary());

        return new MlScoreRequest(
                transaction.transactionId(),
                transaction.tenantId(),
                properties.ml().modelName(),
                transaction,
                features);
    }

    private void putPartyFeatures(Map<String, String> features, String prefix, TransactionParty party) {
        if (party == null) {
            return;
        }
        putIfPresent(features, prefix + "InstitutionCode", party.institutionCode());
        putIfPresent(features, prefix + "CountryCode", party.countryCode());
    }

    private ModelSignal modelSignal(MlScoreResponse mlScore, BigDecimal contribution) {
        return new ModelSignal(
                mlScore.modelName(),
                mlScore.modelVersion(),
                normaliseScore(mlScore.score()),
                contribution,
                featureValues(mlScore.featureContributions()));
    }

    private Map<String, String> featureValues(List<MlFeatureContribution> featureContributions) {
        Map<String, String> featureValues = new LinkedHashMap<>();
        featureContributions.forEach(feature -> putIfPresent(
                featureValues,
                feature.featureName(),
                feature.featureValue()));
        return featureValues;
    }

    private Map<String, String> mlDecisionAttributes(MlScoreResponse mlScore, String strategy) {
        Map<String, String> attributes = new LinkedHashMap<>();
        attributes.putAll(mlScore.attributes());
        attributes.put("mlProvider", properties.ml().provider());
        attributes.put("modelName", mlScore.modelName());
        putIfPresent(attributes, "modelVersion", mlScore.modelVersion());
        attributes.put("mlScore", normaliseScore(mlScore.score()).toPlainString());
        attributes.put("mlScoreThreshold", properties.ml().scoreThreshold().toPlainString());
        attributes.put("decisionStrategy", strategy);
        return attributes;
    }

    private BigDecimal normaliseScore(BigDecimal value) {
        return value.max(BigDecimal.ZERO).min(ONE).setScale(2, RoundingMode.HALF_UP);
    }

    private RiskBand higherRiskBand(RiskBand first, RiskBand second) {
        return first.ordinal() >= second.ordinal() ? first : second;
    }

    private DecisionOutcome higherOutcome(DecisionOutcome first, DecisionOutcome second) {
        return first.ordinal() >= second.ordinal() ? first : second;
    }

    private void putIfPresent(Map<String, String> values, String key, String value) {
        if (key != null && !key.isBlank() && value != null && !value.isBlank()) {
            values.put(key, value);
        }
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
