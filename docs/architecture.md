# Architecture

OpenEFMS is structured as a modular monorepo.

The repository contains multiple deployable Spring Boot services and small shared libraries. Each deployable service owns its API, configuration, runtime container, and future persistence boundary.

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
