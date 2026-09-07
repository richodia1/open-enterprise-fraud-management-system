package org.openefms.common.models.explanation;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Investigator-facing explanation pack for a fraud decision.
 */
public record ExplanationResponse(
        UUID explanationId,
        UUID decisionId,
        UUID transactionId,
        String tenantId,
        String summary,
        List<EvidenceItem> evidence,
        OffsetDateTime explainedAt,
        Map<String, String> attributes
) {
    public ExplanationResponse {
        Objects.requireNonNull(explanationId, "explanationId must not be null");
        Objects.requireNonNull(decisionId, "decisionId must not be null");
        Objects.requireNonNull(transactionId, "transactionId must not be null");
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        Objects.requireNonNull(summary, "summary must not be null");
        Objects.requireNonNull(explainedAt, "explainedAt must not be null");
        evidence = evidence == null ? List.of() : List.copyOf(evidence);
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }
}
