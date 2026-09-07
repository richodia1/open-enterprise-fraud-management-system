package org.openefms.common.models.explanation;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

/**
 * Evidence item used to explain a fraud decision.
 */
public record EvidenceItem(
        String evidenceType,
        String title,
        String description,
        BigDecimal contribution,
        Map<String, String> attributes
) {
    public EvidenceItem {
        Objects.requireNonNull(evidenceType, "evidenceType must not be null");
        Objects.requireNonNull(title, "title must not be null");
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }
}
