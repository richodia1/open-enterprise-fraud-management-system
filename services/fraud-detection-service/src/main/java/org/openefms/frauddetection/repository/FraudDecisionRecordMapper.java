package org.openefms.frauddetection.repository;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import org.openefms.common.events.EventMetadata;
import org.openefms.common.models.fraud.FraudDecisionResponse;
import org.openefms.common.models.fraud.ModelSignal;
import org.openefms.common.models.fraud.RuleHit;
import org.openefms.frauddetection.entity.FraudDecisionEntity;
import org.openefms.frauddetection.entity.FraudDecisionRecord;
import org.openefms.frauddetection.service.FraudDecisionPersistenceException;
import org.springframework.stereotype.Component;

@Component
public class FraudDecisionRecordMapper {
    public FraudDecisionRecord fromDecision(FraudDecisionResponse decision) {
        return fromDecision(decision, null);
    }

    public FraudDecisionRecord fromDecision(FraudDecisionResponse decision, EventMetadata metadata) {
        return new FraudDecisionRecord(
                decision.decisionId(),
                decision.transactionId(),
                decision.tenantId(),
                decision.mode(),
                decision.outcome(),
                decision.recommendedAction(),
                decision.riskScore().value(),
                decision.riskScore().band(),
                decision.riskScore().reason(),
                decision.ruleHits().size(),
                decision.modelSignals().size(),
                ruleEvidenceSummary(decision.ruleHits()),
                modelSignalSummary(decision.modelSignals()),
                decision.decidedAt(),
                metadata == null ? null : metadata.eventType(),
                metadata == null ? null : metadata.eventId(),
                metadata == null ? null : metadata.correlationId(),
                metadata == null ? null : metadata.causationId(),
                metadata == null ? null : metadata.traceId(),
                decision.attributes());
    }

    public FraudDecisionEntity toEntity(FraudDecisionRecord record) {
        FraudDecisionEntity entity = new FraudDecisionEntity();
        entity.decisionId(record.decisionId());
        entity.transactionId(record.transactionId());
        entity.tenantId(record.tenantId());
        entity.mode(record.mode());
        entity.outcome(record.outcome());
        entity.recommendedAction(record.recommendedAction());
        entity.riskScoreValue(record.riskScoreValue());
        entity.riskBand(record.riskBand());
        entity.riskReason(record.riskReason());
        entity.ruleHitCount(record.ruleHitCount());
        entity.modelSignalCount(record.modelSignalCount());
        entity.ruleEvidenceSummary(record.ruleEvidenceSummary());
        entity.modelSignalSummary(record.modelSignalSummary());
        entity.decidedAt(record.decidedAt());
        entity.eventType(record.eventType());
        entity.eventId(record.eventId());
        entity.correlationId(record.correlationId());
        entity.causationId(record.causationId());
        entity.traceId(record.traceId());
        entity.attributesText(toText(record.attributes()));
        return entity;
    }

    public FraudDecisionRecord toRecord(FraudDecisionEntity entity) {
        return new FraudDecisionRecord(
                entity.decisionId(),
                entity.transactionId(),
                entity.tenantId(),
                entity.mode(),
                entity.outcome(),
                entity.recommendedAction(),
                entity.riskScoreValue(),
                entity.riskBand(),
                entity.riskReason(),
                entity.ruleHitCount(),
                entity.modelSignalCount(),
                entity.ruleEvidenceSummary(),
                entity.modelSignalSummary(),
                entity.decidedAt(),
                entity.eventType(),
                entity.eventId(),
                entity.correlationId(),
                entity.causationId(),
                entity.traceId(),
                fromText(entity.attributesText()));
    }

    private String ruleEvidenceSummary(List<RuleHit> ruleHits) {
        if (ruleHits.isEmpty()) {
            return "No rule hits.";
        }
        return ruleHits.stream()
                .map(this::ruleHitSummary)
                .collect(Collectors.joining("\n"));
    }

    private String ruleHitSummary(RuleHit hit) {
        return "%s|%s|%s|%s".formatted(
                hit.ruleCode(),
                hit.severity(),
                decimalText(hit.contribution()),
                singleLine(hit.explanation()));
    }

    private String modelSignalSummary(List<ModelSignal> modelSignals) {
        if (modelSignals.isEmpty()) {
            return "No model signals.";
        }
        return modelSignals.stream()
                .map(this::modelSignalSummary)
                .collect(Collectors.joining("\n"));
    }

    private String modelSignalSummary(ModelSignal signal) {
        return "%s|%s|%s|%s".formatted(
                signal.modelName(),
                singleLine(signal.modelVersion()),
                decimalText(signal.score()),
                decimalText(signal.contribution()));
    }

    private String decimalText(BigDecimal value) {
        return value == null ? "" : value.toPlainString();
    }

    private String singleLine(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.trim().replace('\n', ' ').replace('\r', ' ');
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
            throw new FraudDecisionPersistenceException("Fraud decision attributes could not be serialized.", exception);
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
            throw new FraudDecisionPersistenceException("Fraud decision attributes could not be deserialized.", exception);
        }

        Map<String, String> attributes = new LinkedHashMap<>();
        properties.stringPropertyNames().stream()
                .sorted()
                .forEach(name -> attributes.put(name, properties.getProperty(name)));
        return attributes;
    }
}
