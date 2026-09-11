package org.openefms.frauddetection.messaging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.openefms.common.events.EventEnvelope;
import org.openefms.common.events.EventMetadata;
import org.openefms.common.events.EventTypes;
import org.openefms.common.events.fraud.FraudDecisionProducedEvent;
import org.openefms.common.events.rabbitmq.RabbitMqExchanges;
import org.openefms.common.events.rabbitmq.RabbitMqHeaders;
import org.openefms.common.events.rabbitmq.RabbitMqRoutingKeys;
import org.openefms.common.models.fraud.DecisionMode;
import org.openefms.common.models.fraud.DecisionOutcome;
import org.openefms.common.models.fraud.FraudDecisionResponse;
import org.openefms.common.models.fraud.RiskBand;
import org.openefms.common.models.fraud.RiskScore;
import org.openefms.frauddetection.decision.FraudDecisionMessage;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

class RabbitMqFraudDecisionEventPublisherTest {
    private static final UUID EVENT_ID = UUID.fromString("fb40ff28-86cc-4b6c-a6df-b538d5155d2e");

    @Test
    void publishesFraudDecisionEventWithBrokerHeaders() {
        CapturingRabbitTemplate rabbitTemplate = new CapturingRabbitTemplate();
        RabbitMqFraudDecisionEventPublisher publisher = new RabbitMqFraudDecisionEventPublisher(rabbitTemplate);
        FraudDecisionMessage message = fraudDecisionMessage();

        publisher.accept(message);

        assertEquals(RabbitMqExchanges.DOMAIN_EVENTS, rabbitTemplate.exchangeName);
        assertEquals(RabbitMqRoutingKeys.FRAUD_DECISION_PRODUCED, rabbitTemplate.routingKey);
        assertSame(message.envelope(), rabbitTemplate.payload);
        assertEquals(MessageProperties.CONTENT_TYPE_JSON, rabbitTemplate.messageProperties.getContentType());
        assertEquals(MessageDeliveryMode.PERSISTENT, rabbitTemplate.messageProperties.getDeliveryMode());
        assertEquals(EVENT_ID.toString(), rabbitTemplate.messageProperties.getMessageId());
        assertEquals(EventTypes.FRAUD_DECISION_PRODUCED, rabbitTemplate.messageProperties.getType());
        assertEquals(EVENT_ID.toString(), rabbitTemplate.header(RabbitMqHeaders.EVENT_ID));
        assertEquals(EventTypes.FRAUD_DECISION_PRODUCED, rabbitTemplate.header(RabbitMqHeaders.EVENT_TYPE));
        assertEquals(1, rabbitTemplate.header(RabbitMqHeaders.EVENT_VERSION));
        assertEquals("fraud-detection-service", rabbitTemplate.header(RabbitMqHeaders.SOURCE_SERVICE));
        assertEquals("tenant-a", rabbitTemplate.header(RabbitMqHeaders.TENANT_ID));
        assertEquals("correlation-1", rabbitTemplate.header(RabbitMqHeaders.CORRELATION_ID));
        assertEquals("7e37875e-7443-47d4-931c-df8ce0f10cab", rabbitTemplate.header(RabbitMqHeaders.CAUSATION_ID));
        assertEquals("trace-1", rabbitTemplate.header(RabbitMqHeaders.TRACE_ID));
    }

    @Test
    void wrapsRabbitFailuresWithPublicationContext() {
        RabbitMqFraudDecisionEventPublisher publisher =
                new RabbitMqFraudDecisionEventPublisher(new FailingRabbitTemplate());

        FraudDecisionPublicationException exception = assertThrows(
                FraudDecisionPublicationException.class,
                () -> publisher.accept(fraudDecisionMessage()));

        assertEquals(EventTypes.FRAUD_DECISION_PRODUCED, exception.eventType());
        assertEquals(RabbitMqExchanges.DOMAIN_EVENTS, exception.exchangeName());
        assertEquals(RabbitMqRoutingKeys.FRAUD_DECISION_PRODUCED, exception.routingKey());
    }

    private FraudDecisionMessage fraudDecisionMessage() {
        FraudDecisionResponse decision = new FraudDecisionResponse(
                UUID.fromString("325dc0f5-775d-4858-89be-1992e52fe2f0"),
                UUID.fromString("118f3bd4-b208-44ff-b1ff-447c78d80d02"),
                "tenant-a",
                DecisionMode.RULES,
                DecisionOutcome.REVIEW,
                new RiskScore(new BigDecimal("0.42"), RiskBand.MEDIUM, "Rule score crossed review threshold."),
                List.of(),
                List.of(),
                OffsetDateTime.parse("2026-09-11T09:00:00Z"),
                Map.of());
        EventEnvelope<FraudDecisionProducedEvent> envelope = new EventEnvelope<>(
                new EventMetadata(
                        EVENT_ID,
                        EventTypes.FRAUD_DECISION_PRODUCED,
                        1,
                        OffsetDateTime.parse("2026-09-11T09:00:01Z"),
                        "fraud-detection-service",
                        "tenant-a",
                        "correlation-1",
                        "7e37875e-7443-47d4-931c-df8ce0f10cab",
                        "trace-1",
                        Map.of("causedByEventType", EventTypes.TRANSACTION_RECEIVED)),
                new FraudDecisionProducedEvent(decision));

        return new FraudDecisionMessage(
                RabbitMqExchanges.DOMAIN_EVENTS,
                RabbitMqRoutingKeys.FRAUD_DECISION_PRODUCED,
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
