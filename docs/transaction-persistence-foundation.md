# Transaction Persistence Foundation

This branch adds the first durable domain record in OEFMS v0.1.

The Ingestion Management Service now owns a received-transaction receipt store. The goal is to prove the service-owned schema pattern before adding persistence to fraud decisions, explanations, cases, rules, and audit events.

## Scope

- Create the local `openefms_ingestion` database.
- Add the `received_transactions` table through Flyway.
- Persist a normalized receipt before RabbitMQ publication.
- Mark the receipt as `PUBLISHED` after the transaction-received event is published.
- Expose read endpoints for a single transaction receipt and tenant-scoped receipt lists.
- Keep fraud scoring, investigation workflow, and audit persistence outside this service.

## Runtime Flow

```text
POST /api/v1/ingestion/transactions
        |
        v
Validate and normalize request
        |
        v
Save received_transactions row as RECEIVED
        |
        v
Publish transaction.received to RabbitMQ
        |
        v
Update received_transactions row to PUBLISHED
```

If RabbitMQ publication fails, the API returns `503 Service Unavailable` and the receipt remains at `RECEIVED`. A later outbox or retry branch should close that reliability gap.

## Database Ownership

| Item | Value |
| --- | --- |
| Service | Ingestion Management Service |
| Database | `openefms_ingestion` |
| Table | `received_transactions` |
| Migration | `services/ingestion-management-service/src/main/resources/db/migration/postgresql/V1__create_received_transactions.sql` |

The table stores normalized transaction identity, amount, channel, direction, party reference fields, correlation metadata, event metadata, status, timestamps, and optional request attributes.

## API Reads

```text
GET /api/v1/ingestion/transactions/{transactionId}
GET /api/v1/ingestion/transactions?tenantId={tenantId}
```

The API returns the shared receipt representation used by the service layer. These endpoints are foundation reads for smoke testing, traceability, and future investigator workflows.

## Next Persistence Candidates

- Explainable AI explanation records.
- Audit Logging append-only event records.
- Rule Management rule definitions and version history.
