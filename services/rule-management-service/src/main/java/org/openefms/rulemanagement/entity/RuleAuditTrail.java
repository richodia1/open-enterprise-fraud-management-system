package org.openefms.rulemanagement.entity;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.openefms.common.models.rule.FraudRuleStatus;
import org.openefms.rulemanagement.enums.RuleAuditAction;

public record RuleAuditTrail(
        String id,
        String tenantId,
        UUID ruleId,
        String ruleCode,
        RuleAuditAction action,
        FraudRuleStatus fromStatus,
        FraudRuleStatus toStatus,
        String note,
        String correlationId,
        OffsetDateTime createdAt,
        String createdBy,
        OffsetDateTime updatedAt,
        String updatedBy
) {
}
