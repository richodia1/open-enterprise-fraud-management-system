package org.openefms.casemanagement.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.openefms.casemanagement.service.CaseWorkflowOrchestrator;
import org.openefms.casemanagement.service.InvalidExplanationPreparedEventException;
import org.openefms.common.events.EventEnvelope;
import org.openefms.common.events.EventMetadata;
import org.openefms.common.events.EventTypes;
import org.openefms.common.events.explanation.ExplanationPreparedEvent;
import org.openefms.common.events.kafka.KafkaTopics;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "oefms.event-transport", havingValue = "kafka")
public class KafkaExplanationPreparedEventListener {
    private static final int SUPPORTED_EVENT_VERSION = 1;

    private final CaseWorkflowOrchestrator orchestrator;
    private final DomainEventMessageSink messageSink;
    private final ObjectMapper objectMapper;
    private final JavaType eventType;

    public KafkaExplanationPreparedEventListener(
            CaseWorkflowOrchestrator orchestrator,
            DomainEventMessageSink messageSink,
            ObjectMapper objectMapper) {
        this.orchestrator = orchestrator;
        this.messageSink = messageSink;
        this.objectMapper = objectMapper;
        this.eventType = objectMapper.getTypeFactory()
                .constructParametricType(EventEnvelope.class, ExplanationPreparedEvent.class);
    }

    @KafkaListener(
            topics = KafkaTopics.EXPLANATION_PREPARED,
            groupId = "${oefms.kafka.consumer-groups.case-management:oefms.case-management}")
    public void handle(String payload) {
        EventEnvelope<ExplanationPreparedEvent> event = readEvent(payload);
        validate(event);

        orchestrator.prepare(event).messages().forEach(messageSink::accept);
    }

    private EventEnvelope<ExplanationPreparedEvent> readEvent(String payload) {
        try {
            return objectMapper.readValue(payload, eventType);
        } catch (JsonProcessingException exception) {
            throw new InvalidExplanationPreparedEventException(List.of("payload must be a valid explanation.prepared event JSON document"));
        }
    }

    private void validate(EventEnvelope<ExplanationPreparedEvent> event) {
        List<String> errors = new ArrayList<>();
        if (event == null) {
            errors.add("event must not be null");
            throw new InvalidExplanationPreparedEventException(errors);
        }

        EventMetadata metadata = event.metadata();

        if (!EventTypes.EXPLANATION_PREPARED.equals(metadata.eventType())) {
            errors.add("eventType must be " + EventTypes.EXPLANATION_PREPARED);
        }
        if (metadata.eventVersion() != SUPPORTED_EVENT_VERSION) {
            errors.add("eventVersion must be " + SUPPORTED_EVENT_VERSION);
        }

        if (!errors.isEmpty()) {
            throw new InvalidExplanationPreparedEventException(errors);
        }
    }
}
