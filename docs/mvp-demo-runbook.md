# MVP Demo Runbook

This runbook is for a local walkthrough of the first backend MVP flow after the stack is healthy.

## What The Demo Shows

The demo submits one reviewable transaction through the API Gateway and follows the event-driven path:

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
Explainable AI Service
        |
        v
Case Management Service
        |
        +--> alert record
        +--> case record
        +--> audit records
```

The generated transaction is intentionally high value, cross-border, and velocity-marked so the rule engine should require investigation.

## Recommended Demo Command

From the repository root:

```bash
./scripts/run-mvp-demo-flow.sh --start-stack
```

`--start-stack` starts only the infrastructure and domain services needed for the MVP event flow. It starts the Spring services one at a time, then starts the API Gateway for the public routes. Use the full smoke command when you want every service and every route checked.

Print the captured JSON responses as well:

```bash
./scripts/run-mvp-demo-flow.sh --start-stack --show-json
```

If default infrastructure ports are already used locally, override the host ports before starting:

```bash
OEFMS_POSTGRES_HOST_PORT=15433 OEFMS_REDIS_HOST_PORT=16379 \
  ./scripts/run-mvp-demo-flow.sh --start-stack
```

The demo script allows up to 360 seconds for first startup because a clean Docker volume can be slow while several Spring services and infrastructure containers initialize together. Override it when needed:

```bash
OEFMS_DEMO_STARTUP_TIMEOUT_SECONDS=480 ./scripts/run-mvp-demo-flow.sh --start-stack
```

## Expected Output

The script prints:

- The generated transaction ID.
- The correlation ID.
- The fraud decision ID, outcome, risk band, risk score, and reason.
- The generated case ID and case priority.
- The generated alert ID and alert priority.
- Confirmation that `ALERT_CREATED` and `CASE_CREATED` audit records were observed.

Captured request and response JSON files are written under:

```text
target/mvp-demo-flow/<transaction-id>/
```

`target/` is ignored by Git, so these local run artifacts are not committed.

## Useful Follow-Up URLs

Replace `<transaction-id>` and `<tenant-id>` with the values printed by the script:

```bash
curl -fsS 'http://localhost:8080/api/v1/fraud/decisions?transactionId=<transaction-id>'
curl -fsS 'http://localhost:8080/api/v1/cases?tenantId=<tenant-id>'
curl -fsS 'http://localhost:8080/api/v1/alerts?tenantId=<tenant-id>'
curl -fsS 'http://localhost:8080/api/v1/audit-events?tenantId=<tenant-id>'
```

## Stop The Stack

```bash
./scripts/stop-local.sh
```

Or ask the demo script to stop it after the run:

```bash
./scripts/run-mvp-demo-flow.sh --start-stack --stop-stack
```
