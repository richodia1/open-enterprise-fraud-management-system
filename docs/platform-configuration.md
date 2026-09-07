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

## Auth Variables

Auth variables belong to the Auth Service and are currently used for local bootstrap configuration only.

| Variable | Purpose | Local Default |
| --- | --- | --- |
| `OEFMS_AUTH_ISSUER` | Authorization server issuer URL. | `http://localhost:8085` |
| `OEFMS_AUTH_BOOTSTRAP_USERNAME` | Local bootstrap username. | `oefms-admin` |
| `OEFMS_AUTH_BOOTSTRAP_PASSWORD` | Local bootstrap password. | `openefms-admin` |
| `OEFMS_AUTH_BOOTSTRAP_ROLES` | Comma-separated local bootstrap roles. | `PLATFORM_ADMIN,FRAUD_ANALYST` |
| `OEFMS_AUTH_BOOTSTRAP_PERMISSIONS` | Comma-separated local bootstrap permissions. | `oefms.read,oefms.write,fraud.decision.read,fraud.case.write` |
| `OEFMS_AUTH_CLIENT_ID` | Local bootstrap OAuth2 client id. | `oefms-platform` |
| `OEFMS_AUTH_CLIENT_SECRET` | Local bootstrap OAuth2 client secret. | `openefms-platform-secret` |
| `OEFMS_AUTH_CLIENT_NAME` | Local bootstrap OAuth2 client name. | `OEFMS Platform Client` |
| `OEFMS_AUTH_CLIENT_REDIRECT_URIS` | Comma-separated authorization-code redirect URIs. | `http://127.0.0.1:8080/login/oauth2/code/oefms-platform` |
| `OEFMS_AUTH_CLIENT_POST_LOGOUT_REDIRECT_URIS` | Comma-separated post-logout redirect URIs. | `http://127.0.0.1:8080/` |
| `OEFMS_AUTH_CLIENT_SCOPES` | Comma-separated OAuth2/OIDC scopes. | `openid,profile,oefms.read,oefms.write,fraud.decision.read,fraud.case.write` |
| `OEFMS_AUTH_CLIENT_ACCESS_TOKEN_TTL` | Local access token lifetime. | `PT15M` |
| `OEFMS_AUTH_CLIENT_REFRESH_TOKEN_TTL` | Local refresh token lifetime. | `PT8H` |
| `OEFMS_AUTH_CLIENT_REQUIRE_PROOF_KEY` | Requires PKCE for supported flows. | `true` |
| `OEFMS_AUTH_CLIENT_REQUIRE_AUTHORIZATION_CONSENT` | Requires user consent for authorization-code flows. | `true` |

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
