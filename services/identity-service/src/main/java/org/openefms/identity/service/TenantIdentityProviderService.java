package org.openefms.identity.service;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.openefms.common.models.management.ManagedResourceConflictException;
import org.openefms.common.models.management.ManagedResourceNotFoundException;
import org.openefms.common.models.management.ManagedResourceStore;
import org.openefms.common.models.management.ManagedResourceValidationException;
import org.openefms.identity.dto.TenantIdentityProviderRequest;
import org.openefms.identity.dto.TenantIdentityProviderTypeDescriptor;
import org.openefms.identity.entity.TenantIdentityProvider;
import org.openefms.identity.enums.TenantIdentityProviderStatus;
import org.openefms.identity.enums.TenantIdentityProviderType;
import org.openefms.identity.repository.TenantIdentityProviderRepository;
import org.springframework.stereotype.Service;

@Service
public class TenantIdentityProviderService {
    private final TenantIdentityProviderRepository repository;
    private final Clock clock = Clock.systemUTC();

    public TenantIdentityProviderService(TenantIdentityProviderRepository repository) {
        this.repository = repository;
    }

    public List<TenantIdentityProviderTypeDescriptor> providerTypes() {
        return Arrays.stream(TenantIdentityProviderType.values())
                .map(type -> new TenantIdentityProviderTypeDescriptor(type.name(), type.displayName(), type.notes()))
                .toList();
    }

    public List<TenantIdentityProvider> list(String tenantId, TenantIdentityProviderType providerType, Boolean enabled, String filterText) {
        String resolvedTenantId = normaliseTenantId(tenantId);
        return repository.findAll().stream()
                .filter(provider -> provider.tenantId().equals(resolvedTenantId))
                .filter(provider -> providerType == null || provider.providerType() == providerType)
                .filter(provider -> enabled == null || provider.enabled() == enabled)
                .filter(provider -> matchesAny(filterText, provider.providerCode(), provider.displayName(), provider.issuerUri(), provider.metadataUrl()))
                .toList();
    }

    public TenantIdentityProvider get(String id) {
        String resolvedId = requiredText(id, "id");
        return repository.findById(resolvedId)
                .orElseThrow(() -> new ManagedResourceNotFoundException("TENANT_IDENTITY_PROVIDER", resolvedId));
    }

    public TenantIdentityProvider create(TenantIdentityProviderRequest request) {
        ValidatedProvider validated = validate(request);
        repository.findByTenantIdAndProviderCode(validated.tenantId(), validated.providerCode())
                .ifPresent(existing -> {
                    throw new ManagedResourceConflictException(
                            "Identity provider code " + existing.providerCode() + " already exists for tenant " + existing.tenantId());
                });
        OffsetDateTime now = OffsetDateTime.now(clock);
        return repository.save(toProvider(UUID.randomUUID().toString(), validated, now, now));
    }

    public TenantIdentityProvider replace(String id, TenantIdentityProviderRequest request) {
        TenantIdentityProvider existing = get(id);
        ValidatedProvider validated = validate(request);
        repository.findByTenantIdAndProviderCode(validated.tenantId(), validated.providerCode())
                .filter(provider -> !provider.id().equals(existing.id()))
                .ifPresent(duplicate -> {
                    throw new ManagedResourceConflictException(
                            "Identity provider code " + duplicate.providerCode() + " already exists for tenant " + duplicate.tenantId());
                });
        return repository.save(toProvider(existing.id(), validated, existing.createdAt(), OffsetDateTime.now(clock)));
    }

    public void delete(String id) {
        TenantIdentityProvider existing = get(id);
        repository.delete(existing.id());
    }

    private TenantIdentityProvider toProvider(
            String id,
            ValidatedProvider validated,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt) {
        return new TenantIdentityProvider(
                id,
                validated.tenantId(),
                validated.providerCode(),
                validated.displayName(),
                validated.providerType(),
                validated.status(),
                validated.enabled(),
                validated.clientId(),
                validated.clientSecretReference(),
                validated.issuerUri(),
                validated.metadataUrl(),
                validated.authorizationUri(),
                validated.tokenUri(),
                validated.jwksUri(),
                validated.ldapUrl(),
                validated.bindDn(),
                validated.baseDn(),
                validated.userSearchFilter(),
                validated.groupSearchFilter(),
                validated.claimMappings(),
                validated.roleMappings(),
                validated.settings(),
                createdAt,
                updatedAt,
                validated.updatedBy());
    }

    private ValidatedProvider validate(TenantIdentityProviderRequest request) {
        if (request == null) {
            throw new ManagedResourceValidationException(List.of("request body is required"));
        }
        List<String> errors = new ArrayList<>();
        String tenantId = normaliseTenantId(request.tenantId());
        String providerCode = normaliseCode(request.providerCode());
        String displayName = textOrNull(request.displayName());
        TenantIdentityProviderType providerType = request.providerType();
        String updatedBy = textOrNull(request.updatedBy());
        if (providerCode == null) {
            errors.add("providerCode is required");
        }
        if (displayName == null) {
            errors.add("displayName is required");
        }
        if (providerType == null) {
            errors.add("providerType is required");
        }
        if (updatedBy == null) {
            errors.add("updatedBy is required");
        }
        if (!errors.isEmpty()) {
            throw new ManagedResourceValidationException(errors);
        }
        return new ValidatedProvider(
                tenantId,
                providerCode,
                displayName,
                providerType,
                request.status() == null ? TenantIdentityProviderStatus.DRAFT : request.status(),
                request.enabled() != null && request.enabled(),
                textOrNull(request.clientId()),
                textOrNull(request.clientSecretReference()),
                textOrNull(request.issuerUri()),
                textOrNull(request.metadataUrl()),
                textOrNull(request.authorizationUri()),
                textOrNull(request.tokenUri()),
                textOrNull(request.jwksUri()),
                textOrNull(request.ldapUrl()),
                textOrNull(request.bindDn()),
                textOrNull(request.baseDn()),
                textOrNull(request.userSearchFilter()),
                textOrNull(request.groupSearchFilter()),
                immutableMap(request.claimMappings()),
                immutableMap(request.roleMappings()),
                immutableMap(request.settings()),
                updatedBy);
    }

    private boolean matchesAny(String filterText, String... values) {
        String filter = textOrNull(filterText);
        if (filter == null) {
            return true;
        }
        String normalisedFilter = filter.toLowerCase(Locale.ROOT);
        for (String value : values) {
            if (value != null && value.toLowerCase(Locale.ROOT).contains(normalisedFilter)) {
                return true;
            }
        }
        return false;
    }

    private Map<String, String> immutableMap(Map<String, String> values) {
        if (values == null || values.isEmpty()) {
            return Map.of();
        }
        Map<String, String> cleaned = new LinkedHashMap<>();
        values.forEach((key, value) -> {
            String cleanedKey = textOrNull(key);
            if (cleanedKey != null) {
                cleaned.put(cleanedKey, value == null ? "" : value);
            }
        });
        return Map.copyOf(cleaned);
    }

    private String normaliseTenantId(String tenantId) {
        String resolved = textOrNull(tenantId);
        return resolved == null ? ManagedResourceStore.GLOBAL_TENANT_ID : resolved;
    }

    private String normaliseCode(String code) {
        String resolved = textOrNull(code);
        return resolved == null ? null : resolved.toLowerCase(Locale.ROOT);
    }

    private String requiredText(String value, String fieldName) {
        String resolved = textOrNull(value);
        if (resolved == null) {
            throw new ManagedResourceValidationException(List.of(fieldName + " is required"));
        }
        return resolved;
    }

    private String textOrNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private record ValidatedProvider(
            String tenantId,
            String providerCode,
            String displayName,
            TenantIdentityProviderType providerType,
            TenantIdentityProviderStatus status,
            boolean enabled,
            String clientId,
            String clientSecretReference,
            String issuerUri,
            String metadataUrl,
            String authorizationUri,
            String tokenUri,
            String jwksUri,
            String ldapUrl,
            String bindDn,
            String baseDn,
            String userSearchFilter,
            String groupSearchFilter,
            Map<String, String> claimMappings,
            Map<String, String> roleMappings,
            Map<String, String> settings,
            String updatedBy
    ) {
    }
}
