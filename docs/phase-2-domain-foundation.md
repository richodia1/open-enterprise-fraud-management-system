# Phase 2 Domain Foundation

Phase 2 turns the Phase 1 platform spine into the first end-to-end fraud-management workflow.

The goal is not to build the entire product at once. The goal is to add a thin, understandable domain slice that contributors can run locally, review in small pull requests, and extend safely.

## Goals

- Implement the first transaction ingestion API.
- Publish transaction-received events through RabbitMQ.
- Consume transaction events in Fraud Detection Service.
- Produce a rule-mode fraud decision from the transaction event.
- Prepare an investigator-facing explanation from decision evidence.
- Create the first alert and case-management handoff.
- Record an audit trail for the transaction, decision, explanation, and case creation steps.
- Keep PostgreSQL as the local test database while preserving environment-driven database configuration.
- Keep ML/AI integration planned but disabled until the data and model-serving boundaries are ready.

## Core Flow

```text
External bank, channel, or payment system
        |
        v
API Gateway
        |
        v
Ingestion Management Service
        |
        | publishes TransactionReceivedEvent
        v
RabbitMQ
        |
        +--> Fraud Detection Service
        |        |
        |        | publishes FraudDecisionProducedEvent
        |        v
        +--> Explainable AI Service
        |        |
        |        | publishes ExplanationPreparedEvent
        |        v
        +--> Case Management Service
        |
        +--> Audit Logging Service
```

The first implementation should keep this flow deliberately narrow. A single transaction can move through ingestion, rule scoring, explanation support, case creation, and audit logging before broader workflows are added.

## Branch Plan

Use one branch per focused change.

| Order | Branch | Purpose |
| --- | --- | --- |
| 1 | `feature/phase-2-domain-foundation-plan` | Capture the Phase 2 sequence and domain implementation boundaries. |
| 2 | `feature/ingestion-transaction-api` | Add the first transaction ingestion REST endpoint and request validation. |
| 3 | `feature/transaction-event-publisher` | Add a small RabbitMQ publisher abstraction and publish `TransactionReceivedEvent`. |
| 4 | `feature/fraud-transaction-consumer` | Consume transaction events in Fraud Detection Service and reuse the rule decision engine. |
| 5 | `feature/fraud-decision-events` | Publish `FraudDecisionProducedEvent` with decision evidence, rule hits, and correlation metadata. |
| 6 | `feature/explanation-decision-support` | Add the first explanation endpoint or consumer that turns decision evidence into investigator support. |
| 7 | `feature/case-alert-audit-flow` | Create the first alert/case handoff and audit trail from decision and explanation events. |
| 8 | `feature/rule-management-foundation` | Add configurable rule definitions after the basic event flow is working. |
| 9 | `feature/ml-runtime-contract` | Define the ML scoring contract and runtime switch without enabling production ML by default. |
| 10 | `feature/transaction-persistence-foundation` | Persist received transaction receipts in the owning Ingestion Management schema. |
| 11 | `feature/fraud-decision-persistence-foundation` | Persist fraud decision summaries in the owning Fraud Detection schema. |
| 12 | `feature/ml-dataset-foundation` | Track fraud dataset references and add the first reproducible feature-matrix preparation path. |

## Service Responsibilities

### Ingestion Management Service

- Accept transaction ingestion requests from external banking or payment systems.
- Validate required transaction fields, timestamps, amount, currency, channel, and parties.
- Assign or propagate request correlation metadata.
- Publish `TransactionReceivedEvent`.
- Persist received transaction receipts and publication status in its own schema.
- Avoid fraud scoring logic; that belongs in Fraud Detection Service.

### Fraud Detection Service

- Consume transaction-received events from `oefms.fraud-detection.transaction-received`.
- Validate the event type and supported event version before scoring.
- Run the configured decision mode.
- Keep `rules` as the default mode.
- Route `ml` and `hybrid` modes through the ML scoring contract while keeping the default runtime disabled.
- Publish `FraudDecisionProducedEvent` with outcome, score, risk band, rule hits, model signals, and evidence references.
- Persist a durable fraud decision summary for traceability and later investigation views.

### ML Dataset Workspace

- Track external dataset references and source mappings without committing raw data.
- Standardize local raw datasets into a transaction-like interim schema.
- Generate a feature matrix suitable for tabular baselines such as logistic regression, random forest, and XGBoost.
- Emit a manifest that connects dataset lineage to the Java `MlScoringClient` runtime contract.

### Rule Management Service

- Expose seeded baseline rule definitions through `GET /api/v1/rules`.
- Support tenant-scoped rule creation, replacement, and lifecycle status updates.
- Keep the first catalog in memory until the persistence lifecycle is settled.
- Keep dynamic Fraud Detection integration for a later branch so rule authoring and decision execution remain reviewable separately.

### Explainable AI Service

- Consume fraud-decision events from `oefms.explainable-ai.fraud-decision-produced`.
- Convert rule hits, model signals, transaction attributes, and historical context into clear decision support.
- Explain why a transaction is risky before a user confirms or rejects a fraud outcome.
- Avoid becoming the final fraud authority.
- Publish `ExplanationPreparedEvent` for downstream case and audit workflows.

### Case Management Service

- Consume explanation events from `oefms.case-management.explanation-prepared`.
- Create an initial alert and case when a transaction needs investigation.
- Publish alert, case, and audit events for reviewable explanations.
- Skip low-risk approvals that do not need investigation.
- Store status, priority, assignment-ready fields, and explanation references.
- Leave full investigation workflow for a later phase.

### Audit Logging Service

- Consume audit events from `oefms.audit-logging.audit-event-recorded`.
- Record audit events for alert and case creation to logs until persistence is added.
- Prepare for append-only audit events across ingestion, fraud decision, explanation preparation, and case creation.
- Preserve correlation IDs and event IDs so the full chain can be traced.
- Avoid editable business workflows.

## Persistence Direction

Phase 2 may introduce the first service-owned persistence models, but only where the workflow needs them.

Each service should own its schema. Shared modules should not contain JPA entities for service-specific business data.

Recommended first persistence boundaries:

| Service | First Persistent Records |
| --- | --- |
| Ingestion Management Service | Received transaction record and ingestion status. |
| Fraud Detection Service | Fraud decision record and decision evidence summary. |
| Rule Management Service | Rule definitions, versions, status history, and approval metadata. |
| Explainable AI Service | Explanation record linked to transaction and decision IDs. |
| Case Management Service | Case record linked to transaction, decision, and explanation IDs. |
| Audit Logging Service | Append-only audit event record. |

Flyway migrations should live inside the owning service. PostgreSQL remains the local test database for v0.1.

## Event Reliability Direction

The first event path can start simple, but it should leave room for stronger reliability.

- Use RabbitMQ exchanges, routing keys, and queues from `common-events`.
- Include event ID, correlation ID, causation ID, source service, schema version, and timestamp.
- Make consumers idempotent around event ID or business transaction ID.
- Prefer explicit retry/dead-letter behavior when consumers become stateful.
- Add an outbox pattern before relying on database writes and message publishing as one business action.
- Treat direct request-path publication as an early Phase 2 step, not the final reliability model.

## ML And Explainability Direction

ML should not be rushed into the live service path.

Phase 2 can define the runtime contract for ML scoring, but production use should wait until datasets, training scripts, validation metrics, model cards, explainability output, and deployment controls exist.

The expected decision modes remain:

| Mode | Phase 2 Posture |
| --- | --- |
| `rules` | Default and runnable. |
| `ml` | Contract-ready but disabled unless a validated scorer is configured. |
| `hybrid` | Contract-ready and combines rule/ML posture when a validated scorer is configured. |

## Out Of Scope For Phase 2

- Full multi-tenant administration.
- Production-grade identity federation.
- Complete case-management workflow.
- Full rule authoring UI.
- Production ML serving.
- Kubernetes deployment.
- Real bank connector certifications.
- Production data retention and privacy automation.

## Exit Criteria

Phase 2 is complete when:

- `mvn clean verify` passes across all modules.
- Static smoke checks pass.
- The local Docker Compose configuration remains valid.
- A transaction can be submitted through the API Gateway to Ingestion Management Service.
- Ingestion Management Service can read back the persisted received transaction receipt.
- A transaction-received event is published to RabbitMQ.
- Fraud Detection Service can consume the event and produce a rule-mode decision.
- Explainable AI Service can prepare decision-support output from the fraud decision.
- Case Management Service can receive or create the first review case.
- Audit Logging Service can record the major workflow events.
- The ML runtime contract is implemented and documented without enabling unvalidated ML by default.
