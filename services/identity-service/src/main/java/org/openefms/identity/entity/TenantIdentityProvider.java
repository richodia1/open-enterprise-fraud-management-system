package org.openefms.identity.entity;

import java.time.OffsetDateTime;
import java.util.Map;
import org.openefms.identity.enums.TenantIdentityProviderStatus;
import org.openefms.identity.enums.TenantIdentityProviderType;

public record TenantIdentityProvider(
        String id,
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
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        String updatedBy
) {
}
