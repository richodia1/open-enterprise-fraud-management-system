package org.openefms.rulemanagement.dto;

public record RuleApprovalDecisionRequest(
        String decidedBy,
        String decisionNote
) {
}
