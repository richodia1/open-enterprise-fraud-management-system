package org.openefms.common.models.fraud;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

/**
 * Contribution from a model or statistical scorer.
 */
public record ModelSignal(
        String modelName,
        String modelVersion,
        BigDecimal score,
        BigDecimal contribution,
        Map<String, String> featureValues
) {
    public ModelSignal {
        Objects.requireNonNull(modelName, "modelName must not be null");
        Objects.requireNonNull(score, "score must not be null");
        featureValues = featureValues == null ? Map.of() : Map.copyOf(featureValues);
    }
}
