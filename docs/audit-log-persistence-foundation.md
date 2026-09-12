# Audit Log Persistence Foundation

This branch adds the first durable Audit Logging Service records.

Audit Logging Service now owns an `audit_events` table in `openefms_audit`. It consumes `audit.event.recorded` messages from RabbitMQ, stores the shared audit record, and retains the event envelope metadata needed to trace case and alert activity through the Phase 2 workflow.

## Scope

- Add PostgreSQL Flyway migration for `audit_events`.
- Persist consumed `AuditEventRecordedEvent` messages.
- Store audit action details, actor, resource identity, summary, event metadata, source service, correlation id, causation id, trace id, and attributes.
- Expose read endpoints by audit event id, tenant id, correlation id, and resource identity.
- Keep audit records append-only from the public API; status changes, idempotency storage, retention policy, and tamper-evidence are later branches.

## Runtime Flow

```text
RabbitMQ audit.event.recorded event
        |
        v
AuditEventRecordedEventListener
        |
        | validates event type and version
        v
PersistingAuditEventSink
        |
        v
Save audit_events row
```

The stored record keeps two timestamps:

| Field | Meaning |
| --- | --- |
| `occurredAt` | When the audited business action happened. |
| `eventOccurredAt` | When the `audit.event.recorded` envelope was produced. |

## Database Ownership

| Item | Value |
| --- | --- |
| Service | Audit Logging Service |
| Database | `openefms_audit` |
| Table | `audit_events` |
| Migration | `services/audit-logging-service/src/main/resources/db/migration/postgresql/V1__create_audit_events.sql` |

The table is indexed by tenant and timestamp, tenant/resource identity, correlation id, source event id, and trace id.

## API Reads

```text
GET /api/v1/audit-events/{auditEventId}
GET /api/v1/audit-events?tenantId={tenantId}
GET /api/v1/audit-events?correlationId={correlationId}
GET /api/v1/audit-events?tenantId={tenantId}&resourceType={resourceType}&resourceId={resourceId}
```

These endpoints provide the first backend audit trail for future investigator timelines and operational evidence screens.

## Later Hardening

- Idempotency for consumed audit event ids.
- Append-only status/history rules enforced beyond the initial read-only API.
- Retention and privacy policy integration.
- Tamper-evident hashes or chained signatures.
- Broader audit emitters for ingestion, fraud decisions, explanations, authentication, and investigator actions.
