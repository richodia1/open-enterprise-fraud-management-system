package org.openefms.casemanagement.client;

public class CaseAssignmentDirectoryUnavailableException extends RuntimeException {
    public CaseAssignmentDirectoryUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }

    public CaseAssignmentDirectoryUnavailableException(String message) {
        super(message);
    }
}
