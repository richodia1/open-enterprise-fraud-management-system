package org.openefms.casemanagement.service;

import java.util.UUID;

public class CaseWorkflowRecordNotFoundException extends RuntimeException {
    public CaseWorkflowRecordNotFoundException(String recordType, UUID recordId) {
        super(recordType + " record was not found: " + recordId);
    }
}
