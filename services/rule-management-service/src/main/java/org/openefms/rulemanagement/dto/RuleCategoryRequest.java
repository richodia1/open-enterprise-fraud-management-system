package org.openefms.rulemanagement.dto;

public record RuleCategoryRequest(
        String tenantId,
        String name,
        String description,
        String updatedBy
) {
}
