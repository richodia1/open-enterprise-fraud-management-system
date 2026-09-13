package org.openefms.rulemanagement.rule;

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
