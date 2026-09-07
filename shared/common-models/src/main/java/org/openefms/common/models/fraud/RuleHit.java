package org.openefms.common.models.fraud;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

/**
 * Rule contribution captured during fraud decisioning.
 */
public record RuleHit(
        String ruleCode,
        String ruleName,
        RiskBand severity,
        BigDecimal contribution,
        String explanation,
        Map<String, String> attributes
) {
    public RuleHit {
        Objects.requireNonNull(ruleCode, "ruleCode must not be null");
        Objects.requireNonNull(severity, "severity must not be null");
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }
}
