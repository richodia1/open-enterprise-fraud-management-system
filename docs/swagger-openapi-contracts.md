# Swagger OpenAPI Contracts

Every runnable OEFMS Spring Boot module exposes its live REST contract through Springdoc OpenAPI.

Start the local stack:

```bash
docker compose --env-file .env.example up -d --build
```

Print the Swagger UI and OpenAPI JSON URLs:

```bash
./scripts/list-swagger-contracts.sh
```

Check which contracts are reachable on a running stack:

```bash
./scripts/list-swagger-contracts.sh --check
```

Stop the local stack when finished:

```bash
docker compose --env-file .env.example down
```

## Default Contract URLs

| Service | Swagger UI | OpenAPI JSON |
| --- | --- | --- |
| API Gateway | `http://localhost:8080/swagger-ui.html` | `http://localhost:8080/v3/api-docs` |
| Fraud Detection Service | `http://localhost:8081/swagger-ui.html` | `http://localhost:8081/v3/api-docs` |
| Rule Management Service | `http://localhost:8082/swagger-ui.html` | `http://localhost:8082/v3/api-docs` |
| List Management Service | `http://localhost:8083/swagger-ui.html` | `http://localhost:8083/v3/api-docs` |
| Case Management Service | `http://localhost:8084/swagger-ui.html` | `http://localhost:8084/v3/api-docs` |
| Auth Service | `http://localhost:8085/swagger-ui.html` | `http://localhost:8085/v3/api-docs` |
| Administration Service | `http://localhost:8086/swagger-ui.html` | `http://localhost:8086/v3/api-docs` |
| Identity Service | `http://localhost:8087/swagger-ui.html` | `http://localhost:8087/v3/api-docs` |
| Configuration Management Service | `http://localhost:8088/swagger-ui.html` | `http://localhost:8088/v3/api-docs` |
| Ingestion Management Service | `http://localhost:8089/swagger-ui.html` | `http://localhost:8089/v3/api-docs` |
| Audit Logging Service | `http://localhost:8090/swagger-ui.html` | `http://localhost:8090/v3/api-docs` |
| Notification Management Service | `http://localhost:8091/swagger-ui.html` | `http://localhost:8091/v3/api-docs` |
| Report Management Service | `http://localhost:8092/swagger-ui.html` | `http://localhost:8092/v3/api-docs` |
| GDPR Service | `http://localhost:8093/swagger-ui.html` | `http://localhost:8093/v3/api-docs` |
| Explainable AI Service | `http://localhost:8094/swagger-ui.html` | `http://localhost:8094/v3/api-docs` |

The current API Gateway also exposes its own local contract. Downstream service Swagger UIs are intentionally opened on their direct service ports so contributors can inspect each service boundary while the platform is still in early MVP development.

Auth Service protects production OAuth/OIDC flows, but its local Swagger and OpenAPI endpoints are public so contributors can inspect the bootstrap contract without logging in.

Management services expose their legacy CRUD contract foundation under `/api/v1/{service}/resources`. See [Legacy CRUD Contract Map](legacy-crud-contracts.md) for the old-system resources covered by the first Java pass.
