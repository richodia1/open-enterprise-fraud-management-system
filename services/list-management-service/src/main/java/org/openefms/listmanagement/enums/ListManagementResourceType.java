package org.openefms.listmanagement.enums;

import org.openefms.common.models.management.ManagedResourceKind;

public enum ListManagementResourceType implements ManagedResourceKind {
    APPROVALS(
            "Approvals",
            "list-management:Approvals",
            "Approval records for list changes."),
    LIST_ITEMS(
            "List Items",
            "list-management:ListItems",
            "Tenant watchlist, allowlist, denylist, and reference-list entries."),
    LIST_TYPES(
            "List Types",
            "list-management:ListTypes",
            "List categories and classification values.");

    private final String displayName;
    private final String legacyResource;
    private final String notes;

    ListManagementResourceType(String displayName, String legacyResource, String notes) {
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
