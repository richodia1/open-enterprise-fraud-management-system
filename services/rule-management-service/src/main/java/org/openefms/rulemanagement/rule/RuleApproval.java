package org.openefms.rulemanagement.rule;

import java.time.OffsetDateTime;
import java.util.UUID;

public record RuleApproval(
        String approvalId,
        String tenantId,
        UUID ruleId,
        String ruleCode,
        String ruleName,
        RuleApprovalAction requestedAction,
        RuleApprovalStatus status,
        String requestedBy,
        OffsetDateTime requestedAt,
        String requestNote,
        String decidedBy,
        OffsetDateTime decidedAt,
        String decisionNote
) {
}
