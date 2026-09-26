package org.openefms.frauddetection.messaging;

import java.util.ArrayList;
import java.util.List;
import org.openefms.common.events.EventEnvelope;
import org.openefms.common.events.EventMetadata;
import org.openefms.common.events.EventTypes;
import org.openefms.common.events.rabbitmq.RabbitMqQueues;
import org.openefms.common.events.transaction.TransactionReceivedEvent;
import org.openefms.frauddetection.service.FraudDecisionOrchestrator;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "oefms.event-transport", havingValue = "rabbitmq", matchIfMissing = true)
public class TransactionReceivedEventListener {
    private static final int SUPPORTED_EVENT_VERSION = 1;

    private final FraudDecisionOrchestrator orchestrator;
    private final FraudDecisionMessageSink messageSink;

    public TransactionReceivedEventListener(
            FraudDecisionOrchestrator orchestrator,
            FraudDecisionMessageSink messageSink) {
        this.orchestrator = orchestrator;
        this.messageSink = messageSink;
    }

    @RabbitListener(queues = RabbitMqQueues.FRAUD_DETECTION_TRANSACTION_RECEIVED)
    public void handle(EventEnvelope<TransactionReceivedEvent> event) {
        validate(event);

        FraudDecisionMessage decisionMessage = orchestrator.decide(event);
        messageSink.accept(decisionMessage);
    }

    private void validate(EventEnvelope<TransactionReceivedEvent> event) {
        List<String> errors = new ArrayList<>();
        if (event == null) {
            errors.add("event must not be null");
            throw new InvalidTransactionReceivedEventException(errors);
        }

        EventMetadata metadata = event.metadata();

        if (!EventTypes.TRANSACTION_RECEIVED.equals(metadata.eventType())) {
            errors.add("eventType must be " + EventTypes.TRANSACTION_RECEIVED);
        }
        if (metadata.eventVersion() != SUPPORTED_EVENT_VERSION) {
            errors.add("eventVersion must be " + SUPPORTED_EVENT_VERSION);
        }

        if (!errors.isEmpty()) {
            throw new InvalidTransactionReceivedEventException(errors);
        }
    }
}
