package org.openefms.ingestionmanagement.service;

import java.util.List;

public class InvalidTransactionIngestionRequestException extends RuntimeException {
    private final List<String> errors;

    public InvalidTransactionIngestionRequestException(List<String> errors) {
        super("Transaction ingestion request failed validation.");
        this.errors = List.copyOf(errors);
    }

    public List<String> errors() {
        return errors;
    }
}
