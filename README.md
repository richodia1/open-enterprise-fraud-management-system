# Open Enterprise Fraud Management System

OEFMS is an open-source enterprise fraud management platform designed to provide a modular, extensible foundation for real-time fraud detection, risk assessment, alert management, explainable AI, and fraud investigation across banking and financial services.

OEFMS v0.1 is the initial foundation of the project and is being developed collaboratively by a group of founding contributors with expertise across software architecture, backend engineering, AI/ML, data engineering, cybersecurity, DevOps, and frontend development.

The project aims to provide an open and adaptable architecture that can be extended for different financial institutions, transaction types, fraud scenarios, regulatory environments, and technology stacks.

This scaffold is intentionally empty of business logic. It only defines the project layout, Spring Boot service shells, shared library modules, Docker build boundaries, and local infrastructure.

## GitHub Repository Description

Open-source modular enterprise fraud management platform for real-time detection, risk scoring, explainable AI, alerts, and investigations.

## Initial Objectives

- Develop a modular enterprise fraud-management architecture.
- Support real-time transaction and event processing.
- Provide configurable fraud detection rules.
- Integrate machine-learning-based fraud detection.
- Provide explainable risk scoring and fraud decisions.
- Support alert generation and prioritisation.
- Provide fraud case-management capabilities.
- Maintain comprehensive audit trails.
- Support integration with banking and payment systems through APIs.
- Provide synthetic datasets and reproducible experiments.
- Enable researchers and developers to extend and evaluate the platform.

The project is intended to be developed openly, with contributions, technical discussions, issues, documentation, and architectural improvements managed through the open-source community.

OEFMS v0.1 is the starting point for building an open, extensible enterprise fraud-management platform for the global financial-services technology community.

## Current Modules

```text
open-enterprise-fraud-management-system/
├── api-gateway/
├── services/
│   ├── auth-service/
│   ├── identity-service/
│   ├── administration-service/
│   ├── configuration-management-service/
│   ├── ingestion-management-service/
│   ├── fraud-detection-service/
│   ├── explainable-ai-service/
│   ├── rule-management-service/
│   ├── list-management-service/
│   ├── case-management-service/
│   ├── audit-logging-service/
│   ├── notification-management-service/
│   ├── report-management-service/
│   └── gdpr-service/
├── shared/
│   ├── common-models/
│   ├── common-events/
│   └── common-persistence/
├── data/
├── ml/
├── infrastructure/
│   ├── postgres/
│   ├── rabbitmq/
│   └── redis/
├── docs/
└── scripts/
```

## Technology Baseline

- Java 21
- Spring Boot 4.1.1
- Spring Cloud 2025.1.3
- Maven multi-module build
- PostgreSQL 16 for local database testing
- Docker Compose for local infrastructure

## Current Roadmap

Phase 1 is the active foundation phase. It focuses on service contracts, Auth, API Gateway routing, RabbitMQ messaging, local runtime hygiene, and the first fraud decision spine before deeper domain implementation.

See [Phase 1 Foundation](docs/phase-1-foundation.md), [Service Contracts](docs/service-contracts.md), and [Platform Configuration](docs/platform-configuration.md).

## Fraud Decision Modes

The Fraud Detection Service is prepared for configurable decisioning:

```text
OEFMS_FRAUD_DECISION_MODE=rules
```

Planned values are `rules`, `ml`, and `hybrid`. The default is `rules` until ML model training, validation, model governance, and serving are implemented.

See [ML and AI Strategy](docs/ml-ai-strategy.md) for the dataset and model direction.

## First Build Target

The first real implementation should be a small vertical slice:

```text
Bank channel or core system
        |
        v
API Gateway
        |
        v
Auth Service validates identity and access
        |
        v
Ingestion Management Service receives and normalises events
        |
        v
Fraud Detection Service produces a preliminary risk decision
        |
        v
Explainable AI Service builds reason and evidence support
        |
        v
Case Management Service presents the explanation to investigators
        |
        v
Investigator confirms, rejects, or escalates the fraud outcome
```

No entities, controllers, repositories, fraud rules, or DTOs have been added yet.

See [Explainable AI](docs/explainable-ai.md) for the planned decision-support boundary.

See [Auth Service](docs/auth-service.md) for the local authentication and OAuth2/OIDC foundation.

See [API Gateway](docs/api-gateway.md) for the first route map, request correlation, and security integration points.
