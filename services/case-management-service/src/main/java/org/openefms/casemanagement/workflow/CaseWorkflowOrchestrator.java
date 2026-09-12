package org.openefms.casemanagement.workflow;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.openefms.casemanagement.messaging.CaseManagementRabbitMqProperties;
import org.openefms.common.events.EventEnvelope;
import org.openefms.common.events.EventMetadata;
import org.openefms.common.events.EventTypes;
import org.openefms.common.events.alert.AlertCreatedEvent;
import org.openefms.common.events.audit.AuditEventRecordedEvent;
import org.openefms.common.events.casefile.CaseCreatedEvent;
import org.openefms.common.events.explanation.ExplanationPreparedEvent;
import org.openefms.common.events.rabbitmq.RabbitMqTopology;
import org.openefms.common.models.alert.AlertCreationRequest;
import org.openefms.common.models.alert.AlertPriority;
import org.openefms.common.models.audit.AuditAction;
import org.openefms.common.models.audit.AuditEventRecord;
import org.openefms.common.models.casefile.CaseCreationRequest;
import org.openefms.common.models.casefile.CasePriority;
import org.openefms.common.models.explanation.ExplanationResponse;
import org.openefms.common.models.fraud.RiskBand;
import org.springframework.stereotype.Service;

@Service
class CaseWorkflowOrchestrator {
    private static final int EVENT_VERSION = 1;
    private static final String SOURCE_SERVICE = "case-management-service";
    private static final String SYSTEM_ACTOR = "system:case-management-service";

    private final CaseManagementRabbitMqProperties rabbitMqProperties;
    private final CaseWorkflowRepository workflowRepository;
    private final CaseWorkflowRecordMapper recordMapper;
    private final Clock clock;

    CaseWorkflowOrchestrator(
            CaseManagementRabbitMqProperties rabbitMqProperties,
            CaseWorkflowRepository workflowRepository,
            CaseWorkflowRecordMapper recordMapper,
            Clock clock) {
        this.rabbitMqProperties = rabbitMqProperties;
        this.workflowRepository = workflowRepository;
        this.recordMapper = recordMapper;
        this.clock = clock;
    }

    CaseWorkflowMessages prepare(EventEnvelope<ExplanationPreparedEvent> event) {
        ExplanationResponse explanation = event.payload().explanation();
        if (!requiresInvestigation(explanation)) {
            return CaseWorkflowMessages.empty();
        }

        OffsetDateTime createdAt = OffsetDateTime.now(clock);
        RiskBand riskBand = riskBand(explanation);
        AlertPriority alertPriority = alertPriority(riskBand);
        CasePriority casePriority = casePriority(alertPriority);
        UUID alertId = UUID.randomUUID();
        UUID caseId = UUID.randomUUID();

        AlertCreationRequest alert = alert(explanation, alertId, alertPriority, riskBand, createdAt);
        CaseCreationRequest caseFile = caseFile(explanation, caseId, alertId, casePriority, createdAt);
        DomainEventMessage<AlertCreatedEvent> alertMessage = alertMessage(alert, event.metadata(), createdAt);
        DomainEventMessage<CaseCreatedEvent> caseMessage =
                caseMessage(caseFile, event.metadata(), alertMessage.envelope().metadata().eventId(), createdAt);
        DomainEventMessage<AuditEventRecordedEvent> alertAuditMessage =
                auditMessage(alertAudit(alert, caseId, createdAt), alertMessage.envelope().metadata(), createdAt);
        DomainEventMessage<AuditEventRecordedEvent> caseAuditMessage =
                auditMessage(caseAudit(caseFile, createdAt), caseMessage.envelope().metadata(), createdAt);

        workflowRepository.save(
                recordMapper.fromAlert(alert, alertMessage.envelope().metadata()),
                recordMapper.fromCase(caseFile, caseMessage.envelope().metadata()));

        return new CaseWorkflowMessages(List.of(alertMessage, caseMessage, alertAuditMessage, caseAuditMessage));
    }

    private boolean requiresInvestigation(ExplanationResponse explanation) {
        String outcome = explanation.attributes().getOrDefault("decisionOutcome", "");
        RiskBand riskBand = riskBand(explanation);
        return !"APPROVE".equalsIgnoreCase(outcome) || riskBand != RiskBand.LOW;
    }

    private AlertCreationRequest alert(
            ExplanationResponse explanation,
            UUID alertId,
            AlertPriority priority,
            RiskBand riskBand,
            OffsetDateTime createdAt) {
        Map<String, String> attributes = new LinkedHashMap<>();
        attributes.put("explanationId", explanation.explanationId().toString());
        attributes.put("decisionOutcome", explanation.attributes().getOrDefault("decisionOutcome", "UNKNOWN"));
        attributes.put("riskScore", explanation.attributes().getOrDefault("riskScore", "0.00"));
        attributes.put("evidenceCount", Integer.toString(explanation.evidence().size()));

        return new AlertCreationRequest(
                alertId,
                explanation.decisionId(),
                explanation.transactionId(),
                explanation.tenantId(),
                priority,
                riskBand,
                "Fraud review alert for transaction " + shortId(explanation.transactionId()),
                explanation.summary(),
                createdAt,
                attributes);
    }

    private CaseCreationRequest caseFile(
            ExplanationResponse explanation,
            UUID caseId,
            UUID alertId,
            CasePriority priority,
            OffsetDateTime createdAt) {
        Map<String, String> attributes = new LinkedHashMap<>();
        attributes.put("explanationId", explanation.explanationId().toString());
        attributes.put("decisionOutcome", explanation.attributes().getOrDefault("decisionOutcome", "UNKNOWN"));
        attributes.put("riskBand", explanation.attributes().getOrDefault("riskBand", "UNKNOWN"));
        attributes.put("riskScore", explanation.attributes().getOrDefault("riskScore", "0.00"));

        return new CaseCreationRequest(
                caseId,
                alertId,
                explanation.transactionId(),
                explanation.decisionId(),
                explanation.tenantId(),
                priority,
                "Fraud investigation case for transaction " + shortId(explanation.transactionId()),
                explanation.summary(),
                createdAt,
                attributes);
    }

    private DomainEventMessage<AlertCreatedEvent> alertMessage(
            AlertCreationRequest alert,
            EventMetadata sourceMetadata,
            OffsetDateTime createdAt) {
        EventEnvelope<AlertCreatedEvent> envelope = new EventEnvelope<>(
                metadata(EventTypes.ALERT_CREATED, sourceMetadata, sourceMetadata.eventId(), createdAt),
                new AlertCreatedEvent(alert));
        return new DomainEventMessage<>(
                rabbitMqProperties.exchange(),
                RabbitMqTopology.routingKeyFor(EventTypes.ALERT_CREATED),
                envelope);
    }

    private DomainEventMessage<CaseCreatedEvent> caseMessage(
            CaseCreationRequest caseFile,
            EventMetadata sourceMetadata,
            UUID causedByEventId,
            OffsetDateTime createdAt) {
        EventEnvelope<CaseCreatedEvent> envelope = new EventEnvelope<>(
                metadata(EventTypes.CASE_CREATED, sourceMetadata, causedByEventId, createdAt),
                new CaseCreatedEvent(caseFile));
        return new DomainEventMessage<>(
                rabbitMqProperties.exchange(),
                RabbitMqTopology.routingKeyFor(EventTypes.CASE_CREATED),
                envelope);
    }

    private DomainEventMessage<AuditEventRecordedEvent> auditMessage(
            AuditEventRecord auditEvent,
            EventMetadata sourceMetadata,
            OffsetDateTime createdAt) {
        EventEnvelope<AuditEventRecordedEvent> envelope = new EventEnvelope<>(
                metadata(EventTypes.AUDIT_EVENT_RECORDED, sourceMetadata, sourceMetadata.eventId(), createdAt),
                new AuditEventRecordedEvent(auditEvent));
        return new DomainEventMessage<>(
                rabbitMqProperties.exchange(),
                RabbitMqTopology.routingKeyFor(EventTypes.AUDIT_EVENT_RECORDED),
                envelope);
    }

    private AuditEventRecord alertAudit(AlertCreationRequest alert, UUID caseId, OffsetDateTime occurredAt) {
        return new AuditEventRecord(
                UUID.randomUUID(),
                alert.tenantId(),
                SYSTEM_ACTOR,
                AuditAction.ALERT_CREATED,
                "alert",
                alert.alertId().toString(),
                "Fraud alert created for transaction " + alert.transactionId(),
                occurredAt,
                Map.of(
                        "transactionId", alert.transactionId().toString(),
                        "decisionId", alert.decisionId().toString(),
                        "caseId", caseId.toString(),
                        "priority", alert.priority().name()));
    }

    private AuditEventRecord caseAudit(CaseCreationRequest caseFile, OffsetDateTime occurredAt) {
        return new AuditEventRecord(
                UUID.randomUUID(),
                caseFile.tenantId(),
                SYSTEM_ACTOR,
                AuditAction.CASE_CREATED,
                "case",
                caseFile.caseId().toString(),
                "Fraud investigation case created for transaction " + caseFile.transactionId(),
                occurredAt,
                Map.of(
                        "transactionId", caseFile.transactionId().toString(),
                        "decisionId", caseFile.decisionId().toString(),
                        "alertId", caseFile.alertId().toString(),
                        "priority", caseFile.priority().name()));
    }

    private EventMetadata metadata(
            String eventType,
            EventMetadata sourceMetadata,
            UUID causedByEventId,
            OffsetDateTime occurredAt) {
        Map<String, String> attributes = new LinkedHashMap<>();
        attributes.put("causedByEventType", sourceMetadata.eventType());

        return new EventMetadata(
                UUID.randomUUID(),
                eventType,
                EVENT_VERSION,
                occurredAt,
                SOURCE_SERVICE,
                sourceMetadata.tenantId(),
                sourceMetadata.correlationId(),
                causedByEventId.toString(),
                sourceMetadata.traceId(),
                attributes);
    }

    private RiskBand riskBand(ExplanationResponse explanation) {
        String riskBand = explanation.attributes().getOrDefault("riskBand", RiskBand.LOW.name());
        try {
            return RiskBand.valueOf(riskBand.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return RiskBand.LOW;
        }
    }

    private AlertPriority alertPriority(RiskBand riskBand) {
        return switch (riskBand) {
            case LOW -> AlertPriority.LOW;
            case MEDIUM -> AlertPriority.MEDIUM;
            case HIGH -> AlertPriority.HIGH;
            case CRITICAL -> AlertPriority.URGENT;
        };
    }

    private CasePriority casePriority(AlertPriority alertPriority) {
        return switch (alertPriority) {
            case LOW -> CasePriority.LOW;
            case MEDIUM -> CasePriority.MEDIUM;
            case HIGH -> CasePriority.HIGH;
            case URGENT -> CasePriority.URGENT;
        };
    }

    private String shortId(UUID value) {
        return value.toString().substring(0, 8);
    }
}
