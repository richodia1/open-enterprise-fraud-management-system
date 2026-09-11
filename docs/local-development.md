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

The live smoke path checks Auth, Ingestion Management, Fraud Detection, Rule Management, Explainable AI, Case Management, and Audit Logging health endpoints. It then posts a low-risk transaction to Ingestion Management, reads back the persisted receipt, posts the same transaction to the Fraud Detection Service decision endpoint, reads back the persisted fraud decision record, reads the ML runtime status, and reads the baseline rule catalog.

## Prepare ML Dataset Sample

Run:

```bash
./scripts/prepare-ml-dataset.sh oefms_synthetic_sample
```

The script writes ignored outputs under `data/interim/`, `data/processed/`, and `ml/artifacts/`. See [ML Dataset Foundation](ml-dataset-foundation.md).

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
| PostgreSQL | 5432 |
| RabbitMQ | 5672 |
| RabbitMQ Management UI | 15672 |
| Redis | 6379 |

## Local Infrastructure Credentials

| Component | URL | Username | Password |
| --- | --- | --- | --- |
| RabbitMQ Management UI | `http://localhost:15672` | `openefms` | `openefms` |
| API Gateway Routes | `http://localhost:8080/gateway/routes` | n/a | n/a |
| Auth Service Bootstrap User | `http://localhost:8085/auth/foundation` | `oefms-admin` | `openefms-admin` |

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

See [Database Providers](database-providers.md) for the provider strategy.

## Fraud Decision Mode

The Fraud Detection Service defaults to rule-based decisioning:

```bash
OEFMS_FRAUD_DECISION_MODE=rules
```

Configured values are `rules`, `ml`, and `hybrid`. `ml` and `hybrid` require a production scorer adapter before they can score live transactions. See [ML and AI Strategy](ml-ai-strategy.md).
