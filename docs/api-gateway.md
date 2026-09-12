# API Gateway

The API Gateway is the planned external entry point for OEFMS.

It owns edge routing, request correlation, and the first security integration point before business services expose production APIs directly.

## Current State

The gateway now has foundation routing for the current service boundaries.

It provides:

- Route definitions for Auth and every scaffolded business service.
- A generated or propagated `X-Correlation-Id` header for every request.
- A local route metadata endpoint.
- Optional JWT validation for `/api/**` routes.

It does not yet provide rate limiting, tenant resolution, service discovery, resilience policies, centralised authorization policy, or production edge hardening.

## Local Route Metadata Endpoint

The local route metadata endpoint is:

```text
GET /gateway/routes
```

It returns route ids, public path patterns, the correlation header name, and whether JWT security is enabled. It does not return downstream service URLs.

## Route Prefixes

| Public Path | Service Boundary |
| --- | --- |
| `/auth/**`, `/oauth2/**`, `/.well-known/**`, `/userinfo`, `/login`, `/login/**` | Auth Service |
| `/api/identity/**` | Identity Service |
| `/api/admin/**` | Administration Service |
| `/api/configuration/**` | Configuration Management Service |
| `/api/v1/ingestion/**` | Ingestion Management Service |
| `/api/v1/fraud/**` | Fraud Detection Service |
| `/api/explanations/**` | Explainable AI Service |
| `/api/v1/rules`, `/api/v1/rules/**` | Rule Management Service |
| `/api/v1/cases`, `/api/v1/cases/**`, `/api/v1/alerts`, `/api/v1/alerts/**` | Case Management Service |
| `/api/v1/audit-events`, `/api/v1/audit-events/**` | Audit Logging Service |
| `/api/lists/**` | List Management Service |
| `/api/cases/**` | Case Management Service |
| `/api/audit/**` | Audit Logging Service |
| `/api/notifications/**` | Notification Management Service |
| `/api/reports/**` | Report Management Service |
| `/api/gdpr/**` | GDPR Service |

Most scaffolded business-service routes strip the first two path segments before forwarding. For example:

```text
/api/lists/actuator/health -> /actuator/health
```

Ingestion Management, Fraud Detection Core, Rule Management, Case Management, and Audit Logging keep their versioned service paths end to end:

```text
/api/v1/ingestion/transactions -> /api/v1/ingestion/transactions
/api/v1/fraud/decisions -> /api/v1/fraud/decisions
/api/v1/fraud/ml/runtime -> /api/v1/fraud/ml/runtime
/api/v1/rules -> /api/v1/rules
/api/v1/cases -> /api/v1/cases
/api/v1/alerts -> /api/v1/alerts
/api/v1/audit-events -> /api/v1/audit-events
```

## Request Correlation

The gateway accepts an existing `X-Correlation-Id` header when supplied. If the request does not include one, the gateway generates a UUID correlation id.

The same header is returned to the caller and forwarded to downstream services.

## Security

Gateway JWT security is disabled locally by default:

```text
OEFMS_GATEWAY_SECURITY_ENABLED=false
```

When enabled, the gateway validates bearer tokens for `/api/**` routes using:

```text
OEFMS_GATEWAY_AUTH_JWK_SET_URI
```

Auth endpoints, actuator health/info, and `/gateway/routes` remain public so login, discovery, and local smoke checks can work.

## Downstream Service URLs

All downstream URLs are environment-driven. Application fallbacks point to localhost ports. Docker Compose and `.env.example` use service DNS names such as `http://fraud-detection-service:8081`.

Use the localhost defaults when running services directly from the JVM. Use the Compose service DNS names when running inside Docker.
