package org.openefms.rulemanagement.rule;

public record RuleApprovalRequest(
        String tenantId,
        RuleApprovalAction requestedAction,
        String requestedBy,
        String note
) {
}
