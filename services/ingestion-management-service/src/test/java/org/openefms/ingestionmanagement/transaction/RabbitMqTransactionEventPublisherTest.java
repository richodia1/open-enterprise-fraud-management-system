package org.openefms.ingestionmanagement.transaction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.openefms.common.events.EventEnvelope;
import org.openefms.common.events.EventMetadata;
import org.openefms.common.events.EventTypes;
import org.openefms.common.events.rabbitmq.RabbitMqExchanges;
import org.openefms.common.events.rabbitmq.RabbitMqHeaders;
import org.openefms.common.events.rabbitmq.RabbitMqRoutingKeys;
import org.openefms.common.events.transaction.TransactionReceivedEvent;
import org.openefms.common.models.transaction.TransactionAmount;
import org.openefms.common.models.transaction.TransactionChannel;
import org.openefms.common.models.transaction.TransactionDirection;
import org.openefms.common.models.transaction.TransactionIngestionRequest;
import org.openefms.common.models.transaction.TransactionParty;
import org.openefms.common.models.transaction.TransactionType;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

class RabbitMqTransactionEventPublisherTest {
    private static final UUID EVENT_ID = UUID.fromString("7e37875e-7443-47d4-931c-df8ce0f10cab");

    @Test
    void publishesTransactionEventWithBrokerHeaders() {
        CapturingRabbitTemplate rabbitTemplate = new CapturingRabbitTemplate();
        RabbitMqTransactionEventPublisher publisher = new RabbitMqTransactionEventPublisher(rabbitTemplate);
        TransactionIngestionMessage message = transactionMessage();

        publisher.publish(message);

        assertEquals(RabbitMqExchanges.DOMAIN_EVENTS, rabbitTemplate.exchangeName);
        assertEquals(RabbitMqRoutingKeys.TRANSACTION_RECEIVED, rabbitTemplate.routingKey);
        assertSame(message.envelope(), rabbitTemplate.payload);
        assertEquals(MessageProperties.CONTENT_TYPE_JSON, rabbitTemplate.messageProperties.getContentType());
        assertEquals(MessageDeliveryMode.PERSISTENT, rabbitTemplate.messageProperties.getDeliveryMode());
        assertEquals(EVENT_ID.toString(), rabbitTemplate.messageProperties.getMessageId());
        assertEquals(EventTypes.TRANSACTION_RECEIVED, rabbitTemplate.messageProperties.getType());
        assertEquals(EVENT_ID.toString(), rabbitTemplate.header(RabbitMqHeaders.EVENT_ID));
        assertEquals(EventTypes.TRANSACTION_RECEIVED, rabbitTemplate.header(RabbitMqHeaders.EVENT_TYPE));
        assertEquals(1, rabbitTemplate.header(RabbitMqHeaders.EVENT_VERSION));
        assertEquals("ingestion-management-service", rabbitTemplate.header(RabbitMqHeaders.SOURCE_SERVICE));
        assertEquals("tenant-a", rabbitTemplate.header(RabbitMqHeaders.TENANT_ID));
        assertEquals("correlation-1", rabbitTemplate.header(RabbitMqHeaders.CORRELATION_ID));
        assertEquals("trace-1", rabbitTemplate.header(RabbitMqHeaders.TRACE_ID));
    }

    @Test
    void wrapsRabbitFailuresWithPublicationContext() {
        RabbitMqTransactionEventPublisher publisher = new RabbitMqTransactionEventPublisher(new FailingRabbitTemplate());

        TransactionEventPublicationException exception = assertThrows(
                TransactionEventPublicationException.class,
                () -> publisher.publish(transactionMessage()));

        assertEquals(EventTypes.TRANSACTION_RECEIVED, exception.eventType());
        assertEquals(RabbitMqExchanges.DOMAIN_EVENTS, exception.exchangeName());
        assertEquals(RabbitMqRoutingKeys.TRANSACTION_RECEIVED, exception.routingKey());
    }

    private TransactionIngestionMessage transactionMessage() {
        TransactionIngestionRequest transaction = new TransactionIngestionRequest(
                UUID.fromString("118f3bd4-b208-44ff-b1ff-447c78d80d02"),
                "EXT-001",
                "tenant-a",
                TransactionType.TRANSFER,
                TransactionChannel.MOBILE,
                TransactionDirection.OUTBOUND,
                new TransactionAmount(new BigDecimal("25.00"), "GBP"),
                party("originator"),
                party("beneficiary"),
                OffsetDateTime.parse("2026-09-11T08:58:00Z"),
                Map.of());
        EventEnvelope<TransactionReceivedEvent> envelope = new EventEnvelope<>(
                new EventMetadata(
                        EVENT_ID,
                        EventTypes.TRANSACTION_RECEIVED,
                        1,
                        OffsetDateTime.parse("2026-09-11T09:00:00Z"),
                        "ingestion-management-service",
                        "tenant-a",
                        "correlation-1",
                        null,
                        "trace-1",
                        Map.of()),
                new TransactionReceivedEvent(transaction));

        return new TransactionIngestionMessage(
                RabbitMqExchanges.DOMAIN_EVENTS,
                RabbitMqRoutingKeys.TRANSACTION_RECEIVED,
                envelope);
    }

    private TransactionParty party(String reference) {
        return new TransactionParty(reference, reference, reference, reference + "-hash", "BANK", "GB", Map.of());
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
