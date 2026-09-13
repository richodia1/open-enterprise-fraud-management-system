package org.openefms.rulemanagement.rule;

import java.util.UUID;
import org.openefms.common.models.rule.FraudRuleStatus;

public record RuleAuditTrailRequest(
        String tenantId,
        UUID ruleId,
        String ruleCode,
        RuleAuditAction action,
        FraudRuleStatus fromStatus,
        FraudRuleStatus toStatus,
        String note,
        String correlationId,
        String createdBy,
        String updatedBy
) {
}
