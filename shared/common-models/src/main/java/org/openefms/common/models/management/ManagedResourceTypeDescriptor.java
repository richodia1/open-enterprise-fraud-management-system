package org.openefms.common.models.management;

public record ManagedResourceTypeDescriptor(
        String resourceType,
        String displayName,
        String legacyResource,
        String notes
) {
}
