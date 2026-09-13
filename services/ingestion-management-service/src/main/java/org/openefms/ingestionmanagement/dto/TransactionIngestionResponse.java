package org.openefms.ingestionmanagement.dto;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public record TransactionIngestionResponse(
        UUID transactionId,
        String externalReference,
        String tenantId,
        String status,
        String eventType,
        String correlationId,
        OffsetDateTime acceptedAt,
        Map<String, String> attributes
) {
    public TransactionIngestionResponse {
        Objects.requireNonNull(transactionId, "transactionId must not be null");
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        Objects.requireNonNull(status, "status must not be null");
        Objects.requireNonNull(eventType, "eventType must not be null");
        Objects.requireNonNull(correlationId, "correlationId must not be null");
        Objects.requireNonNull(acceptedAt, "acceptedAt must not be null");
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }
}
