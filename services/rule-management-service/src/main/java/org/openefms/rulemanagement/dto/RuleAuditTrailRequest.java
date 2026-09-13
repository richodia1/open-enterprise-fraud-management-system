package org.openefms.rulemanagement.dto;

import java.util.UUID;
import org.openefms.common.models.rule.FraudRuleStatus;
import org.openefms.rulemanagement.enums.RuleAuditAction;

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
