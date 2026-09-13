package org.openefms.rulemanagement.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.openefms.rulemanagement.dto.RuleDefinitionRequest;
import org.openefms.rulemanagement.dto.RuleStatusUpdateRequest;

class RuleDefinitionValidator {
    private static final BigDecimal ZERO = new BigDecimal("0.00");
    private static final BigDecimal ONE = new BigDecimal("1.00");
    private static final String RULE_CODE_PATTERN = "^[A-Z][A-Z0-9_]{2,63}$";

    void validateCreateOrReplace(RuleDefinitionRequest request) {
        List<String> errors = new ArrayList<>();
        if (request == null) {
            errors.add("request body must not be null");
        } else {
            validateText(request.tenantId(), "tenantId", errors);
            validateText(request.ruleCode(), "ruleCode", errors);
            validateRuleCode(request.ruleCode(), errors);
            validateText(request.ruleName(), "ruleName", errors);
            validateText(request.explanation(), "explanation", errors);
            validateText(request.updatedBy(), "updatedBy", errors);
            validateRequired(request.category(), "category", errors);
            validateRequired(request.status(), "status", errors);
            validateRequired(request.severity(), "severity", errors);
            validateContribution(request.contribution(), errors);
            validateParameters(request.parameters(), errors);
        }
        throwIfInvalid(errors);
    }

    void validateStatusUpdate(RuleStatusUpdateRequest request) {
        List<String> errors = new ArrayList<>();
        if (request == null) {
            errors.add("request body must not be null");
        } else {
            validateText(request.tenantId(), "tenantId", errors);
            validateRequired(request.status(), "status", errors);
            validateText(request.updatedBy(), "updatedBy", errors);
        }
        throwIfInvalid(errors);
    }

    void validateFilter(String tenantId, String status, String category) {
        List<String> errors = new ArrayList<>();
        validateText(tenantId, "tenantId", errors);
        validateEnumValue(status, "status", errors);
        validateEnumValue(category, "category", errors);
        throwIfInvalid(errors);
    }

    void validateLookup(String tenantId, String ruleCode) {
        List<String> errors = new ArrayList<>();
        validateText(tenantId, "tenantId", errors);
        validateText(ruleCode, "ruleCode", errors);
        validateRuleCode(ruleCode, errors);
        throwIfInvalid(errors);
    }

    private void validateText(String value, String fieldName, List<String> errors) {
        if (value == null || value.trim().isEmpty()) {
            errors.add(fieldName + " must not be blank");
        }
    }

    private void validateRuleCode(String value, List<String> errors) {
        if (value == null || value.isBlank()) {
            return;
        }
        String normalisedValue = value.trim().toUpperCase(Locale.ROOT);
        if (!normalisedValue.matches(RULE_CODE_PATTERN)) {
            errors.add("ruleCode must start with a letter and contain only uppercase letters, numbers, and underscores");
        }
    }

    private void validateRequired(Object value, String fieldName, List<String> errors) {
        if (value == null) {
            errors.add(fieldName + " must not be null");
        }
    }

    private void validateContribution(BigDecimal contribution, List<String> errors) {
        if (contribution == null) {
            errors.add("contribution must not be null");
            return;
        }
        if (contribution.compareTo(ZERO) < 0 || contribution.compareTo(ONE) > 0) {
            errors.add("contribution must be between 0.00 and 1.00");
        }
    }

    private void validateParameters(Map<String, String> parameters, List<String> errors) {
        if (parameters == null) {
            return;
        }
        parameters.forEach((key, value) -> {
            if (key == null || key.trim().isEmpty()) {
                errors.add("parameters keys must not be blank");
            }
            if (value == null) {
                errors.add("parameters values must not be null");
            }
        });
    }

    private void validateEnumValue(String value, String fieldName, List<String> errors) {
        if (value != null && value.trim().isEmpty()) {
            errors.add(fieldName + " must not be blank when supplied");
        }
    }

    private void throwIfInvalid(List<String> errors) {
        if (!errors.isEmpty()) {
            throw new InvalidRuleDefinitionException(errors);
        }
    }
}
