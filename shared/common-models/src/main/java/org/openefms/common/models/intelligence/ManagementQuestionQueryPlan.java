package org.openefms.common.models.intelligence;

import java.util.Map;
import java.util.Objects;

/**
 * Structured data plan that downstream adapters can execute before the answer is summarised.
 */
public record ManagementQuestionQueryPlan(
        String sourceService,
        String endpoint,
        String purpose,
        Map<String, String> parameters
) {
    public ManagementQuestionQueryPlan {
        Objects.requireNonNull(sourceService, "sourceService must not be null");
        Objects.requireNonNull(endpoint, "endpoint must not be null");
        Objects.requireNonNull(purpose, "purpose must not be null");
        parameters = parameters == null ? Map.of() : Map.copyOf(parameters);
    }
}
