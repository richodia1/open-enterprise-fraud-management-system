package org.openefms.common.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class TenantScopedAttributesEntity extends TenantScopedEntity {
    @Column(name = "attributes_text", nullable = false, columnDefinition = "text")
    private String attributesText;

    protected TenantScopedAttributesEntity() {
    }

    public String attributesText() {
        return attributesText;
    }

    public void attributesText(String attributesText) {
        this.attributesText = attributesText;
    }
}
