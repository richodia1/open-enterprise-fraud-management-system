# Local Smoke Tests

Local smoke tests provide a fast confidence check for contributors before they raise a pull request.

## Static Smoke Checks

The default script checks:

- Docker Compose configuration using `.env.example`.
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

When `--gateway` is also supplied, the script starts and probes `api-gateway` as well.

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
| Audit Logging Service | `GET /actuator/health` |
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

## Gateway Probe

Gateway health, transaction ingestion, Fraud Detection, ML runtime, and Rule Management routes can be included with:

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
| `OEFMS_SMOKE_ENV_FILE` | Compose env file. | `.env.example` |
| `OEFMS_SMOKE_INGESTION_SERVICE_URL` | Ingestion Management Service base URL. | `http://localhost:8089` |
| `OEFMS_SMOKE_FRAUD_SERVICE_URL` | Fraud Detection Service base URL. | `http://localhost:8081` |
| `OEFMS_SMOKE_RULE_SERVICE_URL` | Rule Management Service base URL. | `http://localhost:8082` |
| `OEFMS_SMOKE_AUDIT_SERVICE_URL` | Audit Logging Service base URL. | `http://localhost:8090` |
| `OEFMS_SMOKE_GATEWAY_URL` | API Gateway base URL. | `http://localhost:8080` |
| `OEFMS_SMOKE_TRANSACTION_ID` | Transaction id used by live probes. | `118f3bd4-b208-44ff-b1ff-447c78d80d02` |
| `OEFMS_SMOKE_GATEWAY_INGESTION_PATH` | Gateway transaction ingestion path. | `/api/v1/ingestion/transactions` |
| `OEFMS_SMOKE_GATEWAY_INGESTION_RECEIPT_PATH` | Gateway transaction receipt path. | `/api/v1/ingestion/transactions/<smoke id>` |
| `OEFMS_SMOKE_GATEWAY_FRAUD_DECISION_PATH` | Gateway fraud decision path. | `/api/v1/fraud/decisions` |
| `OEFMS_SMOKE_GATEWAY_ML_RUNTIME_PATH` | Gateway ML runtime path. | `/api/v1/fraud/ml/runtime` |
| `OEFMS_SMOKE_GATEWAY_RULE_PATH` | Gateway rule catalog path. | `/api/v1/rules` |
| `OEFMS_SMOKE_HEALTH_TIMEOUT_SECONDS` | HTTP health wait timeout. | `180` |
| `OEFMS_SMOKE_HEALTH_INTERVAL_SECONDS` | HTTP health poll interval. | `3` |

## Cleanup

When `--start-stack` is used, the script leaves the stack running so failures can be inspected.

Stop it with:

```bash
./scripts/stop-local.sh
```
