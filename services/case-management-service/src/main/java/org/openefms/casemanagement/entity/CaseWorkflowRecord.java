package org.openefms.casemanagement.entity;

import java.util.Objects;

public record CaseWorkflowRecord(
        AlertRecord alert,
        CaseRecord caseFile
) {
    public CaseWorkflowRecord {
        Objects.requireNonNull(alert, "alert must not be null");
        Objects.requireNonNull(caseFile, "caseFile must not be null");
    }
}
