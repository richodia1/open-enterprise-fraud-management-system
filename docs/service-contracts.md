# Service Contracts

Phase 1 contracts define the first API and event shapes without adding persistence entities or service workflows.

The contracts live in shared modules:

| Module | Purpose |
| --- | --- |
| `shared/common-models` | API/data contracts used by services. |
| `shared/common-events` | Event envelope, metadata, event type names, typed event payloads, and RabbitMQ naming conventions. |

These modules should stay small. Do not add repositories, JPA annotations, service logic, or institution-specific behavior to shared contracts.

## Contract Packages

| Package | Responsibility |
| --- | --- |
| `org.openefms.common.models.transaction` | Transaction ingestion request, parties, amount, channel, direction, and type. |
| `org.openefms.common.models.fraud` | Fraud decision response, risk score, risk band, decision mode, decision outcome, rule hits, and model signals. |
| `org.openefms.common.models.explanation` | Explanation request, explanation response, and evidence items. |
| `org.openefms.common.models.alert` | Alert creation request and priority. |
| `org.openefms.common.models.casefile` | Case creation request and priority. |
| `org.openefms.common.models.audit` | Audit event record and audit action. |
| `org.openefms.common.events` | Generic event envelope, metadata, and event type constants. |
| `org.openefms.common.events.rabbitmq` | RabbitMQ exchange, queue, routing-key, binding, and header conventions. |

## Initial Flow Contracts

The first vertical slice should use these contracts:

```text
TransactionIngestionRequest
        |
        v
EventEnvelope<TransactionReceivedEvent>
        |
        v
FraudDecisionResponse
        |
        v
EventEnvelope<FraudDecisionProducedEvent>
        |
        v
ExplanationRequest / ExplanationResponse
        |
        v
EventEnvelope<ExplanationPreparedEvent>
        |
        v
AlertCreationRequest
        |
        v
EventEnvelope<AlertCreatedEvent>
        |
        v
CaseCreationRequest
        |
        v
EventEnvelope<CaseCreatedEvent>
```

Audit events should use:

```text
AuditEventRecord
        |
        v
EventEnvelope<AuditEventRecordedEvent>
```

## Event Naming

Initial event names are exposed through `EventTypes`.

| Constant | Event Name |
| --- | --- |
| `TRANSACTION_RECEIVED` | `transaction.received` |
| `FRAUD_DECISION_PRODUCED` | `fraud.decision.produced` |
| `EXPLANATION_PREPARED` | `explanation.prepared` |
| `ALERT_CREATED` | `alert.created` |
| `CASE_CREATED` | `case.created` |
| `AUDIT_EVENT_RECORDED` | `audit.event.recorded` |

Use `EventMetadata.eventVersion` for versioning instead of embedding a version suffix in the event name.

See [RabbitMQ Event Spine](rabbitmq-event-spine.md) for the first broker exchange, queue, routing-key, binding, and header conventions.

## Contract Rules

- Use Java records and enums for shared contracts.
- Keep contracts serialisation-friendly and free of persistence annotations.
- Prefer external references, hashes, and metadata maps over raw personally identifiable information.
- Keep tenant, correlation, causation, trace, and source-service metadata on events.
- Add new shared fields only when more than one service needs them.
- Keep service-owned workflow decisions inside the owning service.
- Treat ML fields as decision-support signals until trained models, validation, governance, and serving are implemented.

Explainable AI Service now uses the explanation contracts to build deterministic decision-support evidence from `FraudDecisionProducedEvent` before publishing `ExplanationPreparedEvent`.
