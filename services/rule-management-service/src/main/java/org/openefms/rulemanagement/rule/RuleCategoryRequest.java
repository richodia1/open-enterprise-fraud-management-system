package org.openefms.rulemanagement.rule;

public record RuleCategoryRequest(
        String tenantId,
        String name,
        String description,
        String updatedBy
) {
}
