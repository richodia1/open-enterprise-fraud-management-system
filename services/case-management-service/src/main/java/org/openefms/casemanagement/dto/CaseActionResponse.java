package org.openefms.casemanagement.dto;

import org.openefms.casemanagement.entity.CaseActionRecord;
import org.openefms.casemanagement.entity.CaseRecord;

public record CaseActionResponse(
        CaseRecord caseFile,
        CaseActionRecord action
) {
}
