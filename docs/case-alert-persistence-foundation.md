# Case Alert Persistence Foundation

This branch adds the first durable Case Management Service records.

Case Management now owns `fraud_alerts` and `fraud_cases` tables in `openefms_cases`. Reviewable explanations still produce `alert.created`, `case.created`, and audit events, but the alert and case are also stored before messages are handed to RabbitMQ publication.

## Scope

- Add PostgreSQL Flyway migrations for `fraud_alerts`, `fraud_cases`, and lifecycle action history.
- Persist the first alert and case records for reviewable explanations.
- Store event metadata, correlation metadata, title, summary, priority, risk posture, status, and workflow attributes.
- Expose read endpoints for alert/case id and tenant-scoped lists.
- Keep advanced comments, SLA tracking, outbox retries, and the full investigation workflow for later branches.

## Runtime Flow

```text
ExplanationPreparedEvent
        |
        v
CaseWorkflowOrchestrator
        |
        +--> build AlertCreationRequest and CaseCreationRequest
        |
        +--> build alert.created and case.created envelopes
        |
        +--> save fraud_alerts and fraud_cases rows
        |
        v
Return messages for RabbitMQ publication
```

If persistence fails, the listener does not publish the alert/case workflow messages. A later outbox branch should make the database write and event publication retryable as one business workflow.

## Database Ownership

| Item | Value |
| --- | --- |
| Service | Case Management Service |
| Database | `openefms_cases` |
| Tables | `fraud_alerts`, `fraud_cases`, `fraud_case_actions` |
| Migrations | `services/case-management-service/src/main/resources/db/migration/postgresql/V1__create_case_workflow_records.sql`, `services/case-management-service/src/main/resources/db/migration/postgresql/V2__add_case_lifecycle_actions.sql` |

The case table references the alert table through `alert_id`. Both tables include tenant, transaction, decision, event, correlation, and trace fields for future investigator and audit screens.

## API Reads

```text
GET /api/v1/alerts/{alertId}
GET /api/v1/alerts?tenantId={tenantId}
GET /api/v1/cases/{caseId}
GET /api/v1/cases?tenantId={tenantId}
GET /api/v1/cases/{caseId}/actions
PATCH /api/v1/cases/{caseId}/assignment
PATCH /api/v1/cases/{caseId}/status
```

These endpoints provide the first case queue backend for the future analyst console. They return the service-owned `AlertRecord` and `CaseRecord` shapes.

## Current Status Model

Alert records currently support `OPEN`, `ACKNOWLEDGED`, and `CLOSED`.

Case records currently support `OPEN`, `IN_REVIEW`, `ESCALATED`, `FRAUD_CONFIRMED`, `FALSE_POSITIVE`, and `CLOSED`.

See [Case Lifecycle Actions](case-lifecycle-actions.md) for the first assignment, status, final disposition, and append-only history contracts.
