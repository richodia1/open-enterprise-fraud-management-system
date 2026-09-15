package org.openefms.casemanagement.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import org.openefms.casemanagement.service.CaseAssignmentPolicy;
import org.openefms.casemanagement.service.CaseAssignmentPolicyProvider;
import org.openefms.common.models.management.ManagedResourceRecord;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class ConfigurationCaseAssignmentPolicyProvider implements CaseAssignmentPolicyProvider {
    private final CaseConfigurationClientProperties properties;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public ConfigurationCaseAssignmentPolicyProvider(CaseConfigurationClientProperties properties) {
        this.properties = properties;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper().findAndRegisterModules();
    }

    @Override
    public CaseAssignmentPolicy policy(String tenantId) {
        CaseAssignmentPolicy fallback = fallbackPolicy();
        if (!properties.enabled()) {
            return fallback;
        }

        try {
            return firstPolicy(tenantId, fallback);
        } catch (CaseAssignmentDirectoryUnavailableException exception) {
            if (properties.requirePolicy()) {
                throw exception;
            }
            return fallback;
        }
    }

    private CaseAssignmentPolicy firstPolicy(String tenantId, CaseAssignmentPolicy fallback) {
        List<ManagedResourceRecord> tenantPolicies = readPolicies(tenantId);
        if (!tenantPolicies.isEmpty()) {
            return CaseAssignmentPolicy.from(tenantPolicies.getFirst(), fallback);
        }

        List<ManagedResourceRecord> globalPolicies = readPolicies("global");
        if (!globalPolicies.isEmpty()) {
            return CaseAssignmentPolicy.from(globalPolicies.getFirst(), fallback);
        }

        return fallback;
    }

    private List<ManagedResourceRecord> readPolicies(String tenantId) {
        HttpRequest request = HttpRequest.newBuilder(policyUri(tenantId))
                .timeout(properties.requestTimeout())
                .header("Accept", "application/json")
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new CaseAssignmentDirectoryUnavailableException(
                        "Configuration assignment policy returned HTTP " + response.statusCode());
            }
            return Arrays.stream(objectMapper.readValue(response.body(), ManagedResourceRecord[].class))
                    .filter(ManagedResourceRecord::active)
                    .sorted(Comparator.comparing(ManagedResourceRecord::code))
                    .toList();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new CaseAssignmentDirectoryUnavailableException(
                    "Configuration assignment policy request was interrupted.",
                    exception);
        } catch (JsonProcessingException exception) {
            throw new CaseAssignmentDirectoryUnavailableException(
                    "Configuration assignment policy response could not be parsed.",
                    exception);
        } catch (IOException exception) {
            throw new CaseAssignmentDirectoryUnavailableException(
                    "Configuration assignment policy is unavailable.",
                    exception);
        }
    }

    private URI policyUri(String tenantId) {
        return UriComponentsBuilder
                .fromUriString(properties.baseUrl())
                .pathSegment(
                        "api",
                        "v1",
                        "configuration-management",
                        "resources",
                        properties.assignmentPolicyResourceType())
                .queryParam("tenantId", tenantId)
                .queryParam("active", true)
                .build()
                .encode()
                .toUri();
    }

    private CaseAssignmentPolicy fallbackPolicy() {
        return new CaseAssignmentPolicy(
                CaseAssignmentPolicy.roles(properties.fallbackAssignmentRoles()),
                CaseAssignmentPolicy.roles(properties.fallbackEscalationRoles()),
                java.util.Map.of("source", "case-management-fallback"));
    }
}
