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

For a richer response, the integration adapter should wait for `explanation.prepared` and send a `TransactionFraudFeedbackResponse` to the transaction core. That payload includes:

- the transaction id and decision id
- the fraud outcome
- the recommended transaction-core action
- the risk score and risk band
- the explanation summary
- the evidence items behind the decision

The same response can be generated through the Explainable AI REST contract:

```text
POST /api/v1/explanations/fraud-decisions/feedback
```

Example response shape:

```json
{
  "transactionId": "118f3bd4-b208-44ff-b1ff-447c78d80d02",
  "decisionId": "325dc0f5-775d-4858-89be-1992e52fe2f0",
  "outcome": "BLOCK",
  "recommendedAction": "REJECT_TRANSACTION",
  "riskScore": {
    "value": 0.7,
    "band": "HIGH",
    "reason": "3 baseline rule indicator(s) matched."
  },
  "explanationSummary": "Decision BLOCK with HIGH risk score 0.70. Recommended action: REJECT_TRANSACTION. Key evidence: High value amount, Velocity review threshold, Cross-border parties."
}
```

## Timeout Policy

If OEFMS does not return a decision within the bank's allowed window, the transaction core must apply its own policy. Recommended options are:

- hold the transaction for manual review
- retry the fraud decision request
- fail closed for high-risk channels
- fail open only for explicitly approved low-risk flows

The timeout policy should be configured by each institution and included in audit records.
