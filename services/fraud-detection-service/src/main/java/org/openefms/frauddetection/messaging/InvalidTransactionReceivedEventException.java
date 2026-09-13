package org.openefms.frauddetection.messaging;

import java.util.List;

public class InvalidTransactionReceivedEventException extends RuntimeException {
    public InvalidTransactionReceivedEventException(List<String> errors) {
        super("Invalid transaction.received event: " + String.join("; ", errors));
    }
}
