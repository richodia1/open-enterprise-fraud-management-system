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

The listener validates event type and version before creating workflow messages.

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

## Audit Logging

Audit Logging Service now consumes:

| Field | Value |
| --- | --- |
| Queue | `oefms.audit-logging.audit-event-recorded` |
| Routing key | `audit.event.recorded` |
| Payload | `EventEnvelope<AuditEventRecordedEvent>` |

The current audit sink records audit events to logs. Durable audit persistence should be added in a later branch.

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
          LoggingAuditEventSink
```

## Still To Add

- Persistent alert and case records.
- Persistent append-only audit records.
- Idempotency for consumed explanation and audit event ids.
- Notification consumer for `alert.created` and `case.created`.
- Case assignment, status history, investigator actions, and final fraud disposition.
