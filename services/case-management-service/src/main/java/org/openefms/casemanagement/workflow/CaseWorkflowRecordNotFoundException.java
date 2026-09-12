package org.openefms.casemanagement.workflow;

import java.util.UUID;

class CaseWorkflowRecordNotFoundException extends RuntimeException {
    CaseWorkflowRecordNotFoundException(String recordType, UUID recordId) {
        super(recordType + " record was not found: " + recordId);
    }
}
