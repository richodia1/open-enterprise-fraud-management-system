package org.openefms.casemanagement.dto;

import java.util.Map;
import org.openefms.casemanagement.enums.CaseAssignmentPurpose;

public record CaseAssignmentRequest(
        String assigneeId,
        String actorId,
        String note,
        Map<String, String> attributes,
        CaseAssignmentPurpose purpose
) {
    public CaseAssignmentRequest(
            String assigneeId,
            String actorId,
            String note,
            Map<String, String> attributes) {
        this(assigneeId, actorId, note, attributes, CaseAssignmentPurpose.ASSIGNMENT);
    }
}
