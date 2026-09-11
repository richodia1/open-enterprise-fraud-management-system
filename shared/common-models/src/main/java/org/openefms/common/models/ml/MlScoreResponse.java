package org.openefms.common.models.ml;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.openefms.common.models.fraud.DecisionOutcome;
import org.openefms.common.models.fraud.RiskBand;

/**
 * Stable response contract for a transaction ML scorer.
 */
public record MlScoreResponse(
        String modelName,
        String modelVersion,
        BigDecimal score,
        RiskBand riskBand,
        DecisionOutcome recommendedOutcome,
        List<MlFeatureContribution> featureContributions,
        OffsetDateTime scoredAt,
        Map<String, String> attributes
) {
    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    private static final BigDecimal ONE = BigDecimal.ONE.setScale(2, RoundingMode.HALF_UP);

    public MlScoreResponse {
        Objects.requireNonNull(modelName, "modelName must not be null");
        Objects.requireNonNull(score, "score must not be null");
        Objects.requireNonNull(riskBand, "riskBand must not be null");
        Objects.requireNonNull(recommendedOutcome, "recommendedOutcome must not be null");
        Objects.requireNonNull(scoredAt, "scoredAt must not be null");
        score = score.setScale(2, RoundingMode.HALF_UP);
        if (score.compareTo(ZERO) < 0 || score.compareTo(ONE) > 0) {
            throw new IllegalArgumentException("score must be between 0.00 and 1.00");
        }
        featureContributions = featureContributions == null ? List.of() : List.copyOf(featureContributions);
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }
}
