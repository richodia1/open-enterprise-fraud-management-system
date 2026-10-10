package org.openefms.common.models.intelligence;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Objects;

/**
 * Natural-language fraud intelligence request for management and investigator support.
 */
public record ManagementQuestionRequest(
        String tenantId,
        String question,
        String audience,
        OffsetDateTime periodStart,
        OffsetDateTime periodEnd,
        Map<String, String> filters,
        Map<String, String> context
) {
    public ManagementQuestionRequest {
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        Objects.requireNonNull(question, "question must not be null");
        audience = audience == null || audience.isBlank() ? "MANAGEMENT" : audience.trim();
        filters = filters == null ? Map.of() : Map.copyOf(filters);
        context = context == null ? Map.of() : Map.copyOf(context);
    }
}
