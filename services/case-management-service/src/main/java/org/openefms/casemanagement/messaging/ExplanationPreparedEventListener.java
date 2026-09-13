package org.openefms.casemanagement.messaging;

import java.util.ArrayList;
import java.util.List;
import org.openefms.casemanagement.service.CaseWorkflowOrchestrator;
import org.openefms.casemanagement.service.InvalidExplanationPreparedEventException;
import org.openefms.common.events.EventEnvelope;
import org.openefms.common.events.EventMetadata;
import org.openefms.common.events.EventTypes;
import org.openefms.common.events.explanation.ExplanationPreparedEvent;
import org.openefms.common.events.rabbitmq.RabbitMqQueues;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class ExplanationPreparedEventListener {
    private static final int SUPPORTED_EVENT_VERSION = 1;

    private final CaseWorkflowOrchestrator orchestrator;
    private final DomainEventMessageSink messageSink;

    public ExplanationPreparedEventListener(CaseWorkflowOrchestrator orchestrator, DomainEventMessageSink messageSink) {
        this.orchestrator = orchestrator;
        this.messageSink = messageSink;
    }

    @RabbitListener(queues = RabbitMqQueues.CASE_MANAGEMENT_EXPLANATION_PREPARED)
    public void handle(EventEnvelope<ExplanationPreparedEvent> event) {
        validate(event);

        orchestrator.prepare(event).messages().forEach(messageSink::accept);
    }

    private void validate(EventEnvelope<ExplanationPreparedEvent> event) {
        List<String> errors = new ArrayList<>();
        if (event == null) {
            errors.add("event must not be null");
            throw new InvalidExplanationPreparedEventException(errors);
        }

        EventMetadata metadata = event.metadata();

        if (!EventTypes.EXPLANATION_PREPARED.equals(metadata.eventType())) {
            errors.add("eventType must be " + EventTypes.EXPLANATION_PREPARED);
        }
        if (metadata.eventVersion() != SUPPORTED_EVENT_VERSION) {
            errors.add("eventVersion must be " + SUPPORTED_EVENT_VERSION);
        }

        if (!errors.isEmpty()) {
            throw new InvalidExplanationPreparedEventException(errors);
        }
    }
}
