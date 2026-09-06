# Architecture

OpenEFMS is structured as a modular monorepo.

The repository contains multiple deployable Spring Boot services and small shared libraries. Each deployable service owns its API, configuration, runtime container, and future persistence boundary.

## High-Level Direction

```text
API Gateway
    |
    +-- Auth Service
    +-- Fraud Detection Service
    +-- Rule Management Service
    +-- List Management Service
    +-- Case Management Service
```

Future modules may include notification, reporting, ingestion, audit, administration, and ML scoring services.

No domain entities or business workflows are implemented in this scaffold.
