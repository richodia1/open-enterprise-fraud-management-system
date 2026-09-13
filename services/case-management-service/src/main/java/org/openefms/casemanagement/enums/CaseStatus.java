package org.openefms.casemanagement.enums;

public enum CaseStatus {
    OPEN,
    IN_REVIEW,
    ESCALATED,
    FRAUD_CONFIRMED,
    FALSE_POSITIVE,
    CLOSED;

    public boolean terminal() {
        return this == FRAUD_CONFIRMED || this == FALSE_POSITIVE || this == CLOSED;
    }
}
