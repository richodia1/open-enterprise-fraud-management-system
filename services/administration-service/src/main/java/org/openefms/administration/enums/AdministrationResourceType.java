package org.openefms.administration.enums;

import org.openefms.common.models.management.ManagedResourceKind;

public enum AdministrationResourceType implements ManagedResourceKind {
    PERMISSION_DEFINITIONS(
            "Permission Definitions",
            "administration:PermissionDefinitions",
            "Permission catalog entries used to protect service features."),
    PERMISSION_GRANTS(
            "Permission Grants",
            "administration:PermissionGrants",
            "Tenant, role, and user permission assignments."),
    ROLES(
            "Roles",
            "administration:Roles",
            "Operational roles for fraud analysts, supervisors, administrators, and auditors."),
    ROLE_GROUPS(
            "Role Groups",
            "administration:RoleGroups",
            "Role grouping metadata used by enterprise deployments.");

    private final String displayName;
    private final String legacyResource;
    private final String notes;

    AdministrationResourceType(String displayName, String legacyResource, String notes) {
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
