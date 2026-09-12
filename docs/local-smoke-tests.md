# Local Smoke Tests

Local smoke tests provide a fast confidence check for contributors before they raise a pull request.

## Static Smoke Checks

The default script checks:

- Docker Compose configuration using `.env.example`.
- The sample ML dataset preparation path.
- XGBoost feature-matrix validation without importing optional ML dependencies.
- Model-comparison feature-matrix validation without importing optional ML dependencies.
- Model-audit feature-matrix validation without importing optional ML dependencies.
- A tiny BAF-shaped mapping probe that confirms real BAF numeric and categorical features are preserved.
- The full Maven build unless `--skip-build` is passed.

```bash
./scripts/smoke-local.sh
```

CI already runs `mvn clean verify`, so CI uses:

```bash
./scripts/smoke-local.sh --skip-build
```

## Live Smoke Checks

To probe a stack that is already running:

```bash
./scripts/smoke-local.sh --skip-build --live
```

To start the core services and then probe them:

```bash
./scripts/smoke-local.sh --skip-build --start-stack
```

Before creating the Compose stack, `--start-stack` runs a tiny Docker runtime preflight with `hello-world`. This catches local Docker Desktop issues where the daemon responds to `docker info` but containers remain stuck in `Created`.

RabbitMQ gets an extended startup grace period in Compose because a clean local volume can spend close to a minute initializing metadata, feature flags, and the default OEFMS virtual host before it can answer its health probe. The probe runs as the `rabbitmq` user so Docker health checks do not create an unreadable Erlang cookie on new volumes.

When `--gateway` is also supplied, the script starts and probes `api-gateway` as well.

For the full MVP readiness path, add `--event-flow`:

```bash
./scripts/smoke-local.sh --skip-build --start-stack --gateway --event-flow
```

This submits a reviewable transaction and waits for records produced by the RabbitMQ event chain.

The core live checks cover:

| Component | Probe |
| --- | --- |
| Auth Service | `GET /actuator/health` |
| Ingestion Management Service | `GET /actuator/health` |
| Ingestion Management Service | `POST /api/v1/ingestion/transactions` |
| Ingestion Management Service | `GET /api/v1/ingestion/transactions/{transactionId}` |
| Fraud Detection Service | `GET /actuator/health` |
| Fraud Detection Service | `GET /api/v1/fraud/ml/runtime` |
| Fraud Detection Service | `GET /api/v1/fraud/decisions/{decisionId}` |
| Rule Management Service | `GET /actuator/health` |
| Rule Management Service | `GET /api/v1/rules` |
| Explainable AI Service | `GET /actuator/health` |
| Case Management Service | `GET /actuator/health` |
| Case Management Service | `GET /api/v1/cases?tenantId=smoke-tenant` |
| Case Management Service | `GET /api/v1/alerts?tenantId=smoke-tenant` |
| Audit Logging Service | `GET /actuator/health` |
| Audit Logging Service | `GET /api/v1/audit-events?tenantId=smoke-tenant` |
| Fraud Detection Service | `POST /api/v1/fraud/decisions` |

The ingestion probe posts a low-risk synthetic transaction and expects:

```text
status=RECEIVED
eventType=transaction.received
```

Because ingestion now publishes to RabbitMQ, this probe requires the broker to be reachable.

The persisted receipt probe expects:

```text
status=PUBLISHED
eventType=transaction.received
tenantId=smoke-tenant
```

The fraud decision probe posts a low-risk synthetic transaction and expects:

```text
mode=RULES
outcome=APPROVE
ruleHitCount=0
```

It then reads back the persisted fraud decision record and expects:

```text
outcome=APPROVE
riskBand=LOW
transactionId=<smoke id>
```

The ML runtime probe reads the configured scorer status and expects:

```text
modelName=xgboost-baseline
state=<reported>
supportedModes=<reported>
```

The rule catalog probe reads active global rules and expects:

```text
HIGH_VALUE_AMOUNT
CRITICAL_VALUE_AMOUNT
```

The case, alert, and audit event list probes expect JSON lists. They may be empty on a fresh local database.

## Event Flow Probe

The event-flow probe uses a generated transaction id unless `OEFMS_SMOKE_REVIEW_TRANSACTION_ID` is supplied.

It submits a reviewable transaction through Ingestion Management and waits for:

- A persisted Fraud Detection decision for the transaction.
- A Case Management case record for the transaction.
- A Case Management alert record for the transaction.
- Audit Logging records for `ALERT_CREATED` and `CASE_CREATED`.
- An audit record containing the submitted transaction id.

The default review transaction uses a high amount, cross-border beneficiary country, and velocity count so the rule-based engine should require investigation.

## Gateway Probe

Gateway health, transaction ingestion, Fraud Detection, ML runtime, Rule Management, Case Management, and Audit Logging routes can be included with:

```bash
./scripts/smoke-local.sh --skip-build --live --gateway
```

The default Gateway fraud decision path is `/api/v1/fraud/decisions`. Override it when testing a different public route:

```bash
OEFMS_SMOKE_GATEWAY_FRAUD_DECISION_PATH=/api/v1/fraud/decisions \
  ./scripts/smoke-local.sh --skip-build --live --gateway
```

## Useful Environment Overrides

| Variable | Purpose | Default |
| --- | --- | --- |
| `MAVEN_CMD` | Maven executable used by the script. | `mvn` |
| `PYTHON_CMD` | Python executable used by the dataset preparation probe. | `python3` |
| `OEFMS_SMOKE_ML_DATASET_CHECKS` | Runs the sample ML dataset, XGBoost validation, model-comparison validation, and BAF mapping probes. | `true` |
| `OEFMS_SMOKE_DOCKER_RUNTIME_PREFLIGHT` | Runs a tiny Docker container before `--start-stack` creates the Compose stack. | `true` |
| `OEFMS_SMOKE_DOCKER_RUN_TIMEOUT_SECONDS` | Docker runtime preflight timeout. | `30` |
| `OEFMS_SMOKE_ENV_FILE` | Compose env file. | `.env.example` |
| `OEFMS_SMOKE_INGESTION_SERVICE_URL` | Ingestion Management Service base URL. | `http://localhost:8089` |
| `OEFMS_SMOKE_FRAUD_SERVICE_URL` | Fraud Detection Service base URL. | `http://localhost:8081` |
| `OEFMS_SMOKE_RULE_SERVICE_URL` | Rule Management Service base URL. | `http://localhost:8082` |
| `OEFMS_SMOKE_AUDIT_SERVICE_URL` | Audit Logging Service base URL. | `http://localhost:8090` |
| `OEFMS_SMOKE_CASE_SERVICE_URL` | Case Management Service base URL. | `http://localhost:8084` |
| `OEFMS_SMOKE_GATEWAY_URL` | API Gateway base URL. | `http://localhost:8080` |
| `OEFMS_SMOKE_TRANSACTION_ID` | Transaction id used by live probes. | `118f3bd4-b208-44ff-b1ff-447c78d80d02` |
| `OEFMS_SMOKE_GATEWAY_INGESTION_PATH` | Gateway transaction ingestion path. | `/api/v1/ingestion/transactions` |
| `OEFMS_SMOKE_GATEWAY_INGESTION_RECEIPT_PATH` | Gateway transaction receipt path. | `/api/v1/ingestion/transactions/<smoke id>` |
| `OEFMS_SMOKE_GATEWAY_FRAUD_DECISION_PATH` | Gateway fraud decision path. | `/api/v1/fraud/decisions` |
| `OEFMS_SMOKE_GATEWAY_ML_RUNTIME_PATH` | Gateway ML runtime path. | `/api/v1/fraud/ml/runtime` |
| `OEFMS_SMOKE_GATEWAY_RULE_PATH` | Gateway rule catalog path. | `/api/v1/rules` |
| `OEFMS_SMOKE_GATEWAY_CASE_PATH` | Gateway case list path. | `/api/v1/cases` |
| `OEFMS_SMOKE_GATEWAY_ALERT_PATH` | Gateway alert list path. | `/api/v1/alerts` |
| `OEFMS_SMOKE_GATEWAY_AUDIT_PATH` | Gateway audit event list path. | `/api/v1/audit-events` |
| `OEFMS_SMOKE_REVIEW_TRANSACTION_ID` | Optional fixed transaction id for the event-flow probe. | generated UUID |
| `OEFMS_SMOKE_HEALTH_TIMEOUT_SECONDS` | HTTP health wait timeout. | `180` |
| `OEFMS_SMOKE_HEALTH_INTERVAL_SECONDS` | HTTP health poll interval. | `3` |

## Cleanup

When `--start-stack` is used, the script leaves the stack running so failures can be inspected.

If the script reports that Docker is reachable but a test container cannot complete, restart Docker Desktop and verify the runtime directly:

```bash
docker run --rm hello-world
```

Stop it with:

```bash
./scripts/stop-local.sh
```
