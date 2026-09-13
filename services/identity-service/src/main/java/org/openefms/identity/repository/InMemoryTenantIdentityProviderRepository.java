package org.openefms.identity.repository;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.openefms.common.models.management.ManagedResourceStore;
import org.openefms.identity.entity.TenantIdentityProvider;
import org.openefms.identity.enums.TenantIdentityProviderStatus;
import org.openefms.identity.enums.TenantIdentityProviderType;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryTenantIdentityProviderRepository implements TenantIdentityProviderRepository {
    private final ConcurrentMap<String, TenantIdentityProvider> providers = new ConcurrentHashMap<>();

    public InMemoryTenantIdentityProviderRepository() {
        OffsetDateTime now = OffsetDateTime.now(Clock.systemUTC());
        String id = UUID.nameUUIDFromBytes("global:microsoft-entra-id".getBytes(StandardCharsets.UTF_8)).toString();
        save(new TenantIdentityProvider(
                id,
                ManagedResourceStore.GLOBAL_TENANT_ID,
                "microsoft-entra-id",
                "Microsoft Entra ID",
                TenantIdentityProviderType.MICROSOFT_ENTRA_ID,
                TenantIdentityProviderStatus.DRAFT,
                false,
                "replace-with-client-id",
                "vault://identity/global/microsoft-entra-id/client-secret",
                "https://login.microsoftonline.com/{tenant-id}/v2.0",
                "https://login.microsoftonline.com/{tenant-id}/v2.0/.well-known/openid-configuration",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                Map.of("email", "email", "displayName", "name", "externalUserId", "oid"),
                Map.of("fraud-analyst", "OEFMS_FRAUD_ANALYST", "fraud-supervisor", "OEFMS_FRAUD_SUPERVISOR"),
                Map.of("scopes", "openid profile email", "protocol", "oidc"),
                now,
                now,
                "system"));
    }

    @Override
    public List<TenantIdentityProvider> findAll() {
        return providers.values().stream()
                .sorted(Comparator
                        .comparing(TenantIdentityProvider::tenantId)
                        .thenComparing(TenantIdentityProvider::providerCode)
                        .thenComparing(TenantIdentityProvider::id))
                .toList();
    }

    @Override
    public Optional<TenantIdentityProvider> findById(String id) {
        return Optional.ofNullable(providers.get(normaliseId(id)));
    }

    @Override
    public Optional<TenantIdentityProvider> findByTenantIdAndProviderCode(String tenantId, String providerCode) {
        String resolvedTenantId = normaliseId(tenantId);
        String resolvedProviderCode = normaliseCode(providerCode);
        return providers.values().stream()
                .filter(provider -> provider.tenantId().equals(resolvedTenantId))
                .filter(provider -> provider.providerCode().equalsIgnoreCase(resolvedProviderCode))
                .findFirst();
    }

    @Override
    public TenantIdentityProvider save(TenantIdentityProvider provider) {
        providers.put(normaliseId(provider.id()), provider);
        return provider;
    }

    @Override
    public void delete(String id) {
        providers.remove(normaliseId(id));
    }

    private String normaliseId(String id) {
        return id == null ? "" : id.trim();
    }

    private String normaliseCode(String code) {
        return normaliseId(code).toLowerCase(Locale.ROOT);
    }
}
