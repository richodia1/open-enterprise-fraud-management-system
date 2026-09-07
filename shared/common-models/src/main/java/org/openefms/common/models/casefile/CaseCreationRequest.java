package org.openefms.common.models.casefile;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Request contract for creating an investigation case from an alert or decision.
 */
public record CaseCreationRequest(
        UUID caseId,
        UUID alertId,
        UUID transactionId,
        UUID decisionId,
        String tenantId,
        CasePriority priority,
        String title,
        String summary,
        OffsetDateTime createdAt,
        Map<String, String> attributes
) {
    public CaseCreationRequest {
        Objects.requireNonNull(caseId, "caseId must not be null");
        Objects.requireNonNull(transactionId, "transactionId must not be null");
        Objects.requireNonNull(decisionId, "decisionId must not be null");
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        Objects.requireNonNull(priority, "priority must not be null");
        Objects.requireNonNull(title, "title must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }
}
