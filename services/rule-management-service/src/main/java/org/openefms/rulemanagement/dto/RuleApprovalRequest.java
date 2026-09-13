package org.openefms.rulemanagement.dto;

import org.openefms.rulemanagement.enums.RuleApprovalAction;

public record RuleApprovalRequest(
        String tenantId,
        RuleApprovalAction requestedAction,
        String requestedBy,
        String note
) {
}
