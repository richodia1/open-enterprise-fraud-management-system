package org.openefms.common.models.management;

import java.util.Map;

public record ManagedResourceRequest(
        String tenantId,
        String code,
        String name,
        String description,
        Boolean active,
        Map<String, String> attributes,
        String updatedBy
) {
}
