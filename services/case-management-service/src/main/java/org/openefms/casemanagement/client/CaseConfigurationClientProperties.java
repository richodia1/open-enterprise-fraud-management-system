package org.openefms.casemanagement.client;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "oefms.case-management.configuration")
public record CaseConfigurationClientProperties(
        String baseUrl,
        String assignmentPolicyResourceType,
        Duration requestTimeout,
        boolean enabled,
        boolean requirePolicy,
        String fallbackAssignmentRoles,
        String fallbackEscalationRoles
) {
    public CaseConfigurationClientProperties {
        if (baseUrl == null || baseUrl.trim().isBlank()) {
            baseUrl = "http://localhost:8088";
        }
        baseUrl = baseUrl.replaceAll("/+$", "");
        if (assignmentPolicyResourceType == null || assignmentPolicyResourceType.trim().isBlank()) {
            assignmentPolicyResourceType = "CASE_ASSIGNMENT_POLICIES";
        }
        if (requestTimeout == null || requestTimeout.isNegative() || requestTimeout.isZero()) {
            requestTimeout = Duration.ofSeconds(5);
        }
        if (fallbackAssignmentRoles == null || fallbackAssignmentRoles.trim().isBlank()) {
            fallbackAssignmentRoles = "FRAUD_ANALYST,FRAUD_SUPERVISOR";
        }
        if (fallbackEscalationRoles == null || fallbackEscalationRoles.trim().isBlank()) {
            fallbackEscalationRoles = "FRAUD_SUPERVISOR,FRAUD_ESCALATION_REVIEWER,COMPLIANCE_OFFICER";
        }
    }
}
