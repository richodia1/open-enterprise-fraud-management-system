package org.openefms.explainableai.messaging;

import java.util.ArrayList;
import java.util.List;
import org.openefms.common.events.EventEnvelope;
import org.openefms.common.events.EventMetadata;
import org.openefms.common.events.EventTypes;
import org.openefms.common.events.fraud.FraudDecisionProducedEvent;
import org.openefms.common.events.rabbitmq.RabbitMqQueues;
import org.openefms.explainableai.service.ExplanationOrchestrator;
import org.openefms.explainableai.service.InvalidFraudDecisionProducedEventException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class FraudDecisionProducedEventListener {
    private static final int SUPPORTED_EVENT_VERSION = 1;

    private final ExplanationOrchestrator orchestrator;
    private final ExplanationMessageSink messageSink;

    public FraudDecisionProducedEventListener(
            ExplanationOrchestrator orchestrator,
            ExplanationMessageSink messageSink) {
        this.orchestrator = orchestrator;
        this.messageSink = messageSink;
    }

    @RabbitListener(queues = RabbitMqQueues.EXPLAINABLE_AI_FRAUD_DECISION_PRODUCED)
    public void handle(EventEnvelope<FraudDecisionProducedEvent> event) {
        validate(event);

        ExplanationMessage explanationMessage = orchestrator.explain(event);
        messageSink.accept(explanationMessage);
    }

    private void validate(EventEnvelope<FraudDecisionProducedEvent> event) {
        List<String> errors = new ArrayList<>();
        if (event == null) {
            errors.add("event must not be null");
            throw new InvalidFraudDecisionProducedEventException(errors);
        }

        EventMetadata metadata = event.metadata();

        if (!EventTypes.FRAUD_DECISION_PRODUCED.equals(metadata.eventType())) {
            errors.add("eventType must be " + EventTypes.FRAUD_DECISION_PRODUCED);
        }
        if (metadata.eventVersion() != SUPPORTED_EVENT_VERSION) {
            errors.add("eventVersion must be " + SUPPORTED_EVENT_VERSION);
        }

        if (!errors.isEmpty()) {
            throw new InvalidFraudDecisionProducedEventException(errors);
        }
    }
}
