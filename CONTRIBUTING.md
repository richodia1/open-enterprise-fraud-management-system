# Contributing

This project is at scaffold stage.

Early contributors should focus on architecture, service boundaries, build tooling, documentation, and the first minimal fraud-check API design before adding business entities or persistence logic.

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
