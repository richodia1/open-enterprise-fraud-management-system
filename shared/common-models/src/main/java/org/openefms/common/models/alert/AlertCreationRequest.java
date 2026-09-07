package org.openefms.common.models.alert;

import org.openefms.common.models.fraud.RiskBand;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Request contract for creating a fraud alert from a preliminary decision.
 */
public record AlertCreationRequest(
        UUID alertId,
        UUID decisionId,
        UUID transactionId,
        String tenantId,
        AlertPriority priority,
        RiskBand riskBand,
        String title,
        String summary,
        OffsetDateTime createdAt,
        Map<String, String> attributes
) {
    public AlertCreationRequest {
        Objects.requireNonNull(alertId, "alertId must not be null");
        Objects.requireNonNull(decisionId, "decisionId must not be null");
        Objects.requireNonNull(transactionId, "transactionId must not be null");
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        Objects.requireNonNull(priority, "priority must not be null");
        Objects.requireNonNull(riskBand, "riskBand must not be null");
        Objects.requireNonNull(title, "title must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }
}
