package org.openefms.explainableai.messaging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.openefms.common.events.EventEnvelope;
import org.openefms.common.events.EventMetadata;
import org.openefms.common.events.EventTypes;
import org.openefms.common.events.explanation.ExplanationPreparedEvent;
import org.openefms.common.events.rabbitmq.RabbitMqExchanges;
import org.openefms.common.events.rabbitmq.RabbitMqHeaders;
import org.openefms.common.events.rabbitmq.RabbitMqRoutingKeys;
import org.openefms.common.models.explanation.ExplanationResponse;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

class RabbitMqExplanationEventPublisherTest {
    private static final UUID EVENT_ID = UUID.fromString("a638bf8a-2c9c-4e43-aae3-caf77d8497ac");

    @Test
    void publishesExplanationEventWithBrokerHeaders() {
        CapturingRabbitTemplate rabbitTemplate = new CapturingRabbitTemplate();
        RabbitMqExplanationEventPublisher publisher = new RabbitMqExplanationEventPublisher(rabbitTemplate);
        ExplanationMessage message = explanationMessage();

        publisher.accept(message);

        assertEquals(RabbitMqExchanges.DOMAIN_EVENTS, rabbitTemplate.exchangeName);
        assertEquals(RabbitMqRoutingKeys.EXPLANATION_PREPARED, rabbitTemplate.routingKey);
        assertSame(message.envelope(), rabbitTemplate.payload);
        assertEquals(MessageProperties.CONTENT_TYPE_JSON, rabbitTemplate.messageProperties.getContentType());
        assertEquals(MessageDeliveryMode.PERSISTENT, rabbitTemplate.messageProperties.getDeliveryMode());
        assertEquals(EVENT_ID.toString(), rabbitTemplate.messageProperties.getMessageId());
        assertEquals(EventTypes.EXPLANATION_PREPARED, rabbitTemplate.messageProperties.getType());
        assertEquals(EVENT_ID.toString(), rabbitTemplate.header(RabbitMqHeaders.EVENT_ID));
        assertEquals(EventTypes.EXPLANATION_PREPARED, rabbitTemplate.header(RabbitMqHeaders.EVENT_TYPE));
        assertEquals(1, rabbitTemplate.header(RabbitMqHeaders.EVENT_VERSION));
        assertEquals("explainable-ai-service", rabbitTemplate.header(RabbitMqHeaders.SOURCE_SERVICE));
        assertEquals("tenant-a", rabbitTemplate.header(RabbitMqHeaders.TENANT_ID));
        assertEquals("correlation-1", rabbitTemplate.header(RabbitMqHeaders.CORRELATION_ID));
        assertEquals("fb40ff28-86cc-4b6c-a6df-b538d5155d2e", rabbitTemplate.header(RabbitMqHeaders.CAUSATION_ID));
        assertEquals("trace-1", rabbitTemplate.header(RabbitMqHeaders.TRACE_ID));
    }

    @Test
    void wrapsRabbitFailuresWithPublicationContext() {
        RabbitMqExplanationEventPublisher publisher = new RabbitMqExplanationEventPublisher(new FailingRabbitTemplate());

        ExplanationPublicationException exception = assertThrows(
                ExplanationPublicationException.class,
                () -> publisher.accept(explanationMessage()));

        assertEquals(EventTypes.EXPLANATION_PREPARED, exception.eventType());
        assertEquals(RabbitMqExchanges.DOMAIN_EVENTS, exception.exchangeName());
        assertEquals(RabbitMqRoutingKeys.EXPLANATION_PREPARED, exception.routingKey());
    }

    private ExplanationMessage explanationMessage() {
        ExplanationResponse explanation = new ExplanationResponse(
                UUID.fromString("d88154d9-5246-43ec-bb65-fcdfd38f64a3"),
                UUID.fromString("325dc0f5-775d-4858-89be-1992e52fe2f0"),
                UUID.fromString("118f3bd4-b208-44ff-b1ff-447c78d80d02"),
                "tenant-a",
                "Decision REVIEW with MEDIUM risk score 0.60. Key evidence: High value amount.",
                List.of(),
                OffsetDateTime.parse("2026-09-11T10:00:00Z"),
                Map.of("evidenceCount", "1"));
        EventEnvelope<ExplanationPreparedEvent> envelope = new EventEnvelope<>(
                new EventMetadata(
                        EVENT_ID,
                        EventTypes.EXPLANATION_PREPARED,
                        1,
                        OffsetDateTime.parse("2026-09-11T10:00:01Z"),
                        "explainable-ai-service",
                        "tenant-a",
                        "correlation-1",
                        "fb40ff28-86cc-4b6c-a6df-b538d5155d2e",
                        "trace-1",
                        Map.of("causedByEventType", EventTypes.FRAUD_DECISION_PRODUCED)),
                new ExplanationPreparedEvent(explanation));

        return new ExplanationMessage(
                RabbitMqExchanges.DOMAIN_EVENTS,
                RabbitMqRoutingKeys.EXPLANATION_PREPARED,
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
