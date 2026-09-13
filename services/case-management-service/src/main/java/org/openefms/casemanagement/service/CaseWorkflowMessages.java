package org.openefms.casemanagement.service;

import java.util.List;
import org.openefms.casemanagement.messaging.DomainEventMessage;

public record CaseWorkflowMessages(
        List<DomainEventMessage<?>> messages
) {
    public CaseWorkflowMessages {
        messages = messages == null ? List.of() : List.copyOf(messages);
    }

    static CaseWorkflowMessages empty() {
        return new CaseWorkflowMessages(List.of());
    }
}
