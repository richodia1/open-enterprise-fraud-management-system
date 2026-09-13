package org.openefms.rulemanagement.service;

import java.util.List;

public class InvalidRuleDefinitionException extends RuntimeException {
    private final List<String> errors;

    public InvalidRuleDefinitionException(List<String> errors) {
        super("Rule definition failed validation.");
        this.errors = List.copyOf(errors);
    }

    public List<String> errors() {
        return errors;
    }
}
