package org.openefms.identity.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.openefms.common.models.management.ManagedResourceConflictException;
import org.openefms.identity.dto.TenantIdentityProviderRequest;
import org.openefms.identity.entity.TenantIdentityProvider;
import org.openefms.identity.enums.TenantIdentityProviderStatus;
import org.openefms.identity.enums.TenantIdentityProviderType;
import org.openefms.identity.repository.InMemoryTenantIdentityProviderRepository;

class TenantIdentityProviderServiceTest {
    @Test
    void exposesSupportedTenantIdentityProviderTypes() {
        TenantIdentityProviderService service = service();

        assertTrue(service.providerTypes().stream()
                .anyMatch(type -> type.providerType().equals(TenantIdentityProviderType.MICROSOFT_ENTRA_ID.name())));
        assertTrue(service.providerTypes().stream()
                .anyMatch(type -> type.providerType().equals(TenantIdentityProviderType.ACTIVE_DIRECTORY_LDAP.name())));
    }

    @Test
    void seedsMicrosoftEntraIdAsDraftProvider() {
        TenantIdentityProviderService service = service();

        TenantIdentityProvider provider = service.list("global", TenantIdentityProviderType.MICROSOFT_ENTRA_ID, false, "microsoft")
                .getFirst();

        assertEquals("microsoft-entra-id", provider.providerCode());
        assertEquals(TenantIdentityProviderStatus.DRAFT, provider.status());
        assertFalse(provider.enabled());
        assertTrue(provider.metadataUrl().contains("login.microsoftonline.com"));
    }

    @Test
    void createsTenantIdentityProviderAndRejectsDuplicateCodes() {
        TenantIdentityProviderService service = service();

        TenantIdentityProvider created = service.create(new TenantIdentityProviderRequest(
                "tenant-a",
                "okta",
                "Okta Workforce",
                TenantIdentityProviderType.OKTA,
                TenantIdentityProviderStatus.ACTIVE,
                true,
                "client-id",
                "vault://identity/tenant-a/okta/client-secret",
                "https://example.okta.com/oauth2/default",
                "https://example.okta.com/oauth2/default/.well-known/openid-configuration",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                Map.of("email", "email"),
                Map.of("FraudAnalyst", "OEFMS_FRAUD_ANALYST"),
                Map.of("protocol", "oidc"),
                "admin"));

        assertEquals("tenant-a", created.tenantId());
        assertEquals(TenantIdentityProviderType.OKTA, created.providerType());
        assertTrue(created.enabled());

        assertThrows(
                ManagedResourceConflictException.class,
                () -> service.create(new TenantIdentityProviderRequest(
                        "tenant-a",
                        "OKTA",
                        "Duplicate Okta",
                        TenantIdentityProviderType.OKTA,
                        TenantIdentityProviderStatus.DRAFT,
                        false,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        Map.of(),
                        Map.of(),
                        Map.of(),
                        "admin")));
    }

    private TenantIdentityProviderService service() {
        return new TenantIdentityProviderService(new InMemoryTenantIdentityProviderRepository());
    }
}
