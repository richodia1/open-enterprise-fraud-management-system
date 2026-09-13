package org.openefms.identity.enums;

public enum TenantIdentityProviderType {
    MICROSOFT_ENTRA_ID("Microsoft Entra ID / Azure AD", "OIDC and OAuth2 integration for Microsoft cloud identity."),
    ACTIVE_DIRECTORY_LDAP("Microsoft Active Directory LDAP", "LDAP integration for on-premises Microsoft Active Directory."),
    OKTA("Okta", "OIDC or SAML integration for Okta tenants."),
    PING_IDENTITY("Ping Identity", "OIDC or SAML integration for Ping Identity tenants."),
    KEYCLOAK("Keycloak", "OIDC integration for Keycloak realms."),
    AUTH0("Auth0", "OIDC integration for Auth0 tenants."),
    GENERIC_OIDC("Generic OIDC", "Standards-based OpenID Connect provider integration."),
    GENERIC_SAML("Generic SAML", "Standards-based SAML provider integration."),
    GENERIC_LDAP("Generic LDAP", "Standards-based LDAP directory integration.");

    private final String displayName;
    private final String notes;

    TenantIdentityProviderType(String displayName, String notes) {
        this.displayName = displayName;
        this.notes = notes;
    }

    public String displayName() {
        return displayName;
    }

    public String notes() {
        return notes;
    }
}
