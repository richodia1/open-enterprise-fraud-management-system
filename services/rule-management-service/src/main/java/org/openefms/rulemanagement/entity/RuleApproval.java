package org.openefms.rulemanagement.entity;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.openefms.rulemanagement.enums.RuleApprovalAction;
import org.openefms.rulemanagement.enums.RuleApprovalStatus;

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
