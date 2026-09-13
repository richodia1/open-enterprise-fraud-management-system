package org.openefms.casemanagement.dto;

import java.util.Map;

public record CaseAssignmentRequest(
        String assigneeId,
        String actorId,
        String note,
        Map<String, String> attributes
) {
}
