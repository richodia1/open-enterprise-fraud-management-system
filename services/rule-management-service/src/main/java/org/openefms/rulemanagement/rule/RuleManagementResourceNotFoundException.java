package org.openefms.rulemanagement.rule;

class RuleManagementResourceNotFoundException extends RuntimeException {
    RuleManagementResourceNotFoundException(String resourceName, String id) {
        super(resourceName + " " + id + " was not found");
    }
}
