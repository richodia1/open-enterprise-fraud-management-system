package org.openefms.auditlogging.audit;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import org.openefms.common.events.EventMetadata;
import org.openefms.common.models.audit.AuditEventRecord;
import org.springframework.stereotype.Component;

@Component
class AuditLogRecordMapper {
    AuditLogRecord fromAuditEvent(AuditEventRecord auditEvent, EventMetadata metadata) {
        return new AuditLogRecord(
                auditEvent.auditEventId(),
                auditEvent.tenantId(),
                auditEvent.actorId(),
                auditEvent.action(),
                auditEvent.resourceType(),
                auditEvent.resourceId(),
                auditEvent.summary(),
                auditEvent.occurredAt(),
                metadata.eventType(),
                metadata.eventId(),
                metadata.correlationId(),
                metadata.causationId(),
                metadata.traceId(),
                metadata.sourceService(),
                metadata.occurredAt(),
                auditEvent.attributes());
    }

    AuditLogEntity toEntity(AuditLogRecord record) {
        AuditLogEntity entity = new AuditLogEntity();
        entity.auditEventId(record.auditEventId());
        entity.tenantId(record.tenantId());
        entity.actorId(record.actorId());
        entity.action(record.action());
        entity.resourceType(record.resourceType());
        entity.resourceId(record.resourceId());
        entity.summary(record.summary());
        entity.occurredAt(record.occurredAt());
        entity.eventType(record.eventType());
        entity.eventId(record.eventId());
        entity.correlationId(record.correlationId());
        entity.causationId(record.causationId());
        entity.traceId(record.traceId());
        entity.sourceService(record.sourceService());
        entity.eventOccurredAt(record.eventOccurredAt());
        entity.attributesText(toText(record.attributes()));
        return entity;
    }

    AuditLogRecord toRecord(AuditLogEntity entity) {
        return new AuditLogRecord(
                entity.auditEventId(),
                entity.tenantId(),
                entity.actorId(),
                entity.action(),
                entity.resourceType(),
                entity.resourceId(),
                entity.summary(),
                entity.occurredAt(),
                entity.eventType(),
                entity.eventId(),
                entity.correlationId(),
                entity.causationId(),
                entity.traceId(),
                entity.sourceService(),
                entity.eventOccurredAt(),
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
            throw new AuditLogPersistenceException("Audit event attributes could not be serialized.", exception);
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
            throw new AuditLogPersistenceException("Audit event attributes could not be deserialized.", exception);
        }

        Map<String, String> attributes = new LinkedHashMap<>();
        properties.stringPropertyNames().stream()
                .sorted()
                .forEach(name -> attributes.put(name, properties.getProperty(name)));
        return attributes;
    }
}
