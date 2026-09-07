package org.openefms.common.events.casefile;

import org.openefms.common.models.casefile.CaseCreationRequest;

import java.util.Objects;

/**
 * Event emitted after an investigation case is opened.
 */
public record CaseCreatedEvent(
        CaseCreationRequest caseFile
) {
    public CaseCreatedEvent {
        Objects.requireNonNull(caseFile, "caseFile must not be null");
    }
}
