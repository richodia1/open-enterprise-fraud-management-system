# Case Alert Audit Flow

Case Management Service now provides the first investigator handoff after Explainable AI prepares an explanation.

## Consumed Event

| Field | Value |
| --- | --- |
| Queue | `oefms.case-management.explanation-prepared` |
| Exchange | `oefms.events` |
| Routing key | `explanation.prepared` |
| Payload | `EventEnvelope<ExplanationPreparedEvent>` |
| Supported event version | `1` |

The listener validates event type and version before creating workflow messages. Reviewable explanations also create durable alert and case records before workflow messages are handed to the RabbitMQ publisher. Audit messages are persisted by Audit Logging Service after they are consumed from RabbitMQ.

## Review Decision

The first workflow opens an alert and case only for explanations that require investigation.

| Explanation Attributes | Behaviour |
| --- | --- |
| `decisionOutcome=APPROVE` and `riskBand=LOW` | Do not open an alert or case. |
| Any other decision outcome or risk band | Open an alert and case. |

## Published Events

For reviewable explanations, Case Management publishes:

| Event | Routing Key | Purpose |
| --- | --- | --- |
| `AlertCreatedEvent` | `alert.created` | Creates the first investigator alert. |
| `CaseCreatedEvent` | `case.created` | Creates the first investigation case linked to the alert. |
| `AuditEventRecordedEvent` | `audit.event.recorded` | Captures alert and case creation audit entries. |

The case event uses the alert event id as its causation id. Audit events use the event id of the workflow event they record as their causation id.

## Durable Alert And Case Records

Case Management Service persists the first review records in its own database:

| Record | Table | Initial Status |
| --- | --- | --- |
| Alert | `fraud_alerts` | `OPEN` |
| Case | `fraud_cases` | `OPEN` |

Read endpoints:

```text
GET /api/v1/alerts/{alertId}
GET /api/v1/alerts?tenantId={tenantId}
GET /api/v1/cases/{caseId}
GET /api/v1/cases?tenantId={tenantId}
```

See [Case Alert Persistence Foundation](case-alert-persistence-foundation.md) for the table shape and persistence flow.

## Audit Logging

Audit Logging Service now consumes:

| Field | Value |
| --- | --- |
| Queue | `oefms.audit-logging.audit-event-recorded` |
| Routing key | `audit.event.recorded` |
| Payload | `EventEnvelope<AuditEventRecordedEvent>` |

The audit sink stores received audit events in the Audit Logging Service database and retains envelope metadata for correlation, causation, source event, source service, and trace lookups.

Read endpoints:

```text
GET /api/v1/audit-events/{auditEventId}
GET /api/v1/audit-events?tenantId={tenantId}
GET /api/v1/audit-events?correlationId={correlationId}
GET /api/v1/audit-events?tenantId={tenantId}&resourceType={resourceType}&resourceId={resourceId}
```

See [Audit Log Persistence Foundation](audit-log-persistence-foundation.md) for the table shape and query flow.

## Processing Path

```text
RabbitMQ explanation.prepared event
        |
        v
ExplanationPreparedEventListener
        |
        | validates event type and version
        v
CaseWorkflowOrchestrator
        |
        | creates alert, case, and audit messages
        | saves fraud_alerts and fraud_cases
        v
RabbitMqCaseWorkflowEventPublisher
        |
        +--> RabbitMQ alert.created
        +--> RabbitMQ case.created
        +--> RabbitMQ audit.event.recorded
                 |
                 v
          AuditEventRecordedEventListener
                 |
                 v
          PersistingAuditEventSink
                 |
                 v
          audit_events
```

## Still To Add

- Idempotency for consumed explanation and audit event ids.
- Notification consumer for `alert.created` and `case.created`.
- Case assignment, status history, investigator actions, and final fraud disposition.
