package org.openefms.explainableai.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.openefms.common.events.EventMetadata;
import org.openefms.common.events.kafka.KafkaHeaders;
import org.openefms.common.events.kafka.KafkaTopics;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "oefms.event-transport", havingValue = "kafka")
public class KafkaExplanationEventPublisher implements ExplanationMessageSink {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public KafkaExplanationEventPublisher(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void accept(ExplanationMessage message) {
        try {
            EventMetadata metadata = message.envelope().metadata();
            ProducerRecord<String, String> record = new ProducerRecord<>(
                    KafkaTopics.forEventType(metadata.eventType()),
                    message.envelope().payload().explanation().transactionId().toString(),
                    objectMapper.writeValueAsString(message.envelope()));
            enrichHeaders(record, metadata);
            kafkaTemplate.send(record).join();
        } catch (JsonProcessingException | RuntimeException exception) {
            throw new ExplanationPublicationException(message, exception);
        }
    }

    private void enrichHeaders(ProducerRecord<String, String> record, EventMetadata metadata) {
        setIfPresent(record, KafkaHeaders.EVENT_ID, metadata.eventId().toString());
        setIfPresent(record, KafkaHeaders.EVENT_TYPE, metadata.eventType());
        setIfPresent(record, KafkaHeaders.EVENT_VERSION, Integer.toString(metadata.eventVersion()));
        setIfPresent(record, KafkaHeaders.SOURCE_SERVICE, metadata.sourceService());
        setIfPresent(record, KafkaHeaders.TENANT_ID, metadata.tenantId());
        setIfPresent(record, KafkaHeaders.CORRELATION_ID, metadata.correlationId());
        setIfPresent(record, KafkaHeaders.CAUSATION_ID, metadata.causationId());
        setIfPresent(record, KafkaHeaders.TRACE_ID, metadata.traceId());
    }

    private void setIfPresent(ProducerRecord<String, String> record, String headerName, String value) {
        if (value != null && !value.isBlank()) {
            record.headers().add(headerName, value.getBytes(StandardCharsets.UTF_8));
        }
    }
}
