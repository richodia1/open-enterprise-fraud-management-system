# Fraud Transaction Consumer

Fraud Detection Service now has the first RabbitMQ consumer for the Phase 2 transaction flow.

## Consumed Event

| Field | Value |
| --- | --- |
| Queue | `oefms.fraud-detection.transaction-received` |
| Exchange | `oefms.events` |
| Routing key | `transaction.received` |
| Payload | `EventEnvelope<TransactionReceivedEvent>` |
| Supported event version | `1` |

The listener is intentionally thin. It validates the incoming event contract and delegates fraud scoring to `FraudDecisionOrchestrator`.

## Processing Path

```text
RabbitMQ transaction.received event
        |
        v
TransactionReceivedEventListener
        |
        | validates event type and version
        v
FraudDecisionOrchestrator
        |
        | runs configured decision mode
        v
FraudDecisionMessageSink
```

The current sink publishes a `FraudDecisionProducedEvent` message to the shared RabbitMQ event exchange.

## Requeue Behaviour

Malformed transaction events are rejected by the listener. Local configuration sets:

```text
OEFMS_RABBITMQ_DEFAULT_REQUEUE_REJECTED=false
```

That prevents invalid messages from being immediately requeued forever.

## Published Event

The listener hands the scored transaction to the fraud decision publisher. The publisher sends:

| Field | Value |
| --- | --- |
| Exchange | `oefms.events` |
| Routing key | `fraud.decision.produced` |
| Payload | `EventEnvelope<FraudDecisionProducedEvent>` |
| Delivery mode | Persistent |

The publisher copies event id, event type, event version, source service, tenant, correlation id, causation id, and trace id into RabbitMQ headers.
