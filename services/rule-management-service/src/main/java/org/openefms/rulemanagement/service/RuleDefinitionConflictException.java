package org.openefms.rulemanagement.service;

public class RuleDefinitionConflictException extends RuntimeException {
    public RuleDefinitionConflictException(String tenantId, String ruleCode) {
        super("Rule " + ruleCode + " already exists for tenant " + tenantId + ".");
    }
}
