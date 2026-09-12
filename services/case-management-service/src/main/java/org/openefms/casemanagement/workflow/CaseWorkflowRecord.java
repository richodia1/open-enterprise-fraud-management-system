package org.openefms.casemanagement.workflow;

import java.util.Objects;

record CaseWorkflowRecord(
        AlertRecord alert,
        CaseRecord caseFile
) {
    CaseWorkflowRecord {
        Objects.requireNonNull(alert, "alert must not be null");
        Objects.requireNonNull(caseFile, "caseFile must not be null");
    }
}
