package org.openefms.administration.service;

import java.time.Clock;
import java.util.List;
import org.openefms.administration.enums.AdministrationResourceType;
import org.openefms.common.models.management.ManagedResourceRecord;
import org.openefms.common.models.management.ManagedResourceRequest;
import org.openefms.common.models.management.ManagedResourceStore;
import org.openefms.common.models.management.ManagedResourceTypeDescriptor;
import org.springframework.stereotype.Service;

@Service
public class AdministrationResourceService {
    private final ManagedResourceStore<AdministrationResourceType> store =
            new ManagedResourceStore<>(AdministrationResourceType.class, Clock.systemUTC());

    public List<ManagedResourceTypeDescriptor> resourceTypes() {
        return store.resourceTypes();
    }

    public List<ManagedResourceRecord> list(AdministrationResourceType resourceType, String tenantId, String filterText, Boolean active) {
        return store.list(resourceType, tenantId, filterText, active);
    }

    public ManagedResourceRecord get(AdministrationResourceType resourceType, String id) {
        return store.get(resourceType, id);
    }

    public ManagedResourceRecord create(AdministrationResourceType resourceType, ManagedResourceRequest request) {
        return store.create(resourceType, request);
    }

    public ManagedResourceRecord replace(AdministrationResourceType resourceType, String id, ManagedResourceRequest request) {
        return store.replace(resourceType, id, request);
    }

    public void delete(AdministrationResourceType resourceType, String id) {
        store.delete(resourceType, id);
    }
}
