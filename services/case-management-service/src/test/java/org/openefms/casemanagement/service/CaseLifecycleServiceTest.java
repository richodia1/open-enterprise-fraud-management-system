package org.openefms.casemanagement.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.openefms.casemanagement.dto.CaseAssignableUser;
import org.openefms.casemanagement.dto.CaseAssignmentRequest;
import org.openefms.casemanagement.dto.CaseStatusUpdateRequest;
import org.openefms.casemanagement.entity.AlertRecord;
import org.openefms.casemanagement.entity.CaseActionRecord;
import org.openefms.casemanagement.entity.CaseRecord;
import org.openefms.casemanagement.entity.CaseWorkflowRecord;
import org.openefms.casemanagement.enums.CaseActionType;
import org.openefms.casemanagement.enums.CaseStatus;
import org.openefms.casemanagement.repository.CaseWorkflowRepository;
import org.openefms.common.models.casefile.CasePriority;

class CaseLifecycleServiceTest {
    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-09-13T09:00:00Z"), ZoneOffset.UTC);
    private static final UUID ALERT_ID = UUID.fromString("6cbdb449-76d2-41e1-a57d-4617ac13f996");
    private static final UUID CASE_ID = UUID.fromString("c1c522e6-6f99-46a4-9795-96971ba894ff");
    private static final UUID DECISION_ID = UUID.fromString("325dc0f5-775d-4858-89be-1992e52fe2f0");
    private static final UUID TRANSACTION_ID = UUID.fromString("118f3bd4-b208-44ff-b1ff-447c78d80d02");

    @Test
    void assignsOpenCaseAndMovesItIntoReview() {
        CapturingCaseWorkflowRepository repository = new CapturingCaseWorkflowRepository(caseRecord(CaseStatus.OPEN));
        CaseLifecycleService service = new CaseLifecycleService(repository, assigneeDirectory(), FIXED_CLOCK);

        var response = service.assign(CASE_ID, new CaseAssignmentRequest(
                " investigator-1 ",
                " lead-analyst ",
                " Taking ownership ",
                Map.of("queue", "manual-review")));

        assertEquals(CaseStatus.IN_REVIEW, response.caseFile().status());
        assertEquals("investigator-1", response.caseFile().assigneeId());
        assertEquals(now(), response.caseFile().assignedAt());
        assertEquals(now(), response.caseFile().updatedAt());
        assertEquals(CaseActionType.ASSIGNED, response.action().actionType());
        assertEquals(CaseStatus.OPEN, response.action().fromStatus());
        assertEquals(CaseStatus.IN_REVIEW, response.action().toStatus());
        assertEquals("lead-analyst", response.action().actorId());
        assertEquals("Taking ownership", response.action().note());
        assertEquals("manual-review", response.action().attributes().get("queue"));
    }

    @Test
    void recordsFinalFraudDispositionAndClosesCase() {
        CapturingCaseWorkflowRepository repository = new CapturingCaseWorkflowRepository(
                caseRecord(CaseStatus.IN_REVIEW, "investigator-1", now().minusHours(1), now().minusHours(1), null));
        CaseLifecycleService service = new CaseLifecycleService(repository, assigneeDirectory(), FIXED_CLOCK);

        var response = service.updateStatus(CASE_ID, new CaseStatusUpdateRequest(
                CaseStatus.FRAUD_CONFIRMED,
                " investigator-1 ",
                "Confirmed mule-account pattern.",
                Map.of("decision", "fraud")));

        assertEquals(CaseStatus.FRAUD_CONFIRMED, response.caseFile().status());
        assertEquals("investigator-1", response.caseFile().assigneeId());
        assertEquals(now(), response.caseFile().closedAt());
        assertEquals(CaseActionType.STATUS_CHANGED, response.action().actionType());
        assertEquals(CaseStatus.IN_REVIEW, response.action().fromStatus());
        assertEquals(CaseStatus.FRAUD_CONFIRMED, response.action().toStatus());
        assertEquals("fraud", response.action().attributes().get("decision"));
        assertEquals(1, service.history(CASE_ID).size());
    }

    @Test
    void rejectsBlankActorsAndTerminalCases() {
        CaseLifecycleService openCaseService =
                new CaseLifecycleService(
                        new CapturingCaseWorkflowRepository(caseRecord(CaseStatus.OPEN)),
                        assigneeDirectory(),
                        FIXED_CLOCK);

        assertThrows(InvalidCaseLifecycleActionException.class, () -> openCaseService.assign(
                CASE_ID,
                new CaseAssignmentRequest("", "lead-analyst", "", Map.of())));
        assertThrows(InvalidCaseLifecycleActionException.class, () -> openCaseService.updateStatus(
                CASE_ID,
                new CaseStatusUpdateRequest(CaseStatus.OPEN, "", "", Map.of())));

        CaseLifecycleService closedCaseService = new CaseLifecycleService(
                new CapturingCaseWorkflowRepository(caseRecord(
                        CaseStatus.FALSE_POSITIVE,
                        "investigator-1",
                        now().minusHours(1),
                        now().minusMinutes(10),
                        now().minusMinutes(10))),
                assigneeDirectory(),
                FIXED_CLOCK);

        assertThrows(InvalidCaseLifecycleActionException.class, () -> closedCaseService.updateStatus(
                CASE_ID,
                new CaseStatusUpdateRequest(CaseStatus.IN_REVIEW, "lead-analyst", "", Map.of())));
    }

    @Test
    void reportsMissingCaseBeforeReadingHistory() {
        CaseLifecycleService service = new CaseLifecycleService(
                new CapturingCaseWorkflowRepository(null),
                assigneeDirectory(),
                FIXED_CLOCK);

        assertThrows(CaseWorkflowRecordNotFoundException.class, () -> service.history(CASE_ID));
    }

    @Test
    void rejectsAssignmentWhenIdentityDirectoryDoesNotContainAssignee() {
        CaseLifecycleService service = new CaseLifecycleService(
                new CapturingCaseWorkflowRepository(caseRecord(CaseStatus.OPEN)),
                new StaticCaseAssignmentDirectory(List.of()),
                FIXED_CLOCK);

        assertThrows(InvalidCaseLifecycleActionException.class, () -> service.assign(
                CASE_ID,
                new CaseAssignmentRequest("unknown-user", "lead-analyst", "", Map.of())));
    }

    private OffsetDateTime now() {
        return OffsetDateTime.now(FIXED_CLOCK);
    }

    private CaseRecord caseRecord(CaseStatus status) {
        return caseRecord(status, null, null, OffsetDateTime.parse("2026-09-12T10:00:00Z"), null);
    }

    private CaseRecord caseRecord(
            CaseStatus status,
            String assigneeId,
            OffsetDateTime assignedAt,
            OffsetDateTime updatedAt,
            OffsetDateTime closedAt) {
        return new CaseRecord(
                CASE_ID,
                ALERT_ID,
                TRANSACTION_ID,
                DECISION_ID,
                "tenant-a",
                CasePriority.HIGH,
                status,
                "Review case",
                "Investigate the transaction.",
                OffsetDateTime.parse("2026-09-12T10:00:00Z"),
                assigneeId,
                assignedAt,
                updatedAt,
                closedAt,
                "case.created",
                UUID.fromString("44b52b93-8baa-47d0-8177-219314f1cf76"),
                "correlation-1",
                "causation-1",
                "trace-1",
                Map.of("riskBand", "HIGH"));
    }

    private CaseAssignmentDirectory assigneeDirectory() {
        return new StaticCaseAssignmentDirectory(List.of(new CaseAssignableUser(
                "investigator-1",
                "tenant-a",
                "investigator-1",
                "Fraud Investigator One",
                "investigator1@example.com",
                List.of("FRAUD_ANALYST"),
                true,
                Map.of())));
    }

    private record StaticCaseAssignmentDirectory(List<CaseAssignableUser> users) implements CaseAssignmentDirectory {
        @Override
        public List<CaseAssignableUser> listAssignableUsers(String tenantId, String filterText) {
            return users;
        }
    }

    private static final class CapturingCaseWorkflowRepository implements CaseWorkflowRepository {
        private final List<CaseActionRecord> actions = new ArrayList<>();
        private CaseRecord caseFile;

        private CapturingCaseWorkflowRepository(CaseRecord caseFile) {
            this.caseFile = caseFile;
        }

        @Override
        public CaseWorkflowRecord save(AlertRecord alert, CaseRecord caseFile) {
            this.caseFile = caseFile;
            return new CaseWorkflowRecord(alert, caseFile);
        }

        @Override
        public Optional<AlertRecord> findAlertById(UUID alertId) {
            return Optional.empty();
        }

        @Override
        public List<AlertRecord> findAlertsByTenantId(String tenantId) {
            return List.of();
        }

        @Override
        public Optional<CaseRecord> findCaseById(UUID caseId) {
            return Optional.ofNullable(caseFile)
                    .filter(record -> record.caseId().equals(caseId));
        }

        @Override
        public List<CaseRecord> findCasesByTenantId(String tenantId) {
            return Optional.ofNullable(caseFile)
                    .filter(record -> record.tenantId().equals(tenantId))
                    .map(record -> List.of(record))
                    .orElseGet(List::of);
        }

        @Override
        public CaseActionRecord saveCaseAction(CaseRecord caseFile, CaseActionRecord action) {
            this.caseFile = caseFile;
            actions.add(action);
            return action;
        }

        @Override
        public List<CaseActionRecord> findCaseActionsByCaseId(UUID caseId) {
            assertTrue(caseFile == null || caseFile.caseId().equals(caseId));
            return actions.stream()
                    .filter(action -> action.caseId().equals(caseId))
                    .toList();
        }
    }
}
