package org.openefms.rulemanagement.service;

public class RuleDefinitionNotFoundException extends RuntimeException {
    public RuleDefinitionNotFoundException(String tenantId, String ruleCode) {
        super("Rule " + ruleCode + " was not found for tenant " + tenantId + ".");
    }
}
