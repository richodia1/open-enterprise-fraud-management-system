package org.openefms.casemanagement.workflow;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.openefms.common.models.casefile.CasePriority;

public record CaseRecord(
        UUID caseId,
        UUID alertId,
        UUID transactionId,
        UUID decisionId,
        String tenantId,
        CasePriority priority,
        CaseStatus status,
        String title,
        String summary,
        OffsetDateTime createdAt,
        String eventType,
        UUID eventId,
        String correlationId,
        String causationId,
        String traceId,
        Map<String, String> attributes
) {
    public CaseRecord {
        Objects.requireNonNull(caseId, "caseId must not be null");
        Objects.requireNonNull(alertId, "alertId must not be null");
        Objects.requireNonNull(transactionId, "transactionId must not be null");
        Objects.requireNonNull(decisionId, "decisionId must not be null");
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        Objects.requireNonNull(priority, "priority must not be null");
        Objects.requireNonNull(status, "status must not be null");
        Objects.requireNonNull(title, "title must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
        summary = summary == null ? "" : summary;
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }
}
