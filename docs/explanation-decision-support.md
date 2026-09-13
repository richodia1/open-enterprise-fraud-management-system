# Explanation Decision Support

Explainable AI Service now provides the first investigator-facing explanation stage in the Phase 2 event flow.

## Consumed Event

| Field | Value |
| --- | --- |
| Queue | `oefms.explainable-ai.fraud-decision-produced` |
| Exchange | `oefms.events` |
| Routing key | `fraud.decision.produced` |
| Payload | `EventEnvelope<FraudDecisionProducedEvent>` |
| Supported event version | `1` |

The listener validates the incoming event type and event version before preparing decision support.

## Explanation Builder

`DecisionSupportExplanationService` builds an `ExplanationResponse` from the fraud decision:

| Input | Evidence Type |
| --- | --- |
| `RuleHit` | `RULE` |
| `ModelSignal` | `MODEL_SIGNAL` |
| No rule or model evidence | `BASELINE_DECISION` |

The first builder is deterministic. It does not call an external AI model or ML explainer yet. That keeps the early MVP auditable and makes the next ML work easier to test.

Explanation summaries include the decision outcome, risk band, risk score, recommended transaction-core action, and top evidence. This lets a user or upstream transaction system understand why a transaction should proceed, be held, be rejected, or be escalated.

## REST Contracts

Explainable AI Service also exposes REST contracts for Swagger inspection and future synchronous feedback adapters:

```text
POST /api/v1/explanations/fraud-decisions
POST /api/v1/explanations/fraud-decisions/feedback
```

The first endpoint returns an `ExplanationResponse`. The second returns a `TransactionFraudFeedbackResponse`, combining the fraud decision, recommended action, explanation summary, and evidence items.

## Published Event

| Field | Value |
| --- | --- |
| Exchange | `oefms.events` |
| Routing key | `explanation.prepared` |
| Payload | `EventEnvelope<ExplanationPreparedEvent>` |
| Event version | `1` |
| Source service | `explainable-ai-service` |

`ExplanationOrchestrator` preserves the source fraud decision event correlation id and trace id. It sets the explanation event causation id to the source `fraud.decision.produced` event id.

## Processing Path

```text
RabbitMQ fraud.decision.produced event
        |
        v
FraudDecisionProducedEventListener
        |
        | validates event type and version
        v
ExplanationOrchestrator
        |
        | builds explanation response
        v
RabbitMqExplanationEventPublisher
        |
        v
RabbitMQ explanation.prepared event
        |
        v
Future integration adapter publishes feedback to the transaction core
```

## Still To Add

- Persistence for explanation records.
- Idempotency storage for consumed fraud decision event ids.
- Outbox-backed publication.
- SHAP or feature-contribution support for real ML models.
- Durable explanation storage before publishing `explanation.prepared`.
- Transaction-core callback, queue, or topic adapters using `TransactionFraudFeedbackResponse`.
- Broader audit events across ingestion, decision, explanation, and investigator actions.
