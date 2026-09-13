package org.openefms.casemanagement.entity;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.openefms.casemanagement.enums.CaseActionType;
import org.openefms.casemanagement.enums.CaseStatus;

public record CaseActionRecord(
        UUID caseActionId,
        UUID caseId,
        String tenantId,
        CaseActionType actionType,
        String actorId,
        String note,
        CaseStatus fromStatus,
        CaseStatus toStatus,
        String assigneeId,
        OffsetDateTime occurredAt,
        Map<String, String> attributes
) {
    public CaseActionRecord {
        Objects.requireNonNull(caseActionId, "caseActionId must not be null");
        Objects.requireNonNull(caseId, "caseId must not be null");
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        Objects.requireNonNull(actionType, "actionType must not be null");
        Objects.requireNonNull(actorId, "actorId must not be null");
        Objects.requireNonNull(occurredAt, "occurredAt must not be null");
        note = note == null ? "" : note;
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }
}
