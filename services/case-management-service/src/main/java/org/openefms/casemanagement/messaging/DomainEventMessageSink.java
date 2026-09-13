package org.openefms.casemanagement.messaging;

public interface DomainEventMessageSink {
    public void accept(DomainEventMessage<?> message);
}
