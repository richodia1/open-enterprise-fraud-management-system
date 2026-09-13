package org.openefms.ingestionmanagement.service;

import java.util.UUID;

public class TransactionReceiptNotFoundException extends RuntimeException {
    public TransactionReceiptNotFoundException(UUID transactionId) {
        super("Transaction receipt " + transactionId + " was not found.");
    }
}
