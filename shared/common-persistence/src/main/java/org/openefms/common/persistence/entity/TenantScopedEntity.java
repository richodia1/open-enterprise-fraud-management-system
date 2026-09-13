package org.openefms.common.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class TenantScopedEntity {
    @Column(name = "tenant_id", nullable = false, length = 128)
    private String tenantId;

    protected TenantScopedEntity() {
    }

    public String tenantId() {
        return tenantId;
    }

    public void tenantId(String tenantId) {
        this.tenantId = tenantId;
    }
}
