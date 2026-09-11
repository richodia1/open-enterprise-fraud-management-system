package org.openefms.frauddetection.transaction;

import java.util.List;

class InvalidTransactionReceivedEventException extends RuntimeException {
    InvalidTransactionReceivedEventException(List<String> errors) {
        super("Invalid transaction.received event: " + String.join("; ", errors));
    }
}
