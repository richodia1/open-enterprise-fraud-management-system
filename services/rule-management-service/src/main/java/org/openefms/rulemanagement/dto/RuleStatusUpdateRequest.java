package org.openefms.rulemanagement.dto;

import org.openefms.common.models.rule.FraudRuleStatus;

public record RuleStatusUpdateRequest(
        String tenantId,
        FraudRuleStatus status,
        String updatedBy
) {
}
