# Architecture

OpenEFMS is structured as a modular monorepo.

The repository contains multiple deployable Spring Boot services and small shared libraries. Each deployable service owns its API, configuration, runtime container, and future persistence boundary.

PostgreSQL is the default local test database for v0.1. The service datasource settings are environment-driven so future deployments can adopt other database providers by supplying the proper JDBC URL, driver, dialect, and migration location.

## High-Level Direction

```text
API Gateway
    |
    +-- Auth Service
    +-- Identity Service
    +-- Administration Service
    +-- Configuration Management Service
    +-- Ingestion Management Service
    +-- Fraud Detection Service
    +-- Rule Management Service
    +-- List Management Service
    +-- Case Management Service
    +-- Audit Logging Service
    +-- Notification Management Service
    +-- Report Management Service
    +-- GDPR Service
```

Future modules may include ML scoring, graph intelligence, device intelligence, and external integration adapters.

No domain entities or business workflows are implemented in this scaffold.
