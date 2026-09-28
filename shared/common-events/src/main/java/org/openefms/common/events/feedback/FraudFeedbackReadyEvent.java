package org.openefms.common.events.feedback;

import java.util.Objects;
import org.openefms.common.models.feedback.TransactionFraudFeedbackResponse;

/**
 * Event emitted when a transaction-core feedback package is ready to consume.
 */
public record FraudFeedbackReadyEvent(
        TransactionFraudFeedbackResponse feedback
) {
    public FraudFeedbackReadyEvent {
        Objects.requireNonNull(feedback, "feedback must not be null");
    }
}
