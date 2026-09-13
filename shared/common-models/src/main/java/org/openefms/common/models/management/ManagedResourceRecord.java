package org.openefms.common.models.management;

import java.time.OffsetDateTime;
import java.util.Map;

public record ManagedResourceRecord(
        String id,
        String tenantId,
        String resourceType,
        String code,
        String name,
        String description,
        boolean active,
        Map<String, String> attributes,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        String updatedBy
) {
}
