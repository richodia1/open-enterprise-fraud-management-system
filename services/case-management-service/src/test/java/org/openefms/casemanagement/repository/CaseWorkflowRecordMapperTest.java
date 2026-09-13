package org.openefms.casemanagement.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.openefms.casemanagement.entity.AlertRecord;
import org.openefms.casemanagement.entity.CaseRecord;
import org.openefms.casemanagement.enums.AlertStatus;
import org.openefms.casemanagement.enums.CaseStatus;
import org.openefms.common.models.alert.AlertPriority;
import org.openefms.common.models.casefile.CasePriority;
import org.openefms.common.models.fraud.RiskBand;

class CaseWorkflowRecordMapperTest {
    private static final UUID ALERT_ID = UUID.fromString("6cbdb449-76d2-41e1-a57d-4617ac13f996");
    private static final UUID CASE_ID = UUID.fromString("c1c522e6-6f99-46a4-9795-96971ba894ff");
    private static final UUID DECISION_ID = UUID.fromString("325dc0f5-775d-4858-89be-1992e52fe2f0");
    private static final UUID TRANSACTION_ID = UUID.fromString("118f3bd4-b208-44ff-b1ff-447c78d80d02");
    private static final UUID EVENT_ID = UUID.fromString("e891362c-cf25-4e25-93ce-5031f34d0a30");

    private final CaseWorkflowRecordMapper mapper = new CaseWorkflowRecordMapper();

    @Test
    void mapsAlertRecordThroughEntity() {
        AlertRecord source = alertRecord();

        AlertRecord roundTripped = mapper.toRecord(mapper.toEntity(source));

        assertEquals(source.alertId(), roundTripped.alertId());
        assertEquals(source.decisionId(), roundTripped.decisionId());
        assertEquals(source.transactionId(), roundTripped.transactionId());
        assertEquals(source.tenantId(), roundTripped.tenantId());
        assertEquals(source.priority(), roundTripped.priority());
        assertEquals(source.riskBand(), roundTripped.riskBand());
        assertEquals(source.status(), roundTripped.status());
        assertEquals(source.eventId(), roundTripped.eventId());
        assertEquals(source.attributes(), roundTripped.attributes());
    }

    @Test
    void mapsCaseRecordThroughEntity() {
        CaseRecord source = caseRecord();

        CaseRecord roundTripped = mapper.toRecord(mapper.toEntity(source));

        assertEquals(source.caseId(), roundTripped.caseId());
        assertEquals(source.alertId(), roundTripped.alertId());
        assertEquals(source.decisionId(), roundTripped.decisionId());
        assertEquals(source.transactionId(), roundTripped.transactionId());
        assertEquals(source.tenantId(), roundTripped.tenantId());
        assertEquals(source.priority(), roundTripped.priority());
        assertEquals(source.status(), roundTripped.status());
        assertEquals(source.eventId(), roundTripped.eventId());
        assertEquals(source.attributes(), roundTripped.attributes());
    }

    private AlertRecord alertRecord() {
        return new AlertRecord(
                ALERT_ID,
                DECISION_ID,
                TRANSACTION_ID,
                "tenant-a",
                AlertPriority.HIGH,
                RiskBand.HIGH,
                AlertStatus.OPEN,
                "Review alert",
                "Review the transaction.",
                OffsetDateTime.parse("2026-09-12T10:00:00Z"),
                "alert.created",
                EVENT_ID,
                "correlation-1",
                "causation-1",
                "trace-1",
                Map.of("riskScore", "0.80", "decisionOutcome", "BLOCK"));
    }

    private CaseRecord caseRecord() {
        return new CaseRecord(
                CASE_ID,
                ALERT_ID,
                TRANSACTION_ID,
                DECISION_ID,
                "tenant-a",
                CasePriority.HIGH,
                CaseStatus.OPEN,
                "Review case",
                "Investigate the transaction.",
                OffsetDateTime.parse("2026-09-12T10:00:00Z"),
                "case.created",
                EVENT_ID,
                "correlation-1",
                "causation-1",
                "trace-1",
                Map.of("riskBand", "HIGH", "decisionOutcome", "BLOCK"));
    }
}
