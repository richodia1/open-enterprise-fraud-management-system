package org.openefms.listmanagement.service;

import java.time.Clock;
import java.util.List;
import org.openefms.common.models.management.ManagedResourceRecord;
import org.openefms.common.models.management.ManagedResourceRequest;
import org.openefms.common.models.management.ManagedResourceStore;
import org.openefms.common.models.management.ManagedResourceTypeDescriptor;
import org.openefms.listmanagement.enums.ListManagementResourceType;
import org.springframework.stereotype.Service;

@Service
public class ListManagementResourceService {
    private final ManagedResourceStore<ListManagementResourceType> store =
            new ManagedResourceStore<>(ListManagementResourceType.class, Clock.systemUTC());

    public List<ManagedResourceTypeDescriptor> resourceTypes() {
        return store.resourceTypes();
    }

    public List<ManagedResourceRecord> list(ListManagementResourceType resourceType, String tenantId, String filterText, Boolean active) {
        return store.list(resourceType, tenantId, filterText, active);
    }

    public ManagedResourceRecord get(ListManagementResourceType resourceType, String id) {
        return store.get(resourceType, id);
    }

    public ManagedResourceRecord create(ListManagementResourceType resourceType, ManagedResourceRequest request) {
        return store.create(resourceType, request);
    }

    public ManagedResourceRecord replace(ListManagementResourceType resourceType, String id, ManagedResourceRequest request) {
        return store.replace(resourceType, id, request);
    }

    public void delete(ListManagementResourceType resourceType, String id) {
        store.delete(resourceType, id);
    }
}
