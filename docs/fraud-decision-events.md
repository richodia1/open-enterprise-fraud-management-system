# Fraud Decision Events

Fraud Detection Service now stores preliminary fraud decisions and publishes them after consuming transaction events.

## Published Event

| Field | Value |
| --- | --- |
| Exchange | `oefms.events` |
| Routing key | `fraud.decision.produced` |
| Payload | `EventEnvelope<FraudDecisionProducedEvent>` |
| Event version | `1` |
| Source service | `fraud-detection-service` |

`FraudDecisionOrchestrator` preserves the source transaction event correlation id and trace id. It sets the decision event causation id to the source `transaction.received` event id.

Before returning the prepared message, the orchestrator stores a `fraud_decisions` record containing the decision outcome, score, evidence summary, and publication event metadata.

## Broker Headers

`RabbitMqFraudDecisionEventPublisher` publishes the decision envelope as JSON and sets the shared OEFMS RabbitMQ headers:

```text
X-OEFMS-Event-Id
X-OEFMS-Event-Type
X-OEFMS-Event-Version
X-OEFMS-Source-Service
X-OEFMS-Tenant-Id
X-Correlation-Id
X-OEFMS-Causation-Id
X-OEFMS-Trace-Id
```

Messages are marked persistent.

## Failure Behaviour

If RabbitMQ cannot accept the decision event, the publisher raises `FraudDecisionPublicationException`. When this happens inside the transaction listener, the consumed transaction message is not acknowledged successfully, allowing the configured RabbitMQ retry or dead-letter path to handle it.

The current local default is:

```text
OEFMS_RABBITMQ_DEFAULT_REQUEUE_REJECTED=false
```

## Still To Add

- Idempotency storage for consumed transaction event ids.
- Outbox-backed publication for persisted fraud decisions.
- Persistent case, alert, and audit storage after the first explanation handoff.
