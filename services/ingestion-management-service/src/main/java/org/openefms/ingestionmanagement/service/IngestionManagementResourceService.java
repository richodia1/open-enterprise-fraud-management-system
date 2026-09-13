package org.openefms.ingestionmanagement.service;

import java.time.Clock;
import java.util.List;
import org.openefms.common.models.management.ManagedResourceRecord;
import org.openefms.common.models.management.ManagedResourceRequest;
import org.openefms.common.models.management.ManagedResourceStore;
import org.openefms.common.models.management.ManagedResourceTypeDescriptor;
import org.openefms.ingestionmanagement.enums.IngestionManagementResourceType;
import org.springframework.stereotype.Service;

@Service
public class IngestionManagementResourceService {
    private final ManagedResourceStore<IngestionManagementResourceType> store =
            new ManagedResourceStore<>(IngestionManagementResourceType.class, Clock.systemUTC());

    public List<ManagedResourceTypeDescriptor> resourceTypes() {
        return store.resourceTypes();
    }

    public List<ManagedResourceRecord> list(IngestionManagementResourceType resourceType, String tenantId, String filterText, Boolean active) {
        return store.list(resourceType, tenantId, filterText, active);
    }

    public ManagedResourceRecord get(IngestionManagementResourceType resourceType, String id) {
        return store.get(resourceType, id);
    }

    public ManagedResourceRecord create(IngestionManagementResourceType resourceType, ManagedResourceRequest request) {
        return store.create(resourceType, request);
    }

    public ManagedResourceRecord replace(IngestionManagementResourceType resourceType, String id, ManagedResourceRequest request) {
        return store.replace(resourceType, id, request);
    }

    public void delete(IngestionManagementResourceType resourceType, String id) {
        store.delete(resourceType, id);
    }
}
