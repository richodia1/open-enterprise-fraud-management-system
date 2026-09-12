package org.openefms.casemanagement.workflow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.openefms.common.models.alert.AlertPriority;
import org.openefms.common.models.casefile.CasePriority;
import org.openefms.common.models.fraud.RiskBand;

class CaseWorkflowQueryServiceTest {
    private static final UUID ALERT_ID = UUID.fromString("6cbdb449-76d2-41e1-a57d-4617ac13f996");
    private static final UUID CASE_ID = UUID.fromString("c1c522e6-6f99-46a4-9795-96971ba894ff");
    private static final UUID DECISION_ID = UUID.fromString("325dc0f5-775d-4858-89be-1992e52fe2f0");
    private static final UUID TRANSACTION_ID = UUID.fromString("118f3bd4-b208-44ff-b1ff-447c78d80d02");

    @Test
    void readsCasesAndAlertsForTenant() {
        CapturingCaseWorkflowRepository repository = new CapturingCaseWorkflowRepository();
        repository.save(alertRecord(), caseRecord());
        CaseWorkflowQueryService queryService = new CaseWorkflowQueryService(repository);

        List<AlertRecord> alerts = queryService.alertsForTenant(" tenant-a ");
        List<CaseRecord> cases = queryService.casesForTenant("tenant-a");

        assertEquals(1, alerts.size());
        assertEquals(ALERT_ID, queryService.alert(ALERT_ID).alertId());
        assertEquals(1, cases.size());
        assertEquals(CASE_ID, queryService.caseFile(CASE_ID).caseId());
    }

    @Test
    void rejectsBlankTenantQueries() {
        CaseWorkflowQueryService queryService = new CaseWorkflowQueryService(new CapturingCaseWorkflowRepository());

        assertThrows(InvalidCaseWorkflowQueryException.class, () -> queryService.alertsForTenant(" "));
        assertThrows(InvalidCaseWorkflowQueryException.class, () -> queryService.casesForTenant(null));
    }

    @Test
    void reportsMissingRecords() {
        CaseWorkflowQueryService queryService = new CaseWorkflowQueryService(new CapturingCaseWorkflowRepository());

        assertThrows(CaseWorkflowRecordNotFoundException.class, () -> queryService.alert(ALERT_ID));
        assertThrows(CaseWorkflowRecordNotFoundException.class, () -> queryService.caseFile(CASE_ID));
    }

    private AlertRecord alertRecord() {
        return new AlertRecord(
                ALERT_ID,
                DECISION_ID,
                TRANSACTION_ID,
                "tenant-a",
                AlertPriority.MEDIUM,
                RiskBand.MEDIUM,
                AlertStatus.OPEN,
                "Review alert",
                "Review the transaction.",
                OffsetDateTime.parse("2026-09-12T10:00:00Z"),
                "alert.created",
                UUID.fromString("e891362c-cf25-4e25-93ce-5031f34d0a30"),
                "correlation-1",
                "causation-1",
                "trace-1",
                Map.of());
    }

    private CaseRecord caseRecord() {
        return new CaseRecord(
                CASE_ID,
                ALERT_ID,
                TRANSACTION_ID,
                DECISION_ID,
                "tenant-a",
                CasePriority.MEDIUM,
                CaseStatus.OPEN,
                "Review case",
                "Investigate the transaction.",
                OffsetDateTime.parse("2026-09-12T10:00:00Z"),
                "case.created",
                UUID.fromString("44b52b93-8baa-47d0-8177-219314f1cf76"),
                "correlation-1",
                "causation-1",
                "trace-1",
                Map.of());
    }

    private static final class CapturingCaseWorkflowRepository implements CaseWorkflowRepository {
        private final List<AlertRecord> alerts = new ArrayList<>();
        private final List<CaseRecord> cases = new ArrayList<>();

        @Override
        public CaseWorkflowRecord save(AlertRecord alert, CaseRecord caseFile) {
            alerts.add(alert);
            cases.add(caseFile);
            return new CaseWorkflowRecord(alert, caseFile);
        }

        @Override
        public Optional<AlertRecord> findAlertById(UUID alertId) {
            return alerts.stream()
                    .filter(alert -> alert.alertId().equals(alertId))
                    .findFirst();
        }

        @Override
        public List<AlertRecord> findAlertsByTenantId(String tenantId) {
            return alerts.stream()
                    .filter(alert -> alert.tenantId().equals(tenantId))
                    .toList();
        }

        @Override
        public Optional<CaseRecord> findCaseById(UUID caseId) {
            return cases.stream()
                    .filter(caseFile -> caseFile.caseId().equals(caseId))
                    .findFirst();
        }

        @Override
        public List<CaseRecord> findCasesByTenantId(String tenantId) {
            return cases.stream()
                    .filter(caseFile -> caseFile.tenantId().equals(tenantId))
                    .toList();
        }
    }
}
