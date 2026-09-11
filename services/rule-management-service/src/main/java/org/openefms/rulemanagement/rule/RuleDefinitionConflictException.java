package org.openefms.rulemanagement.rule;

class RuleDefinitionConflictException extends RuntimeException {
    RuleDefinitionConflictException(String tenantId, String ruleCode) {
        super("Rule " + ruleCode + " already exists for tenant " + tenantId + ".");
    }
}
