package org.openefms.ingestionmanagement.transaction;

import java.util.Objects;

record TransactionIngestionResult(
        TransactionIngestionResponse response,
        TransactionIngestionMessage message
) {
    TransactionIngestionResult {
        Objects.requireNonNull(response, "response must not be null");
        Objects.requireNonNull(message, "message must not be null");
    }
}
