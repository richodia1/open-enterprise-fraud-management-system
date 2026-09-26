package org.openefms.auditlogging.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.openefms.auditlogging.service.AuditEventSink;
import org.openefms.auditlogging.service.InvalidAuditEventRecordedEventException;
import org.openefms.common.events.EventEnvelope;
import org.openefms.common.events.EventMetadata;
import org.openefms.common.events.EventTypes;
import org.openefms.common.events.audit.AuditEventRecordedEvent;
import org.openefms.common.events.kafka.KafkaTopics;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "oefms.event-transport", havingValue = "kafka")
public class KafkaAuditEventRecordedEventListener {
    private static final int SUPPORTED_EVENT_VERSION = 1;

    private final AuditEventSink auditEventSink;
    private final ObjectMapper objectMapper;
    private final JavaType eventType;

    public KafkaAuditEventRecordedEventListener(AuditEventSink auditEventSink, ObjectMapper objectMapper) {
        this.auditEventSink = auditEventSink;
        this.objectMapper = objectMapper;
        this.eventType = objectMapper.getTypeFactory()
                .constructParametricType(EventEnvelope.class, AuditEventRecordedEvent.class);
    }

    @KafkaListener(
            topics = KafkaTopics.AUDIT_EVENT_RECORDED,
            groupId = "${oefms.kafka.consumer-groups.audit-logging:oefms.audit-logging}")
    public void handle(String payload) {
        EventEnvelope<AuditEventRecordedEvent> event = readEvent(payload);
        validate(event);

        auditEventSink.record(event.payload().auditEvent(), event.metadata());
    }

    private EventEnvelope<AuditEventRecordedEvent> readEvent(String payload) {
        try {
            return objectMapper.readValue(payload, eventType);
        } catch (JsonProcessingException exception) {
            throw new InvalidAuditEventRecordedEventException(List.of("payload must be a valid audit.event.recorded event JSON document"));
        }
    }

    private void validate(EventEnvelope<AuditEventRecordedEvent> event) {
        List<String> errors = new ArrayList<>();
        if (event == null) {
            errors.add("event must not be null");
            throw new InvalidAuditEventRecordedEventException(errors);
        }

        EventMetadata metadata = event.metadata();

        if (!EventTypes.AUDIT_EVENT_RECORDED.equals(metadata.eventType())) {
            errors.add("eventType must be " + EventTypes.AUDIT_EVENT_RECORDED);
        }
        if (metadata.eventVersion() != SUPPORTED_EVENT_VERSION) {
            errors.add("eventVersion must be " + SUPPORTED_EVENT_VERSION);
        }

        if (!errors.isEmpty()) {
            throw new InvalidAuditEventRecordedEventException(errors);
        }
    }
}
