package org.openefms.casemanagement.client;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "oefms.case-management.identity")
public record CaseIdentityClientProperties(
        String baseUrl,
        String userResourceType,
        Duration requestTimeout,
        boolean validateAssignees
) {
    public CaseIdentityClientProperties {
        if (baseUrl == null || baseUrl.trim().isBlank()) {
            baseUrl = "http://localhost:8087";
        }
        baseUrl = baseUrl.replaceAll("/+$", "");
        if (userResourceType == null || userResourceType.trim().isBlank()) {
            userResourceType = "CUSTOM_USERS";
        }
        if (requestTimeout == null || requestTimeout.isNegative() || requestTimeout.isZero()) {
            requestTimeout = Duration.ofSeconds(5);
        }
    }
}
