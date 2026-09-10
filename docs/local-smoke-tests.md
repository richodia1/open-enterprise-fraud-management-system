# Local Smoke Tests

Phase 1 local smoke tests provide a fast confidence check for contributors before they raise a pull request.

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

To start the core Phase 1 services and then probe them:

```bash
./scripts/smoke-local.sh --skip-build --start-stack
```

The core live checks cover:

| Component | Probe |
| --- | --- |
| Auth Service | `GET /actuator/health` |
| Ingestion Management Service | `GET /actuator/health` |
| Fraud Detection Service | `GET /actuator/health` |
| Explainable AI Service | `GET /actuator/health` |
| Case Management Service | `GET /actuator/health` |
| Fraud Detection Service | `POST /api/v1/fraud/decisions` |

The fraud decision probe posts a low-risk synthetic transaction and expects:

```text
mode=RULES
outcome=APPROVE
ruleHitCount=0
```

## Gateway Probe

Gateway health and the Gateway fraud decision route can be included with:

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
| `OEFMS_SMOKE_FRAUD_SERVICE_URL` | Fraud Detection Service base URL. | `http://localhost:8081` |
| `OEFMS_SMOKE_GATEWAY_URL` | API Gateway base URL. | `http://localhost:8080` |
| `OEFMS_SMOKE_GATEWAY_FRAUD_DECISION_PATH` | Gateway fraud decision path. | `/api/v1/fraud/decisions` |
| `OEFMS_SMOKE_HEALTH_TIMEOUT_SECONDS` | HTTP health wait timeout. | `180` |
| `OEFMS_SMOKE_HEALTH_INTERVAL_SECONDS` | HTTP health poll interval. | `3` |

## Cleanup

When `--start-stack` is used, the script leaves the stack running so failures can be inspected.

Stop it with:

```bash
./scripts/stop-local.sh
```
