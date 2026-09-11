# Fraud Detection Core

Fraud Detection Core is the first implementation of a preliminary fraud-check path.

It is intentionally small: the service now stores a durable decision summary, but no external rule engine, production ML scorer, outbox, idempotency store, or production fraud policy has been added yet.

## HTTP Decision Endpoint

Fraud Detection Service exposes:

```text
POST /api/v1/fraud/decisions
```

Request body:

```text
TransactionIngestionRequest
```

Response body:

```text
FraudDecisionResponse
```

The endpoint uses the shared transaction and fraud contracts in `common-models`. Successful decisions are stored in the service-owned `fraud_decisions` table.

## Decision Record Reads

Fraud Detection Service exposes the first decision-history reads:

```text
GET /api/v1/fraud/decisions/{decisionId}
GET /api/v1/fraud/decisions?transactionId={transactionId}
GET /api/v1/fraud/decisions?tenantId={tenantId}
```

These endpoints return a persisted `FraudDecisionRecord` with transaction identity, tenant, mode, outcome, score, risk band, evidence summary, event metadata, and decision attributes.

## Default Decision Mode

The default mode is:

```text
OEFMS_FRAUD_DECISION_MODE=rules
```

`rules` mode is the default and fully runnable. `ml` and `hybrid` are routed through the `MlScoringClient` contract, but the default runtime returns `501 Not Implemented` until a production scorer adapter, validation, explainability, and governance are implemented.

## Baseline Rule Indicators

The first baseline rule engine produces deterministic `RuleHit` entries from:

| Rule Code | Purpose |
| --- | --- |
| `HIGH_VALUE_AMOUNT` | Amount is at or above the high-value threshold. |
| `CRITICAL_VALUE_AMOUNT` | Amount is at or above the critical-value threshold. |
| `VELOCITY_REVIEW_THRESHOLD` | Recent transaction count is at or above the review threshold. |
| `VELOCITY_BLOCK_THRESHOLD` | Recent transaction count is at or above the block threshold. |
| `CROSS_BORDER_PARTIES` | Originator and beneficiary country codes differ. |
| `HIGH_RISK_HINT` | Input attributes carry an upstream high-risk hint. |
| `UNKNOWN_TRANSACTION_CHANNEL` | Transaction arrived through the generic `OTHER` channel. |

Velocity can be supplied through any of these request attributes:

```text
velocityCount
recentTransactionCount
transactionsInLastHour
```

High-risk hints can be supplied through:

```text
riskHint=high
highRisk=true
```

## Score And Outcome

Rule contributions are summed and capped at `1.00`.

| Score Range | Risk Band | Outcome |
| --- | --- | --- |
| Below review threshold | `LOW` | `APPROVE` |
| At or above review threshold | `MEDIUM` | `REVIEW` |
| At or above block threshold | `HIGH` | `BLOCK` |
| At or above escalation threshold | `CRITICAL` | `ESCALATE` |

Thresholds are environment-driven and documented in [Platform Configuration](platform-configuration.md).

Rule Management now exposes matching baseline rule definitions through `GET /api/v1/rules`. Fraud Detection does not read the Rule Management catalog yet; dynamic rule lookup will be added after the API contract has settled.

## ML Runtime Boundary

Fraud Detection Service exposes:

```text
GET /api/v1/fraud/ml/runtime
```

This reports the configured ML provider, model name, runtime state, supported modes, and score threshold.

Current local defaults report `DISABLED`. If a provider name is configured without a scorer implementation, the service reports `CONTRACT_ONLY`.

See [ML Runtime Contract](ml-runtime-contract.md) for the request/response contracts and future adapter expectations.

## Event Boundary

The orchestration layer can turn an `EventEnvelope<TransactionReceivedEvent>` into a transport-ready `FraudDecisionMessage`.

That message includes:

- Exchange: `oefms.events`
- Routing key: `fraud.decision.produced`
- Payload: `EventEnvelope<FraudDecisionProducedEvent>`

Fraud Detection Service now has a RabbitMQ listener for `transaction.received` events. The listener validates the event type and version, delegates scoring to the same orchestration layer as the HTTP endpoint, stores the decision record, and hands the prepared decision message to a RabbitMQ publisher.

The publisher emits `fraud.decision.produced` with persistent delivery and the shared broker header contract.
