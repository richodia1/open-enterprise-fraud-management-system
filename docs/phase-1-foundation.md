# Phase 1 Foundation

Phase 1 turns the scaffold into a reliable platform spine before domain-heavy fraud workflows are added.

The focus is structure, contracts, local infrastructure, Auth, Gateway, messaging, and the first fraud decision path. It should remain deliberately small so later contributors can understand the project quickly.

## Goals

- Keep the Java 21 Spring Boot multi-module structure buildable and easy to extend.
- Establish clear service boundaries before adding entities, repositories, or workflow logic.
- Define the first transaction, event, fraud decision, alert, explanation, case, and audit contracts.
- Prepare RabbitMQ as the first asynchronous event backbone.
- Implement the Auth Service foundation before protected business APIs are exposed.
- Implement API Gateway routing, request correlation, and security integration.
- Keep PostgreSQL as the local test database while retaining environment-driven provider configuration.
- Keep ML datasets, model artifacts, secrets, and environment-specific files out of Git.
- Keep CI passing after every small branch.

## Branch Plan

Use one branch per focused foundation change.

| Order | Branch | Purpose |
| --- | --- | --- |
| 1 | `feature/phase-1-foundation-plan` | Capture the Phase 1 roadmap and immediate cleanup items. |
| 2 | `feature/platform-config-cleanup` | Tighten compose, env, health, and local runtime configuration. |
| 3 | `feature/service-contracts` | Add initial shared API and event contracts without persistence entities. |
| 4 | `feature/auth-service-foundation` | Add the first Auth Service foundation for users, clients, roles, permissions, and token flow. |
| 5 | `feature/api-gateway-routing` | Add Gateway route definitions, request correlation, and auth integration points. |
| 6 | `feature/rabbitmq-event-spine` | Add exchange, queue, routing-key, publisher, and consumer conventions. |
| 7 | `feature/fraud-detection-core` | Add the first fraud-check orchestration path using rule mode as the default. |
| 8 | `feature/phase-1-local-smoke-tests` | Add local smoke checks for build, compose config, health endpoints, and first route flow. |

## Workstreams

### Repository And Delivery Hygiene

- Keep branches short-lived and PR-sized.
- Require `mvn clean verify` before merge.
- Keep `docker compose config` valid after every infrastructure change.
- Run local smoke checks before larger integration branches merge.
- Review license, README, security notes, and contribution guidance before any future public release.

### Architecture And Contracts

- Treat shared modules as contract libraries, not business-logic dumping grounds.
- Define stable request, response, and event names before building persistence.
- Keep contract versioning in mind from the first API and event models.
- Record major architectural choices in documentation before implementation spreads across services.
- Initial shared API and event shapes are tracked in [Service Contracts](service-contracts.md).

### Local Infrastructure

- PostgreSQL remains the default local database provider.
- RabbitMQ is the default local broker.
- Redis remains available for future caching, idempotency, or rate-limiting support.
- Service configuration must remain environment-driven so deployments can swap provider details.
- Shared runtime environment rules are tracked in [Platform Configuration](platform-configuration.md).
- Local validation is tracked in [Local Smoke Tests](local-smoke-tests.md).

### Auth And Gateway

- Auth Service should be implemented before protected business endpoints.
- Gateway should become the main entry point for external clients.
- Gateway should handle request correlation, route-level security hooks, and edge concerns.
- Service-to-service trust should be designed before internal calls become complex.

### Fraud Decision Spine

- Ingestion Management Service receives and normalises external transaction events.
- Fraud Detection Service produces the preliminary risk decision.
- Rule mode remains the default decision mode.
- ML and hybrid modes remain configuration-ready but should not be enabled until validated models and model-serving boundaries exist.
- Explainable AI Service receives decision evidence and prepares investigator-facing explanations.
- Case Management Service remains the final human investigation workflow boundary.

## Out Of Scope For Phase 1

- Full production authentication hardening.
- Complete fraud rules management.
- Real banking integrations.
- Full case-management workflow.
- Production ML model serving.
- Training pipelines with production model governance.
- Frontend analyst console.
- Kubernetes or cloud deployment automation.

## Exit Criteria

Phase 1 is complete when:

- `mvn clean verify` passes across all modules.
- `docker compose config` passes.
- The local stack can start with PostgreSQL, RabbitMQ, Redis, Gateway, Auth, Ingestion, Fraud Detection, Explainable AI, and Case Management services.
- API Gateway has the first documented routes to core services.
- Auth Service has a minimal documented token and access-control foundation.
- Initial service contracts exist for transaction ingestion, fraud decisioning, explanation support, alert creation, case creation, and audit events.
- RabbitMQ event naming and routing conventions are documented and used by the first fraud decision path.
- Fraud Detection Service can run in `rules` mode by default.
- ML and hybrid modes remain disabled until training, validation, explainability, and serving are implemented.
- No raw datasets, trained model artifacts, local secrets, or generated build outputs are committed.
