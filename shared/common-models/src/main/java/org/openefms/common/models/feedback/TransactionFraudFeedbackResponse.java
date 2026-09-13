package org.openefms.common.models.feedback;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.openefms.common.models.explanation.EvidenceItem;
import org.openefms.common.models.explanation.ExplanationResponse;
import org.openefms.common.models.fraud.DecisionMode;
import org.openefms.common.models.fraud.DecisionOutcome;
import org.openefms.common.models.fraud.FraudDecisionResponse;
import org.openefms.common.models.fraud.RecommendedTransactionAction;
import org.openefms.common.models.fraud.RiskScore;

/**
 * Combined feedback payload for transaction cores and operator-facing clients.
 */
public record TransactionFraudFeedbackResponse(
        UUID transactionId,
        UUID decisionId,
        UUID explanationId,
        String tenantId,
        DecisionMode mode,
        DecisionOutcome outcome,
        RecommendedTransactionAction recommendedAction,
        RiskScore riskScore,
        String explanationSummary,
        List<EvidenceItem> evidence,
        OffsetDateTime decidedAt,
        OffsetDateTime explainedAt,
        Map<String, String> attributes
) {
    public TransactionFraudFeedbackResponse {
        Objects.requireNonNull(transactionId, "transactionId must not be null");
        Objects.requireNonNull(decisionId, "decisionId must not be null");
        Objects.requireNonNull(explanationId, "explanationId must not be null");
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        Objects.requireNonNull(mode, "mode must not be null");
        Objects.requireNonNull(outcome, "outcome must not be null");
        Objects.requireNonNull(recommendedAction, "recommendedAction must not be null");
        Objects.requireNonNull(riskScore, "riskScore must not be null");
        Objects.requireNonNull(explanationSummary, "explanationSummary must not be null");
        Objects.requireNonNull(decidedAt, "decidedAt must not be null");
        Objects.requireNonNull(explainedAt, "explainedAt must not be null");
        evidence = evidence == null ? List.of() : List.copyOf(evidence);
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }

    public static TransactionFraudFeedbackResponse from(
            FraudDecisionResponse decision,
            ExplanationResponse explanation) {
        Objects.requireNonNull(decision, "decision must not be null");
        Objects.requireNonNull(explanation, "explanation must not be null");

        Map<String, String> attributes = new LinkedHashMap<>(decision.attributes());
        attributes.putAll(explanation.attributes());

        return new TransactionFraudFeedbackResponse(
                decision.transactionId(),
                decision.decisionId(),
                explanation.explanationId(),
                decision.tenantId(),
                decision.mode(),
                decision.outcome(),
                decision.recommendedAction(),
                decision.riskScore(),
                explanation.summary(),
                explanation.evidence(),
                decision.decidedAt(),
                explanation.explainedAt(),
                attributes);
    }
}
