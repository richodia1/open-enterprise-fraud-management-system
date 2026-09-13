package org.openefms.casemanagement.repository;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import org.openefms.casemanagement.entity.AlertRecord;
import org.openefms.casemanagement.entity.CaseRecord;
import org.openefms.casemanagement.entity.FraudAlertEntity;
import org.openefms.casemanagement.entity.FraudCaseEntity;
import org.openefms.casemanagement.enums.AlertStatus;
import org.openefms.casemanagement.enums.CaseStatus;
import org.openefms.casemanagement.service.CaseWorkflowPersistenceException;
import org.openefms.common.events.EventMetadata;
import org.openefms.common.models.alert.AlertCreationRequest;
import org.openefms.common.models.casefile.CaseCreationRequest;
import org.springframework.stereotype.Component;

@Component
public class CaseWorkflowRecordMapper {
    public AlertRecord fromAlert(AlertCreationRequest alert, EventMetadata metadata) {
        return new AlertRecord(
                alert.alertId(),
                alert.decisionId(),
                alert.transactionId(),
                alert.tenantId(),
                alert.priority(),
                alert.riskBand(),
                AlertStatus.OPEN,
                alert.title(),
                alert.summary(),
                alert.createdAt(),
                metadata.eventType(),
                metadata.eventId(),
                metadata.correlationId(),
                metadata.causationId(),
                metadata.traceId(),
                alert.attributes());
    }

    public CaseRecord fromCase(CaseCreationRequest caseFile, EventMetadata metadata) {
        return new CaseRecord(
                caseFile.caseId(),
                caseFile.alertId(),
                caseFile.transactionId(),
                caseFile.decisionId(),
                caseFile.tenantId(),
                caseFile.priority(),
                CaseStatus.OPEN,
                caseFile.title(),
                caseFile.summary(),
                caseFile.createdAt(),
                metadata.eventType(),
                metadata.eventId(),
                metadata.correlationId(),
                metadata.causationId(),
                metadata.traceId(),
                caseFile.attributes());
    }

    public FraudAlertEntity toEntity(AlertRecord record) {
        FraudAlertEntity entity = new FraudAlertEntity();
        entity.alertId(record.alertId());
        entity.decisionId(record.decisionId());
        entity.transactionId(record.transactionId());
        entity.tenantId(record.tenantId());
        entity.priority(record.priority());
        entity.riskBand(record.riskBand());
        entity.status(record.status());
        entity.title(record.title());
        entity.summary(record.summary());
        entity.createdAt(record.createdAt());
        entity.eventType(record.eventType());
        entity.eventId(record.eventId());
        entity.correlationId(record.correlationId());
        entity.causationId(record.causationId());
        entity.traceId(record.traceId());
        entity.attributesText(toText(record.attributes()));
        return entity;
    }

    public FraudCaseEntity toEntity(CaseRecord record) {
        FraudCaseEntity entity = new FraudCaseEntity();
        entity.caseId(record.caseId());
        entity.alertId(record.alertId());
        entity.transactionId(record.transactionId());
        entity.decisionId(record.decisionId());
        entity.tenantId(record.tenantId());
        entity.priority(record.priority());
        entity.status(record.status());
        entity.title(record.title());
        entity.summary(record.summary());
        entity.createdAt(record.createdAt());
        entity.eventType(record.eventType());
        entity.eventId(record.eventId());
        entity.correlationId(record.correlationId());
        entity.causationId(record.causationId());
        entity.traceId(record.traceId());
        entity.attributesText(toText(record.attributes()));
        return entity;
    }

    public AlertRecord toRecord(FraudAlertEntity entity) {
        return new AlertRecord(
                entity.alertId(),
                entity.decisionId(),
                entity.transactionId(),
                entity.tenantId(),
                entity.priority(),
                entity.riskBand(),
                entity.status(),
                entity.title(),
                entity.summary(),
                entity.createdAt(),
                entity.eventType(),
                entity.eventId(),
                entity.correlationId(),
                entity.causationId(),
                entity.traceId(),
                fromText(entity.attributesText()));
    }

    public CaseRecord toRecord(FraudCaseEntity entity) {
        return new CaseRecord(
                entity.caseId(),
                entity.alertId(),
                entity.transactionId(),
                entity.decisionId(),
                entity.tenantId(),
                entity.priority(),
                entity.status(),
                entity.title(),
                entity.summary(),
                entity.createdAt(),
                entity.eventType(),
                entity.eventId(),
                entity.correlationId(),
                entity.causationId(),
                entity.traceId(),
                fromText(entity.attributesText()));
    }

    private String toText(Map<String, String> attributes) {
        Properties properties = new Properties();
        Map<String, String> source = attributes == null ? Map.of() : attributes;
        source.forEach((key, value) -> {
            if (key != null && value != null) {
                properties.setProperty(key, value);
            }
        });

        try {
            StringWriter writer = new StringWriter();
            properties.store(writer, null);
            return writer.toString();
        } catch (IOException exception) {
            throw new CaseWorkflowPersistenceException("Case workflow attributes could not be serialized.", exception);
        }
    }

    private Map<String, String> fromText(String text) {
        if (text == null || text.isBlank()) {
            return Map.of();
        }

        Properties properties = new Properties();
        try {
            properties.load(new StringReader(text));
        } catch (IOException exception) {
            throw new CaseWorkflowPersistenceException("Case workflow attributes could not be deserialized.", exception);
        }

        Map<String, String> attributes = new LinkedHashMap<>();
        properties.stringPropertyNames().stream()
                .sorted()
                .forEach(name -> attributes.put(name, properties.getProperty(name)));
        return attributes;
    }
}
