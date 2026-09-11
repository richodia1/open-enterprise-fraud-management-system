package org.openefms.rulemanagement.rule;

import org.openefms.common.models.rule.FraudRuleStatus;

public record RuleStatusUpdateRequest(
        String tenantId,
        FraudRuleStatus status,
        String updatedBy
) {
}
