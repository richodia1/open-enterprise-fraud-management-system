# Contributing

This project is at scaffold stage.

Early contributors should focus on architecture, service boundaries, build tooling, documentation, and the first minimal fraud-check API design before adding business entities or persistence logic.

See [Phase 1 Foundation](docs/phase-1-foundation.md) for the current implementation sequence and branch plan.

## Branches

Use one focused branch per change. Current Phase 1 branches should follow names such as:

- `feature/service-contracts`
- `feature/auth-service-foundation`
- `feature/api-gateway-routing`
- `feature/rabbitmq-event-spine`
- `feature/fraud-detection-core`

## Local Requirements

- JDK 21
- Maven 3.9+
- Docker Desktop or Docker Engine

## Build

```bash
mvn clean verify
```

## Local Infrastructure

```bash
docker compose up --build
```
