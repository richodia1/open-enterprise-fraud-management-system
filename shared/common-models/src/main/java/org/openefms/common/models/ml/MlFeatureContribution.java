package org.openefms.common.models.ml;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Per-feature contribution returned by an explainable model scorer.
 */
public record MlFeatureContribution(
        String featureName,
        String featureValue,
        BigDecimal contribution,
        String explanation
) {
    public MlFeatureContribution {
        Objects.requireNonNull(featureName, "featureName must not be null");
        Objects.requireNonNull(contribution, "contribution must not be null");
    }
}
