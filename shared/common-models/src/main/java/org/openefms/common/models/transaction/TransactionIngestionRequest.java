package org.openefms.common.models.transaction;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * First shared request contract for normalised transaction ingestion.
 */
public record TransactionIngestionRequest(
        UUID transactionId,
        String externalReference,
        String tenantId,
        TransactionType transactionType,
        TransactionChannel channel,
        TransactionDirection direction,
        TransactionAmount amount,
        TransactionParty originator,
        TransactionParty beneficiary,
        OffsetDateTime initiatedAt,
        Map<String, String> attributes
) {
    public TransactionIngestionRequest {
        Objects.requireNonNull(transactionId, "transactionId must not be null");
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        Objects.requireNonNull(transactionType, "transactionType must not be null");
        Objects.requireNonNull(channel, "channel must not be null");
        Objects.requireNonNull(direction, "direction must not be null");
        Objects.requireNonNull(amount, "amount must not be null");
        Objects.requireNonNull(initiatedAt, "initiatedAt must not be null");
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }
}
