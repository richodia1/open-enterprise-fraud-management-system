package org.openefms.rulemanagement.rule;

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
