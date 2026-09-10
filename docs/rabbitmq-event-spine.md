# RabbitMQ Event Spine

The RabbitMQ event spine defines the first broker conventions for Phase 1. It does not add service workflows, listeners, publishers, entities, retry code, or business rules yet.

## Exchange Names

| Exchange | Purpose |
| --- | --- |
| `oefms.events` | Topic exchange for domain events. |
| `oefms.events.dlx` | Dead-letter exchange for rejected or expired messages. |

The exchange names are exposed in `RabbitMqExchanges`.

The initial shared dead-letter queue name is `oefms.dead-letter` and is exposed in `RabbitMqQueues`.

## Routing Keys

Routing keys mirror the event names in `EventTypes`.

| Event Type | Routing Key |
| --- | --- |
| `transaction.received` | `transaction.received` |
| `fraud.decision.produced` | `fraud.decision.produced` |
| `explanation.prepared` | `explanation.prepared` |
| `alert.created` | `alert.created` |
| `case.created` | `case.created` |
| `audit.event.recorded` | `audit.event.recorded` |

Use `EventMetadata.eventVersion` for versioning instead of embedding a version suffix in routing keys.

## Initial Bindings

| Consumer Service | Queue | Routing Key |
| --- | --- | --- |
| Fraud Detection Service | `oefms.fraud-detection.transaction-received` | `transaction.received` |
| Explainable AI Service | `oefms.explainable-ai.fraud-decision-produced` | `fraud.decision.produced` |
| Case Management Service | `oefms.case-management.explanation-prepared` | `explanation.prepared` |
| Notification Management Service | `oefms.notification-management.alert-created` | `alert.created` |
| Notification Management Service | `oefms.notification-management.case-created` | `case.created` |
| Audit Logging Service | `oefms.audit-logging.audit-event-recorded` | `audit.event.recorded` |

The binding list is exposed through `RabbitMqTopology.domainEventBindings()`.

## Header Contract

Publishers should set these broker headers in addition to the `EventEnvelope.metadata` payload:

| Header | Source |
| --- | --- |
| `X-OEFMS-Event-Id` | `EventMetadata.eventId` |
| `X-OEFMS-Event-Type` | `EventMetadata.eventType` |
| `X-OEFMS-Event-Version` | `EventMetadata.eventVersion` |
| `X-OEFMS-Source-Service` | `EventMetadata.sourceService` |
| `X-OEFMS-Tenant-Id` | `EventMetadata.tenantId` |
| `X-Correlation-Id` | `EventMetadata.correlationId` |
| `X-OEFMS-Causation-Id` | `EventMetadata.causationId` |
| `X-OEFMS-Trace-Id` | `EventMetadata.traceId` |

The header names are exposed in `RabbitMqHeaders`.

## Publisher Convention

Publishers should:

- Publish `EventEnvelope<T>` as JSON.
- Publish to the `oefms.events` exchange.
- Use `RabbitMqTopology.routingKeyFor(envelope.metadata().eventType())`.
- Set `contentType` to `application/json`.
- Mark domain-event messages as persistent.
- Copy correlation, causation, trace, tenant, source-service, event id, event type, and event version into broker headers.
- Preserve the original correlation id when publishing follow-up events.
- Set the causation id to the event id that caused the new event when available.

## Consumer Convention

Consumers should:

- Treat `EventEnvelope.metadata.eventId` as the idempotency key.
- Validate the event type and event version before processing.
- Reject malformed events so they can move to the dead-letter path.
- Preserve correlation and trace metadata in logs, audit events, and follow-up events.
- Own their queue and service-specific retry policy.
- Keep broker listeners thin and delegate business decisions to service-owned application code.

## Current Scope

Phase 1 only defines names, bindings, headers, and runtime configuration. The concrete RabbitMQ publishers, listeners, topology declaration beans, retry policies, and dead-letter handling should be added in service-specific implementation branches.
