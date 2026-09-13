package org.openefms.rulemanagement.service;

public class RuleManagementResourceNotFoundException extends RuntimeException {
    public RuleManagementResourceNotFoundException(String resourceName, String id) {
        super(resourceName + " " + id + " was not found");
    }
}
