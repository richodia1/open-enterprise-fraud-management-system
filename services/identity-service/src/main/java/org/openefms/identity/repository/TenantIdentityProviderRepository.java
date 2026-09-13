package org.openefms.identity.repository;

import java.util.List;
import java.util.Optional;
import org.openefms.identity.entity.TenantIdentityProvider;

public interface TenantIdentityProviderRepository {
    List<TenantIdentityProvider> findAll();

    Optional<TenantIdentityProvider> findById(String id);

    Optional<TenantIdentityProvider> findByTenantIdAndProviderCode(String tenantId, String providerCode);

    TenantIdentityProvider save(TenantIdentityProvider provider);

    void delete(String id);
}
