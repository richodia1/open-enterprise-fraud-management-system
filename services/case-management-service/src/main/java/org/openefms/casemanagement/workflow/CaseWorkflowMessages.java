package org.openefms.casemanagement.workflow;

import java.util.List;

record CaseWorkflowMessages(
        List<DomainEventMessage<?>> messages
) {
    CaseWorkflowMessages {
        messages = messages == null ? List.of() : List.copyOf(messages);
    }

    static CaseWorkflowMessages empty() {
        return new CaseWorkflowMessages(List.of());
    }
}
