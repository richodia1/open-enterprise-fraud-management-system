package org.openefms.ingestionmanagement.transaction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.openefms.common.events.EventTypes;
import org.openefms.common.events.rabbitmq.RabbitMqExchanges;
import org.openefms.common.events.rabbitmq.RabbitMqRoutingKeys;
import org.openefms.common.models.transaction.TransactionAmount;
import org.openefms.common.models.transaction.TransactionChannel;
import org.openefms.common.models.transaction.TransactionDirection;
import org.openefms.common.models.transaction.TransactionIngestionRequest;
import org.openefms.common.models.transaction.TransactionParty;
import org.openefms.common.models.transaction.TransactionType;
import org.openefms.ingestionmanagement.messaging.IngestionRabbitMqProperties;

class TransactionIngestionServiceTest {
    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-09-11T09:00:00Z"), ZoneOffset.UTC);
    private static final UUID TRANSACTION_ID = UUID.fromString("118f3bd4-b208-44ff-b1ff-447c78d80d02");

    @Test
    void receivesValidTransactionsAndPreparesTransportMessage() {
        CapturingTransactionEventPublisher publisher = new CapturingTransactionEventPublisher();
        TransactionIngestionService service = service(publisher);

        TransactionIngestionResult result = service.receive(validTransaction(), " correlation-1 ", " trace-1 ");

        assertEquals(TRANSACTION_ID, result.response().transactionId());
        assertEquals("EXT-001", result.response().externalReference());
        assertEquals("tenant-a", result.response().tenantId());
        assertEquals(TransactionIngestionService.RECEIVED_STATUS, result.response().status());
        assertEquals(EventTypes.TRANSACTION_RECEIVED, result.response().eventType());
        assertEquals("correlation-1", result.response().correlationId());
        assertEquals(OffsetDateTime.parse("2026-09-11T09:00:00Z"), result.response().acceptedAt());
        assertEquals("published", result.response().attributes().get("eventPublication"));

        assertEquals(1, publisher.publishCount());
        assertEquals(result.message(), publisher.lastMessage());
        assertEquals(RabbitMqExchanges.DOMAIN_EVENTS, result.message().exchangeName());
        assertEquals(RabbitMqRoutingKeys.TRANSACTION_RECEIVED, result.message().routingKey());
        assertEquals(EventTypes.TRANSACTION_RECEIVED, result.message().envelope().metadata().eventType());
        assertEquals(1, result.message().envelope().metadata().eventVersion());
        assertEquals("ingestion-management-service", result.message().envelope().metadata().sourceService());
        assertEquals("tenant-a", result.message().envelope().metadata().tenantId());
        assertEquals("correlation-1", result.message().envelope().metadata().correlationId());
        assertEquals("trace-1", result.message().envelope().metadata().traceId());
        assertEquals(TRANSACTION_ID, result.message().envelope().payload().transaction().transactionId());
        assertEquals("GBP", result.message().envelope().payload().transaction().amount().currency());
        assertEquals("GB", result.message().envelope().payload().transaction().originator().countryCode());
    }

    @Test
    void createsCorrelationIdWhenOneIsNotSupplied() {
        TransactionIngestionService service = service(new CapturingTransactionEventPublisher());

        TransactionIngestionResult result = service.receive(validTransaction(), null, null);

        assertNotNull(result.response().correlationId());
        assertFalse(result.response().correlationId().isBlank());
        assertEquals(result.response().correlationId(), result.message().envelope().metadata().correlationId());
    }

    @Test
    void rejectsInvalidTransactions() {
        CapturingTransactionEventPublisher publisher = new CapturingTransactionEventPublisher();
        TransactionIngestionService service = service(publisher);
        TransactionIngestionRequest invalidTransaction = new TransactionIngestionRequest(
                TRANSACTION_ID,
                "EXT-001",
                " ",
                TransactionType.TRANSFER,
                TransactionChannel.MOBILE,
                TransactionDirection.OUTBOUND,
                new TransactionAmount(BigDecimal.ZERO, "12"),
                new TransactionParty(null, null, null, null, null, "GBR", Map.of()),
                null,
                OffsetDateTime.parse("2026-09-11T08:58:00Z"),
                Map.of());

        InvalidTransactionIngestionRequestException exception = assertThrows(
                InvalidTransactionIngestionRequestException.class,
                () -> service.receive(invalidTransaction, "correlation-1", null));

        assertTrue(exception.errors().contains("tenantId must not be blank"));
        assertTrue(exception.errors().contains("amount.value must be greater than zero"));
        assertTrue(exception.errors().contains("amount.currency must be a three-letter ISO 4217 code"));
        assertTrue(exception.errors().contains("originator must include at least one reference"));
        assertTrue(exception.errors().contains("originator.countryCode must be a two-letter ISO 3166 country code"));
        assertEquals(0, publisher.publishCount());
    }

    private TransactionIngestionService service(TransactionEventPublisher publisher) {
        return new TransactionIngestionService(
                new TransactionIngestionRequestValidator(),
                publisher,
                new IngestionRabbitMqProperties(null, null),
                FIXED_CLOCK);
    }

    private TransactionIngestionRequest validTransaction() {
        return new TransactionIngestionRequest(
                TRANSACTION_ID,
                " EXT-001 ",
                " tenant-a ",
                TransactionType.TRANSFER,
                TransactionChannel.MOBILE,
                TransactionDirection.OUTBOUND,
                new TransactionAmount(new BigDecimal("25.00"), "gbp"),
                party(" originator ", "gb"),
                party(" beneficiary ", "gb"),
                OffsetDateTime.parse("2026-09-11T08:58:00Z"),
                Map.of(" sourceSystem ", " core-banking "));
    }

    private TransactionParty party(String reference, String countryCode) {
        return new TransactionParty(reference, reference, reference, reference + "-hash", "BANK", countryCode, Map.of());
    }

    private static final class CapturingTransactionEventPublisher implements TransactionEventPublisher {
        private TransactionIngestionMessage lastMessage;
        private int publishCount;

        @Override
        public void publish(TransactionIngestionMessage message) {
            this.lastMessage = message;
            this.publishCount++;
        }

        private TransactionIngestionMessage lastMessage() {
            return lastMessage;
        }

        private int publishCount() {
            return publishCount;
        }
    }
}
