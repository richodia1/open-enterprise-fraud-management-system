package org.openefms.casemanagement.workflow;

public interface DomainEventMessageSink {
    void accept(DomainEventMessage<?> message);
}
