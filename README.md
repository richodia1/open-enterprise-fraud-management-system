# Open Enterprise Fraud Management System

OEFMS is an open-source enterprise fraud management platform designed to provide a modular, extensible foundation for real-time fraud detection, risk assessment, alert management, explainable AI, and fraud investigation across banking and financial services.

OEFMS v0.1 is the initial foundation of the project and is being developed collaboratively by a group of founding contributors with expertise across software architecture, backend engineering, AI/ML, data engineering, cybersecurity, DevOps, and frontend development.

The project aims to provide an open and adaptable architecture that can be extended for different financial institutions, transaction types, fraud scenarios, regulatory environments, and technology stacks.

OEFMS is still in its foundation phase. It currently defines the project layout, Spring Boot service shells, shared contracts, local infrastructure, Auth foundation, API Gateway routing, RabbitMQ conventions, transaction ingestion with a service-owned receipt store, persisted rule-mode fraud decisions, explainable decision support, the first case and alert handoff, persisted case, alert, and audit event records, an in-memory rule-management catalog, an ML runtime contract, the first ML dataset preparation foundation, optional XGBoost and model-comparison experiment paths, and an offline ML leakage/slice-audit workflow.

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

Phase 1 established the platform foundation: service contracts, Auth, API Gateway routing, RabbitMQ messaging, local runtime hygiene, and the first rule-mode fraud decision spine.

Phase 2 is the next implementation phase. It focuses on the first end-to-end domain workflow: transaction ingestion, RabbitMQ event publication, fraud decision consumption, explainable decision support, case handoff, and audit logging.

See [Phase 1 Foundation](docs/phase-1-foundation.md), [Phase 2 Domain Foundation](docs/phase-2-domain-foundation.md), [MVP Live Stack Readiness](docs/mvp-live-stack-readiness.md), [Transaction Persistence Foundation](docs/transaction-persistence-foundation.md), [Fraud Decision Persistence Foundation](docs/fraud-decision-persistence-foundation.md), [Case Alert Persistence Foundation](docs/case-alert-persistence-foundation.md), [Audit Log Persistence Foundation](docs/audit-log-persistence-foundation.md), [ML Dataset Foundation](docs/ml-dataset-foundation.md), [Ingestion Management API](docs/ingestion-management-api.md), [Fraud Transaction Consumer](docs/fraud-transaction-consumer.md), [Fraud Decision Events](docs/fraud-decision-events.md), [Explanation Decision Support](docs/explanation-decision-support.md), [Case Alert Audit Flow](docs/case-alert-audit-flow.md), [Rule Management Foundation](docs/rule-management-foundation.md), [ML Runtime Contract](docs/ml-runtime-contract.md), [ML Leakage And Fairness Audit](docs/ml-leakage-fairness-audit.md), [Service Contracts](docs/service-contracts.md), [RabbitMQ Event Spine](docs/rabbitmq-event-spine.md), [Fraud Detection Core](docs/fraud-detection-core.md), and [Platform Configuration](docs/platform-configuration.md).

## Fraud Decision Modes

The Fraud Detection Service is prepared for configurable decisioning:

```text
OEFMS_FRAUD_DECISION_MODE=rules
```

Configured values are `rules`, `ml`, and `hybrid`. The default is `rules`; `ml` and `hybrid` require a production `MlScoringClient` adapter before they can score live transactions.

See [ML and AI Strategy](docs/ml-ai-strategy.md) for the dataset and model direction.

## ML Research Baseline

The preferred first academic dataset is the Bank Account Fraud Dataset Suite `Base.csv`. Keep the raw file local at:

```text
data/raw/bank-account-fraud/Base.csv
```

Prepare the feature matrix:

```bash
./scripts/prepare-ml-dataset.sh bank_account_fraud_neurips_2022
```

Optionally train the XGBoost baseline after installing the Python ML stack:

```bash
python3 -m pip install -r ml/requirements.txt
python3 ml/experiments/xgboost-baseline/train_xgboost_baseline.py
```

Training outputs stay under ignored `ml/artifacts/`.

See [XGBoost Baseline Results](docs/ml-xgboost-baseline-results.md) for the first local full-dataset BAF run summary and [ML Threshold Calibration And Comparison](docs/ml-threshold-calibration-and-comparison.md) for the model-comparison workflow.

Generate prediction scores and run the leakage/slice audit:

```bash
python3 ml/experiments/model-comparison/compare_tabular_models.py --write-scores
python3 ml/experiments/model-audit/audit_dataset_and_predictions.py
```

See [ML Leakage And Fairness Audit](docs/ml-leakage-fairness-audit.md) for the first full local BAF audit summary.

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

Ingestion Management transaction receipt persistence, Fraud Detection decision persistence, Case Management alert/case persistence, and Audit Logging event persistence have been added. Production fraud policy, full investigation workflow, durable explanation storage, and the frontend analyst console have not been added yet.

See [Explainable AI](docs/explainable-ai.md) for the planned decision-support boundary.

See [Auth Service](docs/auth-service.md) for the local authentication and OAuth2/OIDC foundation.

See [API Gateway](docs/api-gateway.md) for the first route map, request correlation, and security integration points.

See [RabbitMQ Event Spine](docs/rabbitmq-event-spine.md) for the first broker exchange, queue, routing-key, binding, and header conventions.

See [Fraud Detection Core](docs/fraud-detection-core.md) for the first rule-mode decision endpoint and orchestration path.

See [Fraud Transaction Consumer](docs/fraud-transaction-consumer.md) for the first RabbitMQ listener in Fraud Detection Service.

See [Fraud Decision Events](docs/fraud-decision-events.md) for publishing rule-mode fraud decisions to RabbitMQ.

See [Explanation Decision Support](docs/explanation-decision-support.md) for the first explanation consumer and event publisher.

See [Case Alert Audit Flow](docs/case-alert-audit-flow.md) for the first investigator case handoff and audit event path.

See [Case Alert Persistence Foundation](docs/case-alert-persistence-foundation.md) for the first durable alert and case records.

See [Audit Log Persistence Foundation](docs/audit-log-persistence-foundation.md) for the first durable audit event records.

See [Rule Management Foundation](docs/rule-management-foundation.md) for the first configurable rule catalog API.

See [ML Runtime Contract](docs/ml-runtime-contract.md) for the scorer interface, status endpoint, and `ml`/`hybrid` mode boundary.

## Local Smoke Checks

CI runs the static local smoke checks after the Maven build:

```bash
./scripts/smoke-local.sh --skip-build
```

For a running local stack, add live health and decision probes:

```bash
./scripts/smoke-local.sh --skip-build --live
```

For the full MVP event-flow probe through the API Gateway:

```bash
./scripts/smoke-local.sh --skip-build --start-stack --gateway --event-flow
```

See [Local Smoke Tests](docs/local-smoke-tests.md) for live stack and optional gateway probes.
