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

## Gateway Variables

Gateway variables belong to the API Gateway and define edge routing, correlation, and optional JWT verification.

The defaults below are for direct local JVM execution. Docker Compose and `.env.example` use service DNS names for the gateway container.

| Variable | Purpose | Local Default |
| --- | --- | --- |
| `OEFMS_GATEWAY_AUTH_SERVICE_URL` | Auth Service base URL. | `http://localhost:8085` |
| `OEFMS_GATEWAY_IDENTITY_SERVICE_URL` | Identity Service base URL. | `http://localhost:8087` |
| `OEFMS_GATEWAY_ADMINISTRATION_SERVICE_URL` | Administration Service base URL. | `http://localhost:8086` |
| `OEFMS_GATEWAY_CONFIGURATION_SERVICE_URL` | Configuration Management Service base URL. | `http://localhost:8088` |
| `OEFMS_GATEWAY_INGESTION_SERVICE_URL` | Ingestion Management Service base URL. | `http://localhost:8089` |
| `OEFMS_GATEWAY_FRAUD_SERVICE_URL` | Fraud Detection Service base URL. | `http://localhost:8081` |
| `OEFMS_GATEWAY_EXPLAINABLE_AI_SERVICE_URL` | Explainable AI Service base URL. | `http://localhost:8094` |
| `OEFMS_GATEWAY_RULE_SERVICE_URL` | Rule Management Service base URL. | `http://localhost:8082` |
| `OEFMS_GATEWAY_LIST_SERVICE_URL` | List Management Service base URL. | `http://localhost:8083` |
| `OEFMS_GATEWAY_CASE_SERVICE_URL` | Case Management Service base URL. | `http://localhost:8084` |
| `OEFMS_GATEWAY_AUDIT_SERVICE_URL` | Audit Logging Service base URL. | `http://localhost:8090` |
| `OEFMS_GATEWAY_NOTIFICATION_SERVICE_URL` | Notification Management Service base URL. | `http://localhost:8091` |
| `OEFMS_GATEWAY_REPORT_SERVICE_URL` | Report Management Service base URL. | `http://localhost:8092` |
| `OEFMS_GATEWAY_GDPR_SERVICE_URL` | GDPR Service base URL. | `http://localhost:8093` |
| `OEFMS_GATEWAY_SECURITY_ENABLED` | Enables JWT verification for `/api/**` routes. | `false` |
| `OEFMS_GATEWAY_AUTH_JWK_SET_URI` | JWK Set URI used when gateway security is enabled. | `http://localhost:8085/oauth2/jwks` |

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
| `OEFMS_RABBITMQ_HOST` | RabbitMQ host used by event-facing services. | `localhost` |
| `OEFMS_RABBITMQ_PORT` | RabbitMQ AMQP port used by event-facing services. | `5672` |
| `OEFMS_RABBITMQ_EXCHANGE` | Domain event exchange. | `oefms.events` |
| `OEFMS_RABBITMQ_DEAD_LETTER_EXCHANGE` | Domain event dead-letter exchange. | `oefms.events.dlx` |

The RabbitMQ Management UI runs on:

```text
http://localhost:15672
```

## Fraud Decision Variables

Fraud decision variables belong to the Fraud Detection Service.

| Variable | Purpose | Local Default |
| --- | --- | --- |
| `OEFMS_FRAUD_DECISION_MODE` | Selects rules, ML, or hybrid decisioning. | `rules` |
| `OEFMS_FRAUD_RULE_HIGH_VALUE_AMOUNT_THRESHOLD` | Rule-mode high-value amount threshold. | `10000.00` |
| `OEFMS_FRAUD_RULE_CRITICAL_VALUE_AMOUNT_THRESHOLD` | Rule-mode critical-value amount threshold. | `100000.00` |
| `OEFMS_FRAUD_RULE_VELOCITY_REVIEW_COUNT` | Rule-mode recent transaction count that contributes to review. | `5` |
| `OEFMS_FRAUD_RULE_VELOCITY_BLOCK_COUNT` | Rule-mode recent transaction count that contributes to block/escalation. | `10` |
| `OEFMS_FRAUD_RULE_REVIEW_SCORE_THRESHOLD` | Score at which rule mode returns `REVIEW`. | `0.35` |
| `OEFMS_FRAUD_RULE_BLOCK_SCORE_THRESHOLD` | Score at which rule mode returns `BLOCK`. | `0.70` |
| `OEFMS_FRAUD_RULE_ESCALATE_SCORE_THRESHOLD` | Score at which rule mode returns `ESCALATE`. | `0.90` |
| `OEFMS_FRAUD_ML_PROVIDER` | Future model provider boundary. | `disabled` |
| `OEFMS_FRAUD_ML_MODEL_NAME` | Future deployed model name. | `xgboost-baseline` |
| `OEFMS_FRAUD_ML_SCORE_THRESHOLD` | Future model score threshold. | `0.80` |

`rules` remains the default runnable mode. `ml` and `hybrid` are wired through the ML scoring contract, but the default runtime reports disabled or contract-only until a validated scorer adapter is registered.

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
