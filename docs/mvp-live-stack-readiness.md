# MVP Live Stack Readiness

This branch prepares the first full local MVP run.

The normal live smoke path checks service health and individual APIs. The MVP event-flow probe goes further: it submits a reviewable transaction through Ingestion Management and waits for downstream records created by the RabbitMQ workflow.

## Recommended First Run

Reset local volumes once if the PostgreSQL volume was created before `openefms_audit` was added:

```bash
docker compose down -v
```

Start the stack and run the Gateway-backed MVP probe:

```bash
./scripts/smoke-local.sh --skip-build --start-stack --gateway --event-flow
```

The script first checks that Docker can start and finish a tiny `hello-world` container. If that preflight fails, restart Docker Desktop and verify `docker run --rm hello-world` before rerunning the MVP probe.

The script leaves the stack running so failures can be inspected.

## What The Event Flow Proves

The `--event-flow` probe checks that one reviewable transaction moves through the foundation workflow:

```text
API Gateway
        |
        v
Ingestion Management Service
        |
        v
RabbitMQ transaction.received
        |
        v
Fraud Detection Service
        |
        v
RabbitMQ fraud.decision.produced
        |
        v
Explainable AI Service
        |
        v
RabbitMQ explanation.prepared
        |
        v
Case Management Service
        |
        +--> fraud_alerts
        +--> fraud_cases
        +--> RabbitMQ audit.event.recorded
                 |
                 v
          Audit Logging Service
                 |
                 v
          audit_events
```

## Expected Observations

The probe waits for:

- A persisted fraud decision for the submitted transaction.
- A non-approval outcome: `REVIEW`, `BLOCK`, or `ESCALATE`.
- A persisted case record for the submitted transaction.
- A persisted alert record for the submitted transaction.
- Audit records for alert and case creation.
- An audit record that retains the submitted transaction id.

## Useful Follow-Up Commands

Read the Gateway route map:

```bash
curl -fsS http://localhost:8080/gateway/routes
```

Read records for the generated event-flow tenant:

```bash
curl -fsS 'http://localhost:8080/api/v1/cases?tenantId=mvp-smoke-tenant'
curl -fsS 'http://localhost:8080/api/v1/alerts?tenantId=mvp-smoke-tenant'
curl -fsS 'http://localhost:8080/api/v1/audit-events?tenantId=mvp-smoke-tenant'
```

Stop the stack when finished:

```bash
./scripts/stop-local.sh
```
