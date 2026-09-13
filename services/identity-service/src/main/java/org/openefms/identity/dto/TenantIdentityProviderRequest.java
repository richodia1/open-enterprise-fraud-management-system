package org.openefms.identity.dto;

import java.util.Map;
import org.openefms.identity.enums.TenantIdentityProviderStatus;
import org.openefms.identity.enums.TenantIdentityProviderType;

public record TenantIdentityProviderRequest(
        String tenantId,
        String providerCode,
        String displayName,
        TenantIdentityProviderType providerType,
        TenantIdentityProviderStatus status,
        Boolean enabled,
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
