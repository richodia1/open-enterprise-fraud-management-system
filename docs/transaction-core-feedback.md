# Transaction Core Feedback Contract

OEFMS is a fraud decision-support platform. It does not own final transaction state, settlement, reversal, or payout execution. The upstream transaction core remains the system of record for transaction status.

## Recommended Processing Pattern

Financial institutions should keep a submitted transaction in an interim state while OEFMS evaluates it.

```text
Transaction submitted
        |
        v
Transaction core marks it PROCESSING or PENDING_FRAUD_DECISION
        |
        v
Transaction core submits the transaction to OEFMS
        |
        v
OEFMS returns or publishes a fraud decision
        |
        v
Transaction core applies its own final status transition
```

## Decision To Core Action Mapping

`FraudDecisionResponse.recommendedAction` is the generic instruction for the transaction core.

| OEFMS outcome | Recommended transaction-core action | Typical core status |
| --- | --- | --- |
| `APPROVE` | `PROCEED_TO_COMPLETION` | `COMPLETED` or `PENDING_PAYOUT` |
| `REVIEW` | `HOLD_FOR_MANUAL_REVIEW` | `HELD_FOR_REVIEW` |
| `BLOCK` | `REJECT_TRANSACTION` | `BLOCKED`, `REJECTED`, or `CANCELLED` |
| `ESCALATE` | `ESCALATE_FOR_INVESTIGATION` | `ESCALATED` or `PENDING_INVESTIGATION` |

Banks may map these actions to their own status names, approval policies, and settlement rules.

## Synchronous Prevention

Use the decision API when the transaction core needs an immediate answer before completing the transaction.

```text
POST /api/v1/fraud/decisions
```

The transaction core should wait for the response and then apply its own state transition.

## Asynchronous Feedback

Use ingestion and events when the transaction core can submit the transaction and consume the result later.

```text
POST /api/v1/ingestion/transactions
        |
        v
transaction.received
        |
        v
fraud.decision.produced
```

The consuming core or integration adapter should subscribe to `fraud.decision.produced`, read `recommendedAction`, and update the source transaction.

## Timeout Policy

If OEFMS does not return a decision within the bank's allowed window, the transaction core must apply its own policy. Recommended options are:

- hold the transaction for manual review
- retry the fraud decision request
- fail closed for high-risk channels
- fail open only for explicitly approved low-risk flows

The timeout policy should be configured by each institution and included in audit records.
