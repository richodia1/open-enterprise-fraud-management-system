package org.openefms.common.models.explanation;

import org.openefms.common.models.fraud.ModelSignal;
import org.openefms.common.models.fraud.RuleHit;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Request for building investigator-facing decision support.
 */
public record ExplanationRequest(
        UUID explanationId,
        UUID decisionId,
        UUID transactionId,
        String tenantId,
        List<RuleHit> ruleHits,
        List<ModelSignal> modelSignals,
        Map<String, String> context
) {
    public ExplanationRequest {
        Objects.requireNonNull(explanationId, "explanationId must not be null");
        Objects.requireNonNull(decisionId, "decisionId must not be null");
        Objects.requireNonNull(transactionId, "transactionId must not be null");
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        ruleHits = ruleHits == null ? List.of() : List.copyOf(ruleHits);
        modelSignals = modelSignals == null ? List.of() : List.copyOf(modelSignals);
        context = context == null ? Map.of() : Map.copyOf(context);
    }
}
