package org.openefms.casemanagement.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
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
import org.openefms.casemanagement.entity.AlertRecord;
import org.openefms.casemanagement.entity.CaseActionRecord;
import org.openefms.casemanagement.entity.CaseRecord;
import org.openefms.casemanagement.entity.CaseWorkflowRecord;
import org.openefms.casemanagement.enums.AlertStatus;
import org.openefms.casemanagement.enums.CaseStatus;
import org.openefms.casemanagement.messaging.CaseManagementRabbitMqProperties;
import org.openefms.casemanagement.messaging.DomainEventMessage;
import org.openefms.casemanagement.repository.CaseWorkflowRecordMapper;
import org.openefms.casemanagement.repository.CaseWorkflowRepository;
import org.openefms.common.events.EventEnvelope;
import org.openefms.common.events.EventMetadata;
import org.openefms.common.events.EventTypes;
import org.openefms.common.events.alert.AlertCreatedEvent;
import org.openefms.common.events.audit.AuditEventRecordedEvent;
import org.openefms.common.events.casefile.CaseCreatedEvent;
import org.openefms.common.events.explanation.ExplanationPreparedEvent;
import org.openefms.common.events.rabbitmq.RabbitMqExchanges;
import org.openefms.common.events.rabbitmq.RabbitMqRoutingKeys;
import org.openefms.common.models.alert.AlertPriority;
import org.openefms.common.models.audit.AuditAction;
import org.openefms.common.models.casefile.CasePriority;
import org.openefms.common.models.explanation.EvidenceItem;
import org.openefms.common.models.explanation.ExplanationResponse;

class CaseWorkflowOrchestratorTest {
    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-09-11T11:00:00Z"), ZoneOffset.UTC);
    private static final UUID SOURCE_EVENT_ID = UUID.fromString("a638bf8a-2c9c-4e43-aae3-caf77d8497ac");
    private static final UUID EXPLANATION_ID = UUID.fromString("d88154d9-5246-43ec-bb65-fcdfd38f64a3");
    private static final UUID DECISION_ID = UUID.fromString("325dc0f5-775d-4858-89be-1992e52fe2f0");
    private static final UUID TRANSACTION_ID = UUID.fromString("118f3bd4-b208-44ff-b1ff-447c78d80d02");

    @Test
    void preparesAlertCaseAndAuditMessagesForReviewableExplanation() {
        CapturingCaseWorkflowRepository repository = new CapturingCaseWorkflowRepository();
        CaseWorkflowOrchestrator orchestrator = orchestrator(repository);

        CaseWorkflowMessages workflowMessages = orchestrator.prepare(explanationEvent("REVIEW", "MEDIUM"));

        assertEquals(4, workflowMessages.messages().size());

        DomainEventMessage<?> alertMessage = message(workflowMessages, RabbitMqRoutingKeys.ALERT_CREATED);
        AlertCreatedEvent alertCreated = (AlertCreatedEvent) alertMessage.envelope().payload();
        assertEquals(AlertPriority.MEDIUM, alertCreated.alert().priority());
        assertEquals(DECISION_ID, alertCreated.alert().decisionId());
        assertEquals(TRANSACTION_ID, alertCreated.alert().transactionId());
        assertEquals("HOLD_FOR_MANUAL_REVIEW", alertCreated.alert().attributes().get("recommendedAction"));
        assertEquals(SOURCE_EVENT_ID.toString(), alertMessage.envelope().metadata().causationId());

        DomainEventMessage<?> caseMessage = message(workflowMessages, RabbitMqRoutingKeys.CASE_CREATED);
        CaseCreatedEvent caseCreated = (CaseCreatedEvent) caseMessage.envelope().payload();
        assertEquals(CasePriority.MEDIUM, caseCreated.caseFile().priority());
        assertEquals(alertCreated.alert().alertId(), caseCreated.caseFile().alertId());
        assertEquals("HOLD_FOR_MANUAL_REVIEW", caseCreated.caseFile().attributes().get("recommendedAction"));
        assertEquals(alertMessage.envelope().metadata().eventId().toString(), caseMessage.envelope().metadata().causationId());

        List<AuditEventRecordedEvent> auditEvents = workflowMessages.messages().stream()
                .filter(message -> RabbitMqRoutingKeys.AUDIT_EVENT_RECORDED.equals(message.routingKey()))
                .map(message -> (AuditEventRecordedEvent) message.envelope().payload())
                .toList();
        assertEquals(2, auditEvents.size());
        assertTrue(auditEvents.stream().anyMatch(event -> event.auditEvent().action() == AuditAction.ALERT_CREATED));
        assertTrue(auditEvents.stream().anyMatch(event -> event.auditEvent().action() == AuditAction.CASE_CREATED));
        assertTrue(auditEvents.stream()
                .allMatch(event -> "HOLD_FOR_MANUAL_REVIEW".equals(event.auditEvent().attributes().get("recommendedAction"))));

        assertEquals(1, repository.alerts.size());
        AlertRecord alertRecord = repository.alerts.getFirst();
        assertEquals(alertCreated.alert().alertId(), alertRecord.alertId());
        assertEquals(AlertStatus.OPEN, alertRecord.status());
        assertEquals(EventTypes.ALERT_CREATED, alertRecord.eventType());
        assertEquals(alertMessage.envelope().metadata().eventId(), alertRecord.eventId());
        assertEquals("correlation-1", alertRecord.correlationId());

        assertEquals(1, repository.cases.size());
        CaseRecord caseRecord = repository.cases.getFirst();
        assertEquals(caseCreated.caseFile().caseId(), caseRecord.caseId());
        assertEquals(alertRecord.alertId(), caseRecord.alertId());
        assertEquals(CaseStatus.OPEN, caseRecord.status());
        assertEquals(EventTypes.CASE_CREATED, caseRecord.eventType());
        assertEquals(caseMessage.envelope().metadata().eventId(), caseRecord.eventId());
    }

    @Test
    void doesNotOpenCaseForLowRiskApproval() {
        CapturingCaseWorkflowRepository repository = new CapturingCaseWorkflowRepository();
        CaseWorkflowOrchestrator orchestrator = orchestrator(repository);

        CaseWorkflowMessages workflowMessages = orchestrator.prepare(explanationEvent("APPROVE", "LOW"));

        assertTrue(workflowMessages.messages().isEmpty());
        assertTrue(repository.alerts.isEmpty());
        assertTrue(repository.cases.isEmpty());
    }

    private CaseWorkflowOrchestrator orchestrator() {
        return orchestrator(new CapturingCaseWorkflowRepository());
    }

    private CaseWorkflowOrchestrator orchestrator(CaseWorkflowRepository repository) {
        return new CaseWorkflowOrchestrator(
                new CaseManagementRabbitMqProperties(RabbitMqExchanges.DOMAIN_EVENTS, RabbitMqExchanges.DEAD_LETTER),
                repository,
                new CaseWorkflowRecordMapper(),
                FIXED_CLOCK);
    }

    private DomainEventMessage<?> message(CaseWorkflowMessages workflowMessages, String routingKey) {
        return workflowMessages.messages().stream()
                .filter(message -> routingKey.equals(message.routingKey()))
                .findFirst()
                .orElseThrow();
    }

    private EventEnvelope<ExplanationPreparedEvent> explanationEvent(String outcome, String riskBand) {
        return new EventEnvelope<>(
                new EventMetadata(
                        SOURCE_EVENT_ID,
                        EventTypes.EXPLANATION_PREPARED,
                        1,
                        OffsetDateTime.parse("2026-09-11T10:00:01Z"),
                        "explainable-ai-service",
                        "tenant-a",
                        "correlation-1",
                        "fb40ff28-86cc-4b6c-a6df-b538d5155d2e",
                        "trace-1",
                        Map.of()),
                new ExplanationPreparedEvent(explanation(outcome, riskBand)));
    }

    private ExplanationResponse explanation(String outcome, String riskBand) {
        return new ExplanationResponse(
                EXPLANATION_ID,
                DECISION_ID,
                TRANSACTION_ID,
                "tenant-a",
                "Decision " + outcome + " with " + riskBand + " risk score 0.60. Key evidence: High value amount.",
                List.of(new EvidenceItem(
                        "RULE",
                        "High value amount",
                        "The amount exceeded the high-value review threshold.",
                        new BigDecimal("0.25"),
                        Map.of("ruleCode", "HIGH_VALUE_AMOUNT"))),
                OffsetDateTime.parse("2026-09-11T10:00:00Z"),
                Map.of(
                        "decisionOutcome", outcome,
                        "recommendedAction", "HOLD_FOR_MANUAL_REVIEW",
                        "riskBand", riskBand,
                        "riskScore", "0.60"));
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

        @Override
        public CaseActionRecord saveCaseAction(CaseRecord caseFile, CaseActionRecord action) {
            return action;
        }

        @Override
        public List<CaseActionRecord> findCaseActionsByCaseId(UUID caseId) {
            return List.of();
        }
    }
}
