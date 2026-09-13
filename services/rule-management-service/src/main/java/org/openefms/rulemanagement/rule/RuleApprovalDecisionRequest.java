package org.openefms.rulemanagement.rule;

public record RuleApprovalDecisionRequest(
        String decidedBy,
        String decisionNote
) {
}
