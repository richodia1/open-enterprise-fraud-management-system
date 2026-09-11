package org.openefms.ingestionmanagement.transaction;

class TransactionReceiptPersistenceException extends RuntimeException {
    TransactionReceiptPersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
