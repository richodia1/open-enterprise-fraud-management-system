package org.openefms.casemanagement.service;

import java.util.List;
import org.openefms.casemanagement.dto.CaseAssignableUser;

public interface CaseAssignmentDirectory {
    public List<CaseAssignableUser> listAssignableUsers(String tenantId, String filterText);

    public default void assertAssignable(String tenantId, String assigneeId) {
        boolean found = listAssignableUsers(tenantId, null).stream()
                .anyMatch(user -> user.matches(assigneeId));
        if (!found) {
            throw new InvalidCaseLifecycleActionException(List.of(
                    "assigneeId must reference an active Identity user for tenant " + tenantId));
        }
    }
}
