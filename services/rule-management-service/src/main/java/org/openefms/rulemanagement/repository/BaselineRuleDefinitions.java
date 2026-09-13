package org.openefms.rulemanagement.repository;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.openefms.common.models.fraud.RiskBand;
import org.openefms.common.models.rule.FraudRuleCategory;
import org.openefms.common.models.rule.FraudRuleDefinition;
import org.openefms.common.models.rule.FraudRuleStatus;
import org.springframework.stereotype.Component;

@Component
public class BaselineRuleDefinitions {
    public static final String GLOBAL_TENANT_ID = "global";
    private static final String SYSTEM_ACTOR = "system:rule-management-service";

    public BaselineRuleDefinitions() {
    }

    public List<FraudRuleDefinition> load(Clock clock) {
        OffsetDateTime now = OffsetDateTime.now(clock);
        return List.of(
                rule(
                        "1b4f2ce0-7c23-41f8-a8c6-3da92ec4d001",
                        "HIGH_VALUE_AMOUNT",
                        "High value amount",
                        FraudRuleCategory.AMOUNT,
                        RiskBand.HIGH,
                        "0.35",
                        "Transaction amount is at or above the configured high-value threshold.",
                        Map.of("threshold", "10000.00"),
                        now),
                rule(
                        "1b4f2ce0-7c23-41f8-a8c6-3da92ec4d002",
                        "CRITICAL_VALUE_AMOUNT",
                        "Critical value amount",
                        FraudRuleCategory.AMOUNT,
                        RiskBand.CRITICAL,
                        "0.65",
                        "Transaction amount is at or above the configured critical-value threshold.",
                        Map.of("threshold", "100000.00"),
                        now),
                rule(
                        "1b4f2ce0-7c23-41f8-a8c6-3da92ec4d003",
                        "VELOCITY_REVIEW_THRESHOLD",
                        "Velocity review threshold",
                        FraudRuleCategory.VELOCITY,
                        RiskBand.MEDIUM,
                        "0.20",
                        "Recent transaction count is at or above the configured review threshold.",
                        Map.of("count", "5", "window", "PT1H"),
                        now),
                rule(
                        "1b4f2ce0-7c23-41f8-a8c6-3da92ec4d004",
                        "VELOCITY_BLOCK_THRESHOLD",
                        "Velocity block threshold",
                        FraudRuleCategory.VELOCITY,
                        RiskBand.HIGH,
                        "0.35",
                        "Recent transaction count is at or above the configured block threshold.",
                        Map.of("count", "10", "window", "PT1H"),
                        now),
                rule(
                        "1b4f2ce0-7c23-41f8-a8c6-3da92ec4d005",
                        "CROSS_BORDER_PARTIES",
                        "Cross-border parties",
                        FraudRuleCategory.GEOGRAPHY,
                        RiskBand.MEDIUM,
                        "0.15",
                        "Originator and beneficiary country codes differ.",
                        Map.of(),
                        now),
                rule(
                        "1b4f2ce0-7c23-41f8-a8c6-3da92ec4d006",
                        "HIGH_RISK_HINT",
                        "High risk hint",
                        FraudRuleCategory.SIGNAL,
                        RiskBand.HIGH,
                        "0.25",
                        "Input attributes contain an upstream high-risk hint.",
                        Map.of("acceptedValues", "high,true,yes,y,1"),
                        now),
                rule(
                        "1b4f2ce0-7c23-41f8-a8c6-3da92ec4d007",
                        "UNKNOWN_TRANSACTION_CHANNEL",
                        "Unknown transaction channel",
                        FraudRuleCategory.CHANNEL,
                        RiskBand.LOW,
                        "0.10",
                        "Transaction arrived through the generic OTHER channel.",
                        Map.of("channel", "OTHER"),
                        now));
    }

    private FraudRuleDefinition rule(
            String ruleId,
            String ruleCode,
            String ruleName,
            FraudRuleCategory category,
            RiskBand severity,
            String contribution,
            String explanation,
            Map<String, String> parameters,
            OffsetDateTime updatedAt) {
        return new FraudRuleDefinition(
                UUID.fromString(ruleId),
                GLOBAL_TENANT_ID,
                ruleCode,
                ruleName,
                category,
                FraudRuleStatus.ACTIVE,
                severity,
                new BigDecimal(contribution),
                explanation,
                parameters,
                1,
                updatedAt,
                SYSTEM_ACTOR);
    }
}
