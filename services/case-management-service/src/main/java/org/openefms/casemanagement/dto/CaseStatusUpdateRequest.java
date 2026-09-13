package org.openefms.casemanagement.dto;

import java.util.Map;
import org.openefms.casemanagement.enums.CaseStatus;

public record CaseStatusUpdateRequest(
        CaseStatus status,
        String actorId,
        String note,
        Map<String, String> attributes
) {
}
