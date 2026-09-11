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
```

## Still To Add

- Persistence for explanation records.
- Idempotency storage for consumed fraud decision event ids.
- Outbox-backed publication.
- SHAP or feature-contribution support for real ML models.
- Persistent Case Management records after the first `explanation.prepared` consumer.
- Audit logging across ingestion, decision, explanation, and case creation.
