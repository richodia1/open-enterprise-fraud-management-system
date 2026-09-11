package org.openefms.casemanagement.messaging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.openefms.casemanagement.workflow.DomainEventMessage;
import org.openefms.common.events.EventEnvelope;
import org.openefms.common.events.EventMetadata;
import org.openefms.common.events.EventTypes;
import org.openefms.common.events.alert.AlertCreatedEvent;
import org.openefms.common.events.rabbitmq.RabbitMqExchanges;
import org.openefms.common.events.rabbitmq.RabbitMqHeaders;
import org.openefms.common.events.rabbitmq.RabbitMqRoutingKeys;
import org.openefms.common.models.alert.AlertCreationRequest;
import org.openefms.common.models.alert.AlertPriority;
import org.openefms.common.models.fraud.RiskBand;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

class RabbitMqCaseWorkflowEventPublisherTest {
    private static final UUID EVENT_ID = UUID.fromString("8fef9128-d7c1-49dd-bb03-c98f5105abef");

    @Test
    void publishesCaseWorkflowEventWithBrokerHeaders() {
        CapturingRabbitTemplate rabbitTemplate = new CapturingRabbitTemplate();
        RabbitMqCaseWorkflowEventPublisher publisher = new RabbitMqCaseWorkflowEventPublisher(rabbitTemplate);
        DomainEventMessage<AlertCreatedEvent> message = alertMessage();

        publisher.accept(message);

        assertEquals(RabbitMqExchanges.DOMAIN_EVENTS, rabbitTemplate.exchangeName);
        assertEquals(RabbitMqRoutingKeys.ALERT_CREATED, rabbitTemplate.routingKey);
        assertSame(message.envelope(), rabbitTemplate.payload);
        assertEquals(MessageProperties.CONTENT_TYPE_JSON, rabbitTemplate.messageProperties.getContentType());
        assertEquals(MessageDeliveryMode.PERSISTENT, rabbitTemplate.messageProperties.getDeliveryMode());
        assertEquals(EVENT_ID.toString(), rabbitTemplate.messageProperties.getMessageId());
        assertEquals(EventTypes.ALERT_CREATED, rabbitTemplate.messageProperties.getType());
        assertEquals(EVENT_ID.toString(), rabbitTemplate.header(RabbitMqHeaders.EVENT_ID));
        assertEquals(EventTypes.ALERT_CREATED, rabbitTemplate.header(RabbitMqHeaders.EVENT_TYPE));
        assertEquals(1, rabbitTemplate.header(RabbitMqHeaders.EVENT_VERSION));
        assertEquals("case-management-service", rabbitTemplate.header(RabbitMqHeaders.SOURCE_SERVICE));
        assertEquals("tenant-a", rabbitTemplate.header(RabbitMqHeaders.TENANT_ID));
        assertEquals("correlation-1", rabbitTemplate.header(RabbitMqHeaders.CORRELATION_ID));
        assertEquals("a638bf8a-2c9c-4e43-aae3-caf77d8497ac", rabbitTemplate.header(RabbitMqHeaders.CAUSATION_ID));
        assertEquals("trace-1", rabbitTemplate.header(RabbitMqHeaders.TRACE_ID));
    }

    @Test
    void wrapsRabbitFailuresWithPublicationContext() {
        RabbitMqCaseWorkflowEventPublisher publisher = new RabbitMqCaseWorkflowEventPublisher(new FailingRabbitTemplate());

        CaseWorkflowPublicationException exception = assertThrows(
                CaseWorkflowPublicationException.class,
                () -> publisher.accept(alertMessage()));

        assertEquals(EventTypes.ALERT_CREATED, exception.eventType());
        assertEquals(RabbitMqExchanges.DOMAIN_EVENTS, exception.exchangeName());
        assertEquals(RabbitMqRoutingKeys.ALERT_CREATED, exception.routingKey());
    }

    private DomainEventMessage<AlertCreatedEvent> alertMessage() {
        AlertCreationRequest alert = new AlertCreationRequest(
                UUID.fromString("61ab44d5-4b27-43d6-9f67-4de7f162d584"),
                UUID.fromString("325dc0f5-775d-4858-89be-1992e52fe2f0"),
                UUID.fromString("118f3bd4-b208-44ff-b1ff-447c78d80d02"),
                "tenant-a",
                AlertPriority.MEDIUM,
                RiskBand.MEDIUM,
                "Fraud review alert for transaction 118f3bd4",
                "Decision REVIEW with MEDIUM risk score 0.60.",
                OffsetDateTime.parse("2026-09-11T11:00:00Z"),
                Map.of("riskScore", "0.60"));
        EventEnvelope<AlertCreatedEvent> envelope = new EventEnvelope<>(
                new EventMetadata(
                        EVENT_ID,
                        EventTypes.ALERT_CREATED,
                        1,
                        OffsetDateTime.parse("2026-09-11T11:00:01Z"),
                        "case-management-service",
                        "tenant-a",
                        "correlation-1",
                        "a638bf8a-2c9c-4e43-aae3-caf77d8497ac",
                        "trace-1",
                        Map.of("causedByEventType", EventTypes.EXPLANATION_PREPARED)),
                new AlertCreatedEvent(alert));

        return new DomainEventMessage<>(
                RabbitMqExchanges.DOMAIN_EVENTS,
                RabbitMqRoutingKeys.ALERT_CREATED,
                envelope);
    }

    private static final class CapturingRabbitTemplate extends RabbitTemplate {
        private String exchangeName;
        private String routingKey;
        private Object payload;
        private MessageProperties messageProperties;

        @Override
        public void convertAndSend(
                String exchange,
                String routingKey,
                Object object,
                MessagePostProcessor messagePostProcessor) throws AmqpException {
            this.exchangeName = exchange;
            this.routingKey = routingKey;
            this.payload = object;
            Message message = messagePostProcessor.postProcessMessage(new Message(new byte[0], new MessageProperties()));
            this.messageProperties = message.getMessageProperties();
        }

        private Object header(String name) {
            return messageProperties.getHeaders().get(name);
        }
    }

    private static final class FailingRabbitTemplate extends RabbitTemplate {
        @Override
        public void convertAndSend(
                String exchange,
                String routingKey,
                Object object,
                MessagePostProcessor messagePostProcessor) throws AmqpException {
            throw new AmqpException("RabbitMQ unavailable");
        }
    }
}
