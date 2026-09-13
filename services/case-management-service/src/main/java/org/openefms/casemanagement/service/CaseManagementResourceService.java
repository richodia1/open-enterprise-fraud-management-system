package org.openefms.casemanagement.service;

import java.time.Clock;
import java.util.List;
import org.openefms.casemanagement.enums.CaseManagementResourceType;
import org.openefms.common.models.management.ManagedResourceRecord;
import org.openefms.common.models.management.ManagedResourceRequest;
import org.openefms.common.models.management.ManagedResourceStore;
import org.openefms.common.models.management.ManagedResourceTypeDescriptor;
import org.springframework.stereotype.Service;

@Service
public class CaseManagementResourceService {
    private final ManagedResourceStore<CaseManagementResourceType> store =
            new ManagedResourceStore<>(CaseManagementResourceType.class, Clock.systemUTC());

    public List<ManagedResourceTypeDescriptor> resourceTypes() {
        return store.resourceTypes();
    }

    public List<ManagedResourceRecord> list(CaseManagementResourceType resourceType, String tenantId, String filterText, Boolean active) {
        return store.list(resourceType, tenantId, filterText, active);
    }

    public ManagedResourceRecord get(CaseManagementResourceType resourceType, String id) {
        return store.get(resourceType, id);
    }

    public ManagedResourceRecord create(CaseManagementResourceType resourceType, ManagedResourceRequest request) {
        return store.create(resourceType, request);
    }

    public ManagedResourceRecord replace(CaseManagementResourceType resourceType, String id, ManagedResourceRequest request) {
        return store.replace(resourceType, id, request);
    }

    public void delete(CaseManagementResourceType resourceType, String id) {
        store.delete(resourceType, id);
    }
}
