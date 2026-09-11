package org.openefms.common.models.rule;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.openefms.common.models.fraud.RiskBand;

/**
 * Shared rule-definition contract exposed by Rule Management.
 */
public record FraudRuleDefinition(
        UUID ruleId,
        String tenantId,
        String ruleCode,
        String ruleName,
        FraudRuleCategory category,
        FraudRuleStatus status,
        RiskBand severity,
        BigDecimal contribution,
        String explanation,
        Map<String, String> parameters,
        int version,
        OffsetDateTime updatedAt,
        String updatedBy
) {
    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    private static final BigDecimal ONE = BigDecimal.ONE.setScale(2, RoundingMode.HALF_UP);

    public FraudRuleDefinition {
        Objects.requireNonNull(ruleId, "ruleId must not be null");
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        Objects.requireNonNull(ruleCode, "ruleCode must not be null");
        Objects.requireNonNull(ruleName, "ruleName must not be null");
        Objects.requireNonNull(category, "category must not be null");
        Objects.requireNonNull(status, "status must not be null");
        Objects.requireNonNull(severity, "severity must not be null");
        Objects.requireNonNull(contribution, "contribution must not be null");
        Objects.requireNonNull(explanation, "explanation must not be null");
        Objects.requireNonNull(updatedAt, "updatedAt must not be null");
        Objects.requireNonNull(updatedBy, "updatedBy must not be null");
        if (version < 1) {
            throw new IllegalArgumentException("version must be greater than zero");
        }
        contribution = contribution.setScale(2, RoundingMode.HALF_UP);
        if (contribution.compareTo(ZERO) < 0 || contribution.compareTo(ONE) > 0) {
            throw new IllegalArgumentException("contribution must be between 0.00 and 1.00");
        }
        parameters = parameters == null ? Map.of() : Map.copyOf(parameters);
    }
}
