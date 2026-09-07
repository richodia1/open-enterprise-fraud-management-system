package org.openefms.common.events.alert;

import org.openefms.common.models.alert.AlertCreationRequest;

import java.util.Objects;

/**
 * Event emitted after an alert is created from a fraud decision.
 */
public record AlertCreatedEvent(
        AlertCreationRequest alert
) {
    public AlertCreatedEvent {
        Objects.requireNonNull(alert, "alert must not be null");
    }
}
