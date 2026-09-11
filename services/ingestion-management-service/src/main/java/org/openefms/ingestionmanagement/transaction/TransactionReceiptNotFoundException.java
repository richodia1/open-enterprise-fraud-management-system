package org.openefms.ingestionmanagement.transaction;

import java.util.UUID;

class TransactionReceiptNotFoundException extends RuntimeException {
    TransactionReceiptNotFoundException(UUID transactionId) {
        super("Transaction receipt " + transactionId + " was not found.");
    }
}
