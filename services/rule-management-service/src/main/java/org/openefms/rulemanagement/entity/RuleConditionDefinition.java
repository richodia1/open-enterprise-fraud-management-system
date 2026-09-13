package org.openefms.rulemanagement.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

public record RuleConditionDefinition(
        String id,
        String tenantId,
        UUID ruleId,
        String ruleCode,
        String operator,
        String datapointValue,
        String groupId,
        String groupConnector,
        String sourceDataPointId,
        String listTypeDataId,
        String sourceDataPointCode,
        String destinationDataPointCode,
        String datapointDataType,
        String destinationDataType,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        String updatedBy
) {
}
