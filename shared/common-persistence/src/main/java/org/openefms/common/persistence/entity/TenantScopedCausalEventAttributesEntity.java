package org.openefms.common.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class TenantScopedCausalEventAttributesEntity extends TenantScopedEventAttributesEntity {
    @Column(name = "causation_id", length = 128)
    private String causationId;

    protected TenantScopedCausalEventAttributesEntity() {
    }

    public String causationId() {
        return causationId;
    }

    public void causationId(String causationId) {
        this.causationId = causationId;
    }
}
