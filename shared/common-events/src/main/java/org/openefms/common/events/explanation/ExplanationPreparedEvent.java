package org.openefms.common.events.explanation;

import org.openefms.common.models.explanation.ExplanationResponse;

import java.util.Objects;

/**
 * Event emitted after an explanation pack is prepared.
 */
public record ExplanationPreparedEvent(
        ExplanationResponse explanation
) {
    public ExplanationPreparedEvent {
        Objects.requireNonNull(explanation, "explanation must not be null");
    }
}
