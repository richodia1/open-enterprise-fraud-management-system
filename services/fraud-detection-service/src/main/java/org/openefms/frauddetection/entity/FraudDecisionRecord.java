package org.openefms.frauddetection.entity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.openefms.common.models.fraud.DecisionMode;
import org.openefms.common.models.fraud.DecisionOutcome;
import org.openefms.common.models.fraud.RecommendedTransactionAction;
import org.openefms.common.models.fraud.RiskBand;

public record FraudDecisionRecord(
        UUID decisionId,
        UUID transactionId,
        String tenantId,
        DecisionMode mode,
        DecisionOutcome outcome,
        RecommendedTransactionAction recommendedAction,
        BigDecimal riskScoreValue,
        RiskBand riskBand,
        String riskReason,
        int ruleHitCount,
        int modelSignalCount,
        String ruleEvidenceSummary,
        String modelSignalSummary,
        OffsetDateTime decidedAt,
        String eventType,
        UUID eventId,
        String correlationId,
        String causationId,
        String traceId,
        Map<String, String> attributes
) {
    public FraudDecisionRecord {
        Objects.requireNonNull(decisionId, "decisionId must not be null");
        Objects.requireNonNull(transactionId, "transactionId must not be null");
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        Objects.requireNonNull(mode, "mode must not be null");
        Objects.requireNonNull(outcome, "outcome must not be null");
        recommendedAction = recommendedAction == null
                ? RecommendedTransactionAction.fromOutcome(outcome)
                : recommendedAction;
        Objects.requireNonNull(riskScoreValue, "riskScoreValue must not be null");
        Objects.requireNonNull(riskBand, "riskBand must not be null");
        Objects.requireNonNull(decidedAt, "decidedAt must not be null");
        riskReason = riskReason == null ? "" : riskReason;
        ruleEvidenceSummary = ruleEvidenceSummary == null ? "" : ruleEvidenceSummary;
        modelSignalSummary = modelSignalSummary == null ? "" : modelSignalSummary;
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }
}
