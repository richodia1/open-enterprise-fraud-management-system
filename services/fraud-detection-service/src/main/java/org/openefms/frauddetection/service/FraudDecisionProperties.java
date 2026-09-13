package org.openefms.frauddetection.service;

import java.math.BigDecimal;
import java.util.Locale;
import org.openefms.common.models.fraud.DecisionMode;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "oefms.fraud.decision")
public record FraudDecisionProperties(
        String mode,
        Rules rules,
        Ml ml
) {
    public FraudDecisionProperties {
        mode = textOrDefault(mode, "rules");
        rules = rules == null ? new Rules(null, null, null, null, null, null, null) : rules;
        ml = ml == null ? new Ml(null, null, null) : ml;
    }

    public DecisionMode activeMode() {
        return switch (mode.toLowerCase(Locale.ROOT).replace("-", "_")) {
            case "rule", "rules" -> DecisionMode.RULES;
            case "ml", "ai" -> DecisionMode.ML;
            case "hybrid" -> DecisionMode.HYBRID;
            default -> throw new IllegalArgumentException("Unsupported fraud decision mode: " + mode);
        };
    }

    public record Rules(
            BigDecimal highValueAmountThreshold,
            BigDecimal criticalValueAmountThreshold,
            Integer velocityReviewCount,
            Integer velocityBlockCount,
            BigDecimal reviewScoreThreshold,
            BigDecimal blockScoreThreshold,
            BigDecimal escalateScoreThreshold
    ) {
        public Rules {
            highValueAmountThreshold = decimalOrDefault(highValueAmountThreshold, "10000.00");
            criticalValueAmountThreshold = decimalOrDefault(criticalValueAmountThreshold, "100000.00");
            velocityReviewCount = integerOrDefault(velocityReviewCount, 5);
            velocityBlockCount = integerOrDefault(velocityBlockCount, 10);
            reviewScoreThreshold = decimalOrDefault(reviewScoreThreshold, "0.35");
            blockScoreThreshold = decimalOrDefault(blockScoreThreshold, "0.70");
            escalateScoreThreshold = decimalOrDefault(escalateScoreThreshold, "0.90");
        }
    }

    public record Ml(
            String provider,
            String modelName,
            BigDecimal scoreThreshold
    ) {
        public Ml {
            provider = textOrDefault(provider, "disabled");
            modelName = textOrDefault(modelName, "xgboost-baseline");
            scoreThreshold = decimalOrDefault(scoreThreshold, "0.80");
        }
    }

    private static BigDecimal decimalOrDefault(BigDecimal value, String defaultValue) {
        return value == null ? new BigDecimal(defaultValue) : value;
    }

    private static Integer integerOrDefault(Integer value, int defaultValue) {
        return value == null ? defaultValue : value;
    }

    private static String textOrDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
