package org.openefms.rulemanagement.entity;

import java.time.OffsetDateTime;

public record RuleCategory(
        String id,
        String tenantId,
        String name,
        String description,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        String updatedBy
) {
}
