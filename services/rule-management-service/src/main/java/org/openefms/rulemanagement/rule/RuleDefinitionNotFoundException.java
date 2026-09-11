package org.openefms.rulemanagement.rule;

class RuleDefinitionNotFoundException extends RuntimeException {
    RuleDefinitionNotFoundException(String tenantId, String ruleCode) {
        super("Rule " + ruleCode + " was not found for tenant " + tenantId + ".");
    }
}
