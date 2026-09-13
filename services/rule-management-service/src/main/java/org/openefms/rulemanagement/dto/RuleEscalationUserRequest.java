package org.openefms.rulemanagement.dto;

import java.util.UUID;

public record RuleEscalationUserRequest(
        String tenantId,
        UUID ruleId,
        String ruleCode,
        String email,
        String userId,
        String name,
        String updatedBy
) {
}
