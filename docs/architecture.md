# Architecture

OpenEFMS is structured as a modular monorepo.

The repository contains multiple deployable Spring Boot services and small shared libraries. Each deployable service owns its API, configuration, runtime container, and future persistence boundary.

PostgreSQL is the default local test database for v0.1. The service datasource settings are environment-driven so future deployments can adopt other database providers by supplying the proper JDBC URL, driver, dialect, and migration location.

Fraud decisioning should support configurable rule-based, ML-based, and hybrid modes. The default mode is rule-based until trained models, validation, explainability, and model governance are added.

The legacy .NET CRUD surfaces are being reintroduced as explicit Java/Spring contracts. The first pass is a contract-first management-resource API so Swagger can show UI-facing resources while persistence and workflow depth are added incrementally.

The foundation sequence is tracked in [Phase 1 Foundation](phase-1-foundation.md). The next domain sequence is tracked in [Phase 2 Domain Foundation](phase-2-domain-foundation.md). Shared API and event shapes are tracked in [Service Contracts](service-contracts.md).

## Service Package Layout

Each Spring Boot service should use the same package shape as domain behavior is added:

| Package | Responsibility |
| --- | --- |
| `controller` | REST controllers and HTTP error handling. |
| `dto` | Service-owned API request or response payloads that are not shared contracts. |
| `entity` | Service-owned JPA entities and domain records. |
| `enums` | Service-owned enum values. Use `enums` because `enum` is a Java keyword. |
| `repository` | Repository interfaces, Spring Data adapters, JPA adapters, and persistence mappers. |
| `service` | Business workflow, validation, orchestration, and service-level exceptions. |
| `messaging` | RabbitMQ publishers, listeners, message envelopes, and broker configuration. |
| `config` | Spring configuration and configuration-properties classes. |

Small scaffolded services keep `package-info.java` markers for empty layers until their first domain slice is added.

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
    +-- Explainable AI Service
    +-- Rule Management Service
    +-- List Management Service
    +-- Case Management Service
    +-- Audit Logging Service
    +-- Notification Management Service
    +-- Report Management Service
    +-- GDPR Service
```

Fraud decisions should be traceable. The Explainable AI Service is intended to produce investigator-facing decision support from rule hits, model signals, feature contributions, historical context, and audit trails. It should explain why a transaction is risky before a user finally confirms or rejects a fraud outcome.

Future modules may include ML scoring, graph intelligence, device intelligence, and external integration adapters.
