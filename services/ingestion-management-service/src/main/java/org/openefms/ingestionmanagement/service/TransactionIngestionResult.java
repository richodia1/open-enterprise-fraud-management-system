package org.openefms.ingestionmanagement.service;

import java.util.Objects;
import org.openefms.ingestionmanagement.dto.TransactionIngestionResponse;
import org.openefms.ingestionmanagement.messaging.TransactionIngestionMessage;

public record TransactionIngestionResult(
        TransactionIngestionResponse response,
        TransactionIngestionMessage message
) {
    public TransactionIngestionResult {
        Objects.requireNonNull(response, "response must not be null");
        Objects.requireNonNull(message, "message must not be null");
    }
}
