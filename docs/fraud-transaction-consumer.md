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

The current sink logs that a `FraudDecisionProducedEvent` message has been prepared. It does not publish the decision event yet.

## Requeue Behaviour

Malformed transaction events are rejected by the listener. Local configuration sets:

```text
OEFMS_RABBITMQ_DEFAULT_REQUEUE_REJECTED=false
```

That prevents invalid messages from being immediately requeued forever.

## Next Branch

`feature/fraud-decision-events` should replace or extend the current sink with a RabbitMQ publisher that sends `fraud.decision.produced` messages to the shared event exchange with the same header convention used by ingestion.
