package org.openefms.casemanagement.messaging;

import java.util.Date;
import org.openefms.common.events.EventMetadata;
import org.openefms.common.events.rabbitmq.RabbitMqHeaders;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class RabbitMqCaseWorkflowEventPublisher implements DomainEventMessageSink {
    private final RabbitTemplate rabbitTemplate;

    public RabbitMqCaseWorkflowEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void accept(DomainEventMessage<?> message) {
        try {
            rabbitTemplate.convertAndSend(
                    message.exchangeName(),
                    message.routingKey(),
                    message.envelope(),
                    amqpMessage -> enrichHeaders(amqpMessage, message.envelope().metadata()));
        } catch (AmqpException exception) {
            throw new CaseWorkflowPublicationException(message, exception);
        }
    }

    private Message enrichHeaders(Message message, EventMetadata metadata) {
        MessageProperties properties = message.getMessageProperties();
        properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        properties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
        properties.setMessageId(metadata.eventId().toString());
        properties.setType(metadata.eventType());
        properties.setTimestamp(Date.from(metadata.occurredAt().toInstant()));
        setIfPresent(properties, RabbitMqHeaders.EVENT_ID, metadata.eventId().toString());
        setIfPresent(properties, RabbitMqHeaders.EVENT_TYPE, metadata.eventType());
        properties.setHeader(RabbitMqHeaders.EVENT_VERSION, metadata.eventVersion());
        setIfPresent(properties, RabbitMqHeaders.SOURCE_SERVICE, metadata.sourceService());
        setIfPresent(properties, RabbitMqHeaders.TENANT_ID, metadata.tenantId());
        setIfPresent(properties, RabbitMqHeaders.CORRELATION_ID, metadata.correlationId());
        setIfPresent(properties, RabbitMqHeaders.CAUSATION_ID, metadata.causationId());
        setIfPresent(properties, RabbitMqHeaders.TRACE_ID, metadata.traceId());
        return message;
    }

    private void setIfPresent(MessageProperties properties, String headerName, String value) {
        if (value != null && !value.isBlank()) {
            properties.setHeader(headerName, value);
        }
    }
}
