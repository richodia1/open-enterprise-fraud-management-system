package org.openefms.rulemanagement.rule;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.openefms.common.models.rule.FraudRuleStatus;

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
