package org.openefms.common.models.fraud;

import java.util.Objects;

/**
 * Decision-support instruction for the upstream transaction core.
 *
 * OEFMS recommends the action; the transaction core remains the system of record
 * for transaction status and final settlement.
 */
public enum RecommendedTransactionAction {
    PROCEED_TO_COMPLETION,
    HOLD_FOR_MANUAL_REVIEW,
    REJECT_TRANSACTION,
    ESCALATE_FOR_INVESTIGATION;

    public static RecommendedTransactionAction fromOutcome(DecisionOutcome outcome) {
        Objects.requireNonNull(outcome, "outcome must not be null");
        return switch (outcome) {
            case APPROVE -> PROCEED_TO_COMPLETION;
            case REVIEW -> HOLD_FOR_MANUAL_REVIEW;
            case BLOCK -> REJECT_TRANSACTION;
            case ESCALATE -> ESCALATE_FOR_INVESTIGATION;
        };
    }
}
