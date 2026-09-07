package org.openefms.common.events.transaction;

import org.openefms.common.models.transaction.TransactionIngestionRequest;

import java.util.Objects;

/**
 * Event emitted after a transaction has been received and normalised.
 */
public record TransactionReceivedEvent(
        TransactionIngestionRequest transaction
) {
    public TransactionReceivedEvent {
        Objects.requireNonNull(transaction, "transaction must not be null");
    }
}
