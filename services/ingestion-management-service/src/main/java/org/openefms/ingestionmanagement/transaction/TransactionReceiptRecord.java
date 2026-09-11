package org.openefms.ingestionmanagement.transaction;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.openefms.common.models.transaction.TransactionChannel;
import org.openefms.common.models.transaction.TransactionDirection;
import org.openefms.common.models.transaction.TransactionType;

public record TransactionReceiptRecord(
        UUID transactionId,
        String externalReference,
        String tenantId,
        TransactionType transactionType,
        TransactionChannel channel,
        TransactionDirection direction,
        BigDecimal amount,
        String currency,
        OffsetDateTime initiatedAt,
        OffsetDateTime acceptedAt,
        TransactionReceiptStatus status,
        String eventType,
        UUID eventId,
        String correlationId,
        String traceId,
        OffsetDateTime publishedAt,
        Map<String, String> attributes
) {
    public TransactionReceiptRecord {
        Objects.requireNonNull(transactionId, "transactionId must not be null");
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        Objects.requireNonNull(transactionType, "transactionType must not be null");
        Objects.requireNonNull(channel, "channel must not be null");
        Objects.requireNonNull(direction, "direction must not be null");
        Objects.requireNonNull(amount, "amount must not be null");
        Objects.requireNonNull(currency, "currency must not be null");
        Objects.requireNonNull(initiatedAt, "initiatedAt must not be null");
        Objects.requireNonNull(acceptedAt, "acceptedAt must not be null");
        Objects.requireNonNull(status, "status must not be null");
        Objects.requireNonNull(eventType, "eventType must not be null");
        Objects.requireNonNull(eventId, "eventId must not be null");
        Objects.requireNonNull(correlationId, "correlationId must not be null");
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }
}
