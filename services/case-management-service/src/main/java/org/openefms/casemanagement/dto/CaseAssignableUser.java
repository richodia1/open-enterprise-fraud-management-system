package org.openefms.casemanagement.dto;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.openefms.casemanagement.enums.CaseAssignmentPurpose;
import org.openefms.common.models.management.ManagedResourceRecord;

public record CaseAssignableUser(
        String id,
        String tenantId,
        String code,
        String displayName,
        String email,
        List<String> roles,
        Set<CaseAssignmentPurpose> eligiblePurposes,
        boolean active,
        Map<String, String> attributes
) {
    public static CaseAssignableUser from(ManagedResourceRecord resource) {
        Map<String, String> attributes = resource.attributes() == null ? Map.of() : resource.attributes();
        return new CaseAssignableUser(
                resource.id(),
                resource.tenantId(),
                resource.code(),
                resource.name(),
                firstPresent(attributes, "email", "mail", "userPrincipalName"),
                roles(attributes),
                Set.of(),
                resource.active(),
                attributes);
    }

    public CaseAssignableUser withEligiblePurposes(Set<CaseAssignmentPurpose> eligiblePurposes) {
        return new CaseAssignableUser(
                id,
                tenantId,
                code,
                displayName,
                email,
                roles,
                Set.copyOf(eligiblePurposes),
                active,
                attributes);
    }

    public boolean matches(String assigneeId) {
        String resolved = normalise(assigneeId);
        if (resolved == null) {
            return false;
        }
        return resolved.equals(normalise(id))
                || resolved.equals(normalise(code))
                || resolved.equals(normalise(email));
    }

    private static List<String> roles(Map<String, String> attributes) {
        String roles = firstPresent(attributes, "roles", "role");
        if (roles == null) {
            return List.of();
        }
        return Arrays.stream(roles.split(","))
                .map(String::trim)
                .filter(role -> !role.isBlank())
                .toList();
    }

    private static String firstPresent(Map<String, String> attributes, String... keys) {
        for (String key : keys) {
            String value = attributes.get(key);
            if (value != null && !value.trim().isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    private static String normalise(String value) {
        if (value == null || value.trim().isBlank()) {
            return null;
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
