# Kafka Event Spine

Kafka is available as an alternative broker for the MVP event spine. RabbitMQ remains the default local transport, but contributors can run the same event workflow through Kafka by setting:

```bash
OEFMS_EVENT_TRANSPORT=kafka
```

The transport switch is intentionally narrow. It changes how services publish and consume integration events, while keeping the shared `EventEnvelope<T>` payload, event metadata, validation rules, and service orchestrators the same.

## Topics

Topic names are exposed in `KafkaTopics`.

| Event Type | Kafka Topic |
| --- | --- |
| `transaction.received` | `oefms.transaction.received` |
| `fraud.decision.produced` | `oefms.fraud.decision.produced` |
| `explanation.prepared` | `oefms.explanation.prepared` |
| `fraud.feedback.ready` | `oefms.fraud.feedback.ready` |
| `alert.created` | `oefms.alert.created` |
| `case.created` | `oefms.case.created` |
| `audit.event.recorded` | `oefms.audit.event.recorded` |

Kafka auto topic creation is enabled in the local Docker Compose broker so contributors do not need a separate topic bootstrap step for development.

## Consumer Groups

| Consumer Service | Default Group |
| --- | --- |
| Fraud Detection Service | `oefms.fraud-detection` |
| Explainable AI Service | `oefms.explainable-ai` |
| Case Management Service | `oefms.case-management` |
| Audit Logging Service | `oefms.audit-logging` |

The consumer group values can be overridden through:

```text
OEFMS_KAFKA_FRAUD_CONSUMER_GROUP
OEFMS_KAFKA_EXPLAINABLE_AI_CONSUMER_GROUP
OEFMS_KAFKA_CASE_CONSUMER_GROUP
OEFMS_KAFKA_AUDIT_CONSUMER_GROUP
```

## Header Contract

Kafka publishers copy searchable event metadata into record headers in addition to the JSON payload:

| Header | Source |
| --- | --- |
| `X-OEFMS-Event-Id` | `EventMetadata.eventId` |
| `X-OEFMS-Event-Type` | `EventMetadata.eventType` |
| `X-OEFMS-Event-Version` | `EventMetadata.eventVersion` |
| `X-OEFMS-Source-Service` | `EventMetadata.sourceService` |
| `X-OEFMS-Tenant-Id` | `EventMetadata.tenantId` |
| `X-Correlation-Id` | `EventMetadata.correlationId` |
| `X-OEFMS-Causation-Id` | `EventMetadata.causationId` |
| `X-OEFMS-Trace-Id` | `EventMetadata.traceId` |

The header names are exposed in `KafkaHeaders`.

## Local Run

RabbitMQ is still the default:

```bash
./scripts/smoke-local.sh --skip-build --start-stack --gateway --event-flow
```

To run the same MVP flow on Kafka, override the transport:

```bash
OEFMS_EVENT_TRANSPORT=kafka ./scripts/smoke-local.sh --skip-build --start-stack --gateway --event-flow
```

The smoke and demo scripts start the broker selected by `OEFMS_EVENT_TRANSPORT`. Event-facing services then decide which publisher and listener beans to use from the same value.

## Current Scope

Kafka is wired for the active MVP event chain:

```text
Ingestion Management Service
        |
        v
Kafka oefms.transaction.received
        |
        v
Fraud Detection Service
        |
        v
Kafka oefms.fraud.decision.produced
        |
        v
Explainable AI Service
        |
        v
Kafka oefms.explanation.prepared
        |
        +--> Kafka oefms.fraud.feedback.ready
        |
        v
Case Management Service
        |
        +--> fraud_alerts
        +--> fraud_cases
        +--> Kafka oefms.audit.event.recorded
                 |
                 v
          Audit Logging Service
                 |
                 v
          audit_events
```

Notification delivery is still outside this live MVP event chain. When notification workers are implemented, they should follow the same transport switch pattern.
