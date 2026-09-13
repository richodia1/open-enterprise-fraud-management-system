package org.openefms.casemanagement.service;

import java.util.List;
import org.openefms.casemanagement.dto.CaseAssignableUser;
import org.openefms.casemanagement.enums.CaseAssignmentPurpose;

public interface CaseAssignmentDirectory {
    public List<CaseAssignableUser> listAssignableUsers(String tenantId, String filterText);

    public default List<CaseAssignableUser> listAssignableUsers(
            String tenantId,
            String filterText,
            CaseAssignmentPurpose purpose) {
        return listAssignableUsers(tenantId, filterText);
    }

    public default void assertAssignable(String tenantId, String assigneeId) {
        assertAssignable(tenantId, assigneeId, CaseAssignmentPurpose.ASSIGNMENT);
    }

    public default void assertAssignable(String tenantId, String assigneeId, CaseAssignmentPurpose purpose) {
        boolean found = listAssignableUsers(tenantId, null, purpose).stream()
                .anyMatch(user -> user.matches(assigneeId));
        if (!found) {
            throw new InvalidCaseLifecycleActionException(List.of(
                    "assigneeId must reference an active Identity user for tenant " + tenantId));
        }
    }
}
