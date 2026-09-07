# Platform Configuration

This document tracks the shared local runtime configuration used by the Spring Boot services and Docker Compose stack.

## Local Defaults

The example environment file is:

```text
.env.example
```

Create a local `.env` from it when you need custom values. Do not commit local secrets or real institution credentials.

## Database Variables

PostgreSQL is the default local database provider for v0.1.

| Variable | Purpose | Local Default |
| --- | --- | --- |
| `OEFMS_DATASOURCE_USERNAME` | Local database user used by database-backed services. | `openefms` |
| `OEFMS_DATASOURCE_PASSWORD` | Local database password used by database-backed services. | `openefms` |

Database-backed services set their own `OEFMS_DATASOURCE_URL` in `docker-compose.yml` so each service can point to its own database.

## RabbitMQ Variables

RabbitMQ is the default local broker for asynchronous events.

| Variable | Purpose | Local Default |
| --- | --- | --- |
| `OEFMS_RABBITMQ_USERNAME` | Local RabbitMQ username. | `openefms` |
| `OEFMS_RABBITMQ_PASSWORD` | Local RabbitMQ password. | `openefms` |
| `OEFMS_RABBITMQ_VHOST` | Local RabbitMQ virtual host. | `openefms` |

The RabbitMQ Management UI runs on:

```text
http://localhost:15672
```

## Fraud Decision Variables

Fraud decision variables belong to the Fraud Detection Service.

| Variable | Purpose | Local Default |
| --- | --- | --- |
| `OEFMS_FRAUD_DECISION_MODE` | Selects rule, ML, or hybrid decisioning. | `rules` |
| `OEFMS_FRAUD_ML_PROVIDER` | Future model provider boundary. | `disabled` |
| `OEFMS_FRAUD_ML_MODEL_NAME` | Future deployed model name. | `xgboost-baseline` |
| `OEFMS_FRAUD_ML_SCORE_THRESHOLD` | Future model score threshold. | `0.80` |

Only `rules` mode should be treated as active until ML training, validation, explainability, and model-serving boundaries are implemented.

## Compose Health Checks

The local stack uses infrastructure health checks for:

- PostgreSQL
- RabbitMQ
- Redis

Services that depend on these infrastructure components wait for healthy containers where Docker Compose supports health-based dependencies.

## Service Configuration Rule

Keep configuration close to the service that owns it:

- Auth-specific config belongs to Auth Service.
- Fraud decision config belongs to Fraud Detection Service.
- Explanation config belongs to Explainable AI Service.
- Broker exchange, queue, and routing-key config should be added with the RabbitMQ event spine.
- Database provider settings should remain environment-driven.
