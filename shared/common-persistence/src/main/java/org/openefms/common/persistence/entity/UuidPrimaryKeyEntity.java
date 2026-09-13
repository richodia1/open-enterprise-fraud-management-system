package org.openefms.common.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import java.util.UUID;

@MappedSuperclass
public abstract class UuidPrimaryKeyEntity implements IdentifiableEntity<UUID> {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    protected UuidPrimaryKeyEntity() {
    }

    public UUID id() {
        return id;
    }

    public void id(UUID id) {
        this.id = id;
    }

    @Override
    public UUID entityId() {
        return id;
    }

    @PrePersist
    protected void assignIdWhenMissing() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
