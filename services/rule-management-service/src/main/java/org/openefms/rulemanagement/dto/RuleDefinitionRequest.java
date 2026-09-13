package org.openefms.rulemanagement.dto;

import java.math.BigDecimal;
import java.util.Map;
import org.openefms.common.models.fraud.RiskBand;
import org.openefms.common.models.rule.FraudRuleCategory;
import org.openefms.common.models.rule.FraudRuleStatus;

public record RuleDefinitionRequest(
        String tenantId,
        String ruleCode,
        String ruleName,
        FraudRuleCategory category,
        FraudRuleStatus status,
        RiskBand severity,
        BigDecimal contribution,
        String explanation,
        Map<String, String> parameters,
        String updatedBy
) {
}
