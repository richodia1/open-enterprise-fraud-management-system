package org.openefms.rulemanagement.rule;

import java.util.List;

public class InvalidRuleDefinitionException extends RuntimeException {
    private final List<String> errors;

    InvalidRuleDefinitionException(List<String> errors) {
        super("Rule definition failed validation.");
        this.errors = List.copyOf(errors);
    }

    List<String> errors() {
        return errors;
    }
}
