package org.openefms.identity.dto;

public record TenantIdentityProviderTypeDescriptor(
        String providerType,
        String displayName,
        String notes
) {
}
