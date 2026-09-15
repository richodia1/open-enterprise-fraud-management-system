package org.openefms.casemanagement.service;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.openefms.casemanagement.dto.CaseAssignableUser;
import org.openefms.casemanagement.enums.CaseAssignmentPurpose;
import org.openefms.common.models.management.ManagedResourceRecord;

public record CaseAssignmentPolicy(
        Set<String> assignmentRoles,
        Set<String> escalationRoles,
        Map<String, String> attributes
) {
    public static CaseAssignmentPolicy from(ManagedResourceRecord resource, CaseAssignmentPolicy fallback) {
        Map<String, String> attributes = resource.attributes() == null ? Map.of() : resource.attributes();
        Set<String> assignmentRoles = roles(attributes.get("assignmentRoles"));
        Set<String> escalationRoles = roles(attributes.get("escalationRoles"));
        if (assignmentRoles.isEmpty() && escalationRoles.isEmpty()) {
            return fallback;
        }
        return new CaseAssignmentPolicy(
                assignmentRoles.isEmpty() ? fallback.assignmentRoles() : assignmentRoles,
                escalationRoles.isEmpty() ? fallback.escalationRoles() : escalationRoles,
                attributes);
    }

    public boolean allows(CaseAssignableUser user, CaseAssignmentPurpose purpose) {
        Set<String> allowedRoles = purpose == CaseAssignmentPurpose.ESCALATION ? escalationRoles : assignmentRoles;
        if (allowedRoles.isEmpty()) {
            return true;
        }
        return user.roles().stream()
                .map(CaseAssignmentPolicy::normalise)
                .anyMatch(allowedRoles::contains);
    }

    public Set<CaseAssignmentPurpose> eligiblePurposes(CaseAssignableUser user) {
        return Arrays.stream(CaseAssignmentPurpose.values())
                .filter(purpose -> allows(user, purpose))
                .collect(Collectors.toUnmodifiableSet());
    }

    public static Set<String> roles(String csv) {
        if (csv == null || csv.trim().isBlank()) {
            return Set.of();
        }
        return Arrays.stream(csv.split(","))
                .map(CaseAssignmentPolicy::normalise)
                .filter(role -> !role.isBlank())
                .collect(Collectors.toUnmodifiableSet());
    }

    private static String normalise(String role) {
        return role == null ? "" : role.trim().toUpperCase(Locale.ROOT);
    }
}
