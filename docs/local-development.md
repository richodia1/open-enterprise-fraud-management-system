# Local Development

## Requirements

- JDK 21
- Maven 3.9+
- Docker Desktop or Docker Engine

## Build All Modules

```bash
mvn clean verify
```

## Check Local Configuration

```bash
./scripts/check-local-config.sh
```

## Run Local Smoke Checks

Run the CI-friendly smoke checks:

```bash
./scripts/smoke-local.sh --skip-build
```

The smoke script also runs the sample ML dataset preparation probe by default. Disable it only when diagnosing a local Python installation:

```bash
OEFMS_SMOKE_ML_DATASET_CHECKS=false ./scripts/smoke-local.sh --skip-build
```

Run Maven verification and the static smoke checks together:

```bash
./scripts/smoke-local.sh
```

Probe a local stack that is already running:

```bash
./scripts/smoke-local.sh --skip-build --live
```

Start the core services and then probe them:

```bash
./scripts/smoke-local.sh --skip-build --start-stack
```

Run the full MVP event-flow probe through the API Gateway:

```bash
./scripts/smoke-local.sh --skip-build --start-stack --gateway --event-flow
```

Run a readable demo flow that prints the generated decision, case, alert, and audit details:

```bash
./scripts/run-mvp-demo-flow.sh --start-stack
```

The demo command starts only the infrastructure and domain services needed for the MVP event flow. It starts the Spring services one at a time, then starts the API Gateway route surface.

List the live Swagger/OpenAPI contract URLs:

```bash
./scripts/list-swagger-contracts.sh
```

After starting the local stack, verify which contract endpoints are reachable:

```bash
./scripts/list-swagger-contracts.sh --check
```

See [Swagger OpenAPI Contracts](swagger-openapi-contracts.md) for the full service URL table.

The `--start-stack` path first checks Docker container startup with a tiny preflight container. If Docker can answer `docker info` but cannot start containers, restart Docker Desktop and verify `docker run --rm hello-world`.

The live smoke path checks Auth, Ingestion Management, Fraud Detection, Rule Management, Explainable AI, Case Management, and Audit Logging health endpoints. It then reads the Case Management case and alert lists, reads the Audit Logging event list, posts a low-risk transaction to Ingestion Management, reads back the persisted receipt, posts the same transaction to the Fraud Detection Service decision endpoint, reads back the persisted fraud decision record, reads the ML runtime status, and reads the baseline rule catalog. With `--event-flow`, the script also posts a reviewable transaction and waits for event-driven fraud decision, case, alert, and audit records.

## Prepare ML Dataset Sample

Run:

```bash
./scripts/prepare-ml-dataset.sh oefms_synthetic_sample
```

The script writes ignored outputs under `data/interim/`, `data/processed/`, and `ml/artifacts/`. See [ML Dataset Foundation](ml-dataset-foundation.md).

For the Bank Account Fraud baseline, place `Base.csv` under `data/raw/bank-account-fraud/`, then run:

```bash
./scripts/prepare-ml-dataset.sh bank_account_fraud_neurips_2022
python3 ml/experiments/xgboost-baseline/train_xgboost_baseline.py --validate-only
```

Install `ml/requirements.txt` only when you want to train the local XGBoost model.

## Run Local Stack

```bash
docker compose up --build
```

The stack uses health checks for PostgreSQL, RabbitMQ, and Redis. See [Platform Configuration](platform-configuration.md) for local environment variables and service ownership rules.

If you previously started local infrastructure before database, RabbitMQ, or Redis settings changed, reset the local volumes once:

```bash
docker compose down -v
docker compose up --build
```

The audit database was added after the first persistence branches. Reset local volumes once before the first full MVP run if the local PostgreSQL volume already existed.

## Default Ports

| Component | Port |
| --- | --- |
| API Gateway | 8080 |
| Fraud Detection Service | 8081 |
| Rule Management Service | 8082 |
| List Management Service | 8083 |
| Case Management Service | 8084 |
| Auth Service | 8085 |
| Administration Service | 8086 |
| Identity Service | 8087 |
| Configuration Management Service | 8088 |
| Ingestion Management Service | 8089 |
| Audit Logging Service | 8090 |
| Notification Management Service | 8091 |
| Report Management Service | 8092 |
| GDPR Service | 8093 |
| Explainable AI Service | 8094 |
| PostgreSQL | `${OEFMS_POSTGRES_HOST_PORT:-5432}` |
| RabbitMQ | `${OEFMS_RABBITMQ_HOST_PORT:-5672}` |
| RabbitMQ Management UI | `${OEFMS_RABBITMQ_MANAGEMENT_HOST_PORT:-15672}` |
| Redis | `${OEFMS_REDIS_HOST_PORT:-6379}` |

## Swagger And OpenAPI

Every runnable Spring Boot module exposes:

```text
/swagger-ui.html
/v3/api-docs
```

The API Gateway Swagger UI is available at `http://localhost:8080/swagger-ui.html`. Each service also exposes its own Swagger UI on the service port listed above.

## Local Infrastructure Credentials

| Component | URL | Username | Password |
| --- | --- | --- | --- |
| RabbitMQ Management UI | `http://localhost:15672` | `openefms` | `openefms` |
| API Gateway Routes | `http://localhost:8080/gateway/routes` | n/a | n/a |
| Auth Service Bootstrap User | `http://localhost:8085/auth/foundation` | `oefms-admin` | `openefms-admin` |

If a local database or broker is already using a default port, override only the host port before starting Docker Compose, for example:

```bash
OEFMS_POSTGRES_HOST_PORT=15433 OEFMS_REDIS_HOST_PORT=16379 docker compose --env-file .env.example up -d
```

## Local Databases

| Service | Database |
| --- | --- |
| Auth Service | `openefms_auth` |
| Ingestion Management Service | `openefms_ingestion` |
| Fraud Detection Service | `openefms_fraud` |
| Explainable AI Service | `openefms_explainable_ai` |
| Rule Management Service | `openefms_rules` |
| List Management Service | `openefms_lists` |
| Case Management Service | `openefms_cases` |
| Audit Logging Service | `openefms_audit` |

See [Database Providers](database-providers.md) for the provider strategy.

## Fraud Decision Mode

The Fraud Detection Service defaults to rule-based decisioning:

```bash
OEFMS_FRAUD_DECISION_MODE=rules
```

Configured values are `rules`, `ml`, and `hybrid`. `ml` and `hybrid` require a production scorer adapter before they can score live transactions. See [ML and AI Strategy](ml-ai-strategy.md).
