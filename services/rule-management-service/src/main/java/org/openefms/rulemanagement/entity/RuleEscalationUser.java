package org.openefms.rulemanagement.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

public record RuleEscalationUser(
        String id,
        String tenantId,
        UUID ruleId,
        String ruleCode,
        String email,
        String userId,
        String name,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        String updatedBy
) {
}
