package org.openefms.auditlogging.audit;

import java.util.ArrayList;
import java.util.List;
import org.openefms.common.events.EventEnvelope;
import org.openefms.common.events.EventMetadata;
import org.openefms.common.events.EventTypes;
import org.openefms.common.events.audit.AuditEventRecordedEvent;
import org.openefms.common.events.rabbitmq.RabbitMqQueues;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
class AuditEventRecordedEventListener {
    private static final int SUPPORTED_EVENT_VERSION = 1;

    private final AuditEventSink auditEventSink;

    AuditEventRecordedEventListener(AuditEventSink auditEventSink) {
        this.auditEventSink = auditEventSink;
    }

    @RabbitListener(queues = RabbitMqQueues.AUDIT_LOGGING_AUDIT_EVENT_RECORDED)
    public void handle(EventEnvelope<AuditEventRecordedEvent> event) {
        validate(event);

        auditEventSink.record(event.payload().auditEvent());
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
