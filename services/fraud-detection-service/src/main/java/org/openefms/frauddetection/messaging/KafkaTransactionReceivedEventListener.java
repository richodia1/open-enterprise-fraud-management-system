package org.openefms.frauddetection.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.openefms.common.events.EventEnvelope;
import org.openefms.common.events.EventMetadata;
import org.openefms.common.events.EventTypes;
import org.openefms.common.events.kafka.KafkaTopics;
import org.openefms.common.events.transaction.TransactionReceivedEvent;
import org.openefms.frauddetection.service.FraudDecisionOrchestrator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "oefms.event-transport", havingValue = "kafka")
public class KafkaTransactionReceivedEventListener {
    private static final int SUPPORTED_EVENT_VERSION = 1;

    private final FraudDecisionOrchestrator orchestrator;
    private final FraudDecisionMessageSink messageSink;
    private final ObjectMapper objectMapper;
    private final JavaType eventType;

    public KafkaTransactionReceivedEventListener(
            FraudDecisionOrchestrator orchestrator,
            FraudDecisionMessageSink messageSink,
            ObjectMapper objectMapper) {
        this.orchestrator = orchestrator;
        this.messageSink = messageSink;
        this.objectMapper = objectMapper;
        this.eventType = objectMapper.getTypeFactory()
                .constructParametricType(EventEnvelope.class, TransactionReceivedEvent.class);
    }

    @KafkaListener(
            topics = KafkaTopics.TRANSACTION_RECEIVED,
            groupId = "${oefms.kafka.consumer-groups.fraud-detection:oefms.fraud-detection}")
    public void handle(String payload) {
        EventEnvelope<TransactionReceivedEvent> event = readEvent(payload);
        validate(event);

        FraudDecisionMessage decisionMessage = orchestrator.decide(event);
        messageSink.accept(decisionMessage);
    }

    private EventEnvelope<TransactionReceivedEvent> readEvent(String payload) {
        try {
            return objectMapper.readValue(payload, eventType);
        } catch (JsonProcessingException exception) {
            throw new InvalidTransactionReceivedEventException(List.of("payload must be a valid transaction.received event JSON document"));
        }
    }

    private void validate(EventEnvelope<TransactionReceivedEvent> event) {
        List<String> errors = new ArrayList<>();
        if (event == null) {
            errors.add("event must not be null");
            throw new InvalidTransactionReceivedEventException(errors);
        }

        EventMetadata metadata = event.metadata();

        if (!EventTypes.TRANSACTION_RECEIVED.equals(metadata.eventType())) {
            errors.add("eventType must be " + EventTypes.TRANSACTION_RECEIVED);
        }
        if (metadata.eventVersion() != SUPPORTED_EVENT_VERSION) {
            errors.add("eventVersion must be " + SUPPORTED_EVENT_VERSION);
        }

        if (!errors.isEmpty()) {
            throw new InvalidTransactionReceivedEventException(errors);
        }
    }
}
