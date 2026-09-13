package org.openefms.common.models.fraud;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Shared response contract for a preliminary fraud decision.
 */
public record FraudDecisionResponse(
        UUID decisionId,
        UUID transactionId,
        String tenantId,
        DecisionMode mode,
        DecisionOutcome outcome,
        RecommendedTransactionAction recommendedAction,
        RiskScore riskScore,
        List<RuleHit> ruleHits,
        List<ModelSignal> modelSignals,
        OffsetDateTime decidedAt,
        Map<String, String> attributes
) {
    public FraudDecisionResponse(
            UUID decisionId,
            UUID transactionId,
            String tenantId,
            DecisionMode mode,
            DecisionOutcome outcome,
            RiskScore riskScore,
            List<RuleHit> ruleHits,
            List<ModelSignal> modelSignals,
            OffsetDateTime decidedAt,
            Map<String, String> attributes) {
        this(
                decisionId,
                transactionId,
                tenantId,
                mode,
                outcome,
                RecommendedTransactionAction.fromOutcome(outcome),
                riskScore,
                ruleHits,
                modelSignals,
                decidedAt,
                attributes);
    }

    public FraudDecisionResponse {
        Objects.requireNonNull(decisionId, "decisionId must not be null");
        Objects.requireNonNull(transactionId, "transactionId must not be null");
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        Objects.requireNonNull(mode, "mode must not be null");
        Objects.requireNonNull(outcome, "outcome must not be null");
        recommendedAction = recommendedAction == null
                ? RecommendedTransactionAction.fromOutcome(outcome)
                : recommendedAction;
        Objects.requireNonNull(riskScore, "riskScore must not be null");
        Objects.requireNonNull(decidedAt, "decidedAt must not be null");
        ruleHits = ruleHits == null ? List.of() : List.copyOf(ruleHits);
        modelSignals = modelSignals == null ? List.of() : List.copyOf(modelSignals);
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }
}
