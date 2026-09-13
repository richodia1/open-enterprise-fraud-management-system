package org.openefms.rulemanagement.dto;

import java.util.UUID;

public record RuleConditionDefinitionRequest(
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
        String updatedBy
) {
}
