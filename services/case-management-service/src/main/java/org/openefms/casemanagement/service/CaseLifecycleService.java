package org.openefms.casemanagement.service;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.openefms.casemanagement.dto.CaseActionResponse;
import org.openefms.casemanagement.dto.CaseAssignmentRequest;
import org.openefms.casemanagement.dto.CaseStatusUpdateRequest;
import org.openefms.casemanagement.entity.CaseActionRecord;
import org.openefms.casemanagement.entity.CaseRecord;
import org.openefms.casemanagement.enums.CaseActionType;
import org.openefms.casemanagement.enums.CaseAssignmentPurpose;
import org.openefms.casemanagement.enums.CaseStatus;
import org.openefms.casemanagement.repository.CaseWorkflowRepository;
import org.springframework.stereotype.Service;

@Service
public class CaseLifecycleService {
    private final CaseWorkflowRepository repository;
    private final CaseAssignmentDirectory assignmentDirectory;
    private final Clock clock;

    public CaseLifecycleService(
            CaseWorkflowRepository repository,
            CaseAssignmentDirectory assignmentDirectory,
            Clock clock) {
        this.repository = repository;
        this.assignmentDirectory = assignmentDirectory;
        this.clock = clock;
    }

    public CaseActionResponse assign(UUID caseId, CaseAssignmentRequest request) {
        CaseRecord current = caseFile(caseId);
        String assigneeId = requiredText(request == null ? null : request.assigneeId(), "assigneeId");
        String actorId = requiredText(request == null ? null : request.actorId(), "actorId");
        CaseAssignmentPurpose purpose = request == null || request.purpose() == null
                ? CaseAssignmentPurpose.ASSIGNMENT
                : request.purpose();
        assertMutable(current);
        assignmentDirectory.assertAssignable(current.tenantId(), assigneeId, purpose);

        OffsetDateTime occurredAt = OffsetDateTime.now(clock);
        CaseStatus nextStatus = nextAssignmentStatus(current.status(), purpose);
        CaseRecord updatedCase = withLifecycle(
                current,
                nextStatus,
                assigneeId,
                occurredAt,
                occurredAt,
                null);
        CaseActionRecord action = new CaseActionRecord(
                UUID.randomUUID(),
                current.caseId(),
                current.tenantId(),
                CaseActionType.ASSIGNED,
                actorId,
                note(request == null ? null : request.note()),
                current.status(),
                nextStatus,
                assigneeId,
                occurredAt,
                actionAttributes(request == null ? null : request.attributes(), purpose));

        return new CaseActionResponse(updatedCase, repository.saveCaseAction(updatedCase, action));
    }

    public CaseActionResponse updateStatus(UUID caseId, CaseStatusUpdateRequest request) {
        CaseRecord current = caseFile(caseId);
        CaseStatus requestedStatus = request == null ? null : request.status();
        String actorId = requiredText(request == null ? null : request.actorId(), "actorId");
        assertMutable(current);
        validateStatusChange(current, requestedStatus);

        OffsetDateTime occurredAt = OffsetDateTime.now(clock);
        OffsetDateTime closedAt = requestedStatus.terminal() ? occurredAt : null;
        CaseRecord updatedCase = withLifecycle(
                current,
                requestedStatus,
                current.assigneeId(),
                current.assignedAt(),
                occurredAt,
                closedAt);
        CaseActionRecord action = new CaseActionRecord(
                UUID.randomUUID(),
                current.caseId(),
                current.tenantId(),
                CaseActionType.STATUS_CHANGED,
                actorId,
                note(request == null ? null : request.note()),
                current.status(),
                requestedStatus,
                current.assigneeId(),
                occurredAt,
                attributes(request == null ? null : request.attributes()));

        return new CaseActionResponse(updatedCase, repository.saveCaseAction(updatedCase, action));
    }

    public List<CaseActionRecord> history(UUID caseId) {
        caseFile(caseId);
        return repository.findCaseActionsByCaseId(caseId);
    }

    private CaseRecord caseFile(UUID caseId) {
        return repository.findCaseById(caseId)
                .orElseThrow(() -> new CaseWorkflowRecordNotFoundException("Case", caseId));
    }

    private void assertMutable(CaseRecord caseFile) {
        if (caseFile.status().terminal()) {
            throw new InvalidCaseLifecycleActionException(List.of("Case already has a terminal status."));
        }
    }

    private void validateStatusChange(CaseRecord current, CaseStatus requestedStatus) {
        List<String> errors = new ArrayList<>();
        if (requestedStatus == null) {
            errors.add("status must not be null");
        } else {
            if (requestedStatus == CaseStatus.OPEN && current.status() != CaseStatus.OPEN) {
                errors.add("status cannot move back to OPEN");
            }
            if (requestedStatus == current.status()) {
                errors.add("status must change");
            }
        }

        if (!errors.isEmpty()) {
            throw new InvalidCaseLifecycleActionException(errors);
        }
    }

    private String requiredText(String value, String fieldName) {
        String normalised = value == null ? "" : value.trim();
        if (normalised.isBlank()) {
            throw new InvalidCaseLifecycleActionException(List.of(fieldName + " must not be blank"));
        }
        return normalised;
    }

    private String note(String value) {
        return value == null ? "" : value.trim();
    }

    private Map<String, String> attributes(Map<String, String> source) {
        if (source == null || source.isEmpty()) {
            return Map.of();
        }

        Map<String, String> attributes = new LinkedHashMap<>();
        source.forEach((key, value) -> {
            if (key != null && value != null) {
                attributes.put(key, value);
            }
        });
        return attributes;
    }

    private Map<String, String> actionAttributes(Map<String, String> source, CaseAssignmentPurpose purpose) {
        Map<String, String> attributes = new LinkedHashMap<>(attributes(source));
        attributes.put("purpose", purpose.name());
        return Map.copyOf(attributes);
    }

    private CaseStatus nextAssignmentStatus(CaseStatus currentStatus, CaseAssignmentPurpose purpose) {
        if (purpose == CaseAssignmentPurpose.ESCALATION) {
            return CaseStatus.ESCALATED;
        }
        return currentStatus == CaseStatus.OPEN ? CaseStatus.IN_REVIEW : currentStatus;
    }

    private CaseRecord withLifecycle(
            CaseRecord current,
            CaseStatus status,
            String assigneeId,
            OffsetDateTime assignedAt,
            OffsetDateTime updatedAt,
            OffsetDateTime closedAt) {
        return new CaseRecord(
                current.caseId(),
                current.alertId(),
                current.transactionId(),
                current.decisionId(),
                current.tenantId(),
                current.priority(),
                status,
                current.title(),
                current.summary(),
                current.createdAt(),
                assigneeId,
                assignedAt,
                updatedAt,
                closedAt,
                current.eventType(),
                current.eventId(),
                current.correlationId(),
                current.causationId(),
                current.traceId(),
                current.attributes());
    }
}
