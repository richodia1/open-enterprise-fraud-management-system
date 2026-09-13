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
import org.openefms.casemanagement.dto.CaseAssignableUser;
import org.openefms.casemanagement.service.CaseAssignmentDirectory;
import org.openefms.common.models.management.ManagedResourceRecord;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class IdentityCaseAssignmentDirectory implements CaseAssignmentDirectory {
    private final CaseIdentityClientProperties properties;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public IdentityCaseAssignmentDirectory(CaseIdentityClientProperties properties) {
        this.properties = properties;
        this.objectMapper = new ObjectMapper().findAndRegisterModules();
        this.httpClient = HttpClient.newHttpClient();
    }

    @Override
    public List<CaseAssignableUser> listAssignableUsers(String tenantId, String filterText) {
        URI uri = usersUri(tenantId, filterText);
        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(properties.requestTimeout())
                .header("Accept", "application/json")
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new CaseAssignmentDirectoryUnavailableException(
                        "Identity user directory returned HTTP " + response.statusCode());
            }
            return Arrays.stream(objectMapper.readValue(response.body(), ManagedResourceRecord[].class))
                    .map(CaseAssignableUser::from)
                    .filter(CaseAssignableUser::active)
                    .sorted(Comparator
                            .comparing(CaseAssignableUser::displayName)
                            .thenComparing(CaseAssignableUser::id))
                    .toList();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new CaseAssignmentDirectoryUnavailableException("Identity user directory request was interrupted.", exception);
        } catch (JsonProcessingException exception) {
            throw new CaseAssignmentDirectoryUnavailableException("Identity user directory response could not be parsed.", exception);
        } catch (IOException exception) {
            throw new CaseAssignmentDirectoryUnavailableException("Identity user directory is unavailable.", exception);
        }
    }

    @Override
    public void assertAssignable(String tenantId, String assigneeId) {
        if (!properties.validateAssignees()) {
            return;
        }
        CaseAssignmentDirectory.super.assertAssignable(tenantId, assigneeId);
    }

    private URI usersUri(String tenantId, String filterText) {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(properties.baseUrl())
                .pathSegment("api", "v1", "identity", "resources", properties.userResourceType())
                .queryParam("tenantId", tenantId)
                .queryParam("active", true);
        if (filterText != null && !filterText.trim().isBlank()) {
            builder.queryParam("filterText", filterText.trim());
        }
        return builder.build().encode().toUri();
    }
}
