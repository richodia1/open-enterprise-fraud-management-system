package org.openefms.identity.enums;

import org.openefms.common.models.management.ManagedResourceKind;

public enum IdentityResourceType implements ManagedResourceKind {
    CUSTOM_USERS(
            "Custom Users",
            "identity:CustomUser",
            "Tenant-local user profile records from the legacy identity service."),
    TENANT_IDENTITY_GROUP_MAPPINGS(
            "Tenant Identity Group Mappings",
            "identity:TenantIdentityGroupMappings",
            "Mappings from external directory groups to OEFMS groups."),
    TENANT_IDENTITY_ROLE_MAPPINGS(
            "Tenant Identity Role Mappings",
            "identity:TenantIdentityRoleMappings",
            "Mappings from external directory groups or claims to OEFMS roles.");

    private final String displayName;
    private final String legacyResource;
    private final String notes;

    IdentityResourceType(String displayName, String legacyResource, String notes) {
        this.displayName = displayName;
        this.legacyResource = legacyResource;
        this.notes = notes;
    }

    @Override
    public String displayName() {
        return displayName;
    }

    @Override
    public String legacyResource() {
        return legacyResource;
    }

    @Override
    public String notes() {
        return notes;
    }
}
