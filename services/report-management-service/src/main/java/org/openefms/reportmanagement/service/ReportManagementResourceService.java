package org.openefms.reportmanagement.service;

import java.time.Clock;
import java.util.List;
import org.openefms.common.models.management.ManagedResourceRecord;
import org.openefms.common.models.management.ManagedResourceRequest;
import org.openefms.common.models.management.ManagedResourceStore;
import org.openefms.common.models.management.ManagedResourceTypeDescriptor;
import org.openefms.reportmanagement.enums.ReportManagementResourceType;
import org.springframework.stereotype.Service;

@Service
public class ReportManagementResourceService {
    private final ManagedResourceStore<ReportManagementResourceType> store =
            new ManagedResourceStore<>(ReportManagementResourceType.class, Clock.systemUTC());

    public List<ManagedResourceTypeDescriptor> resourceTypes() {
        return store.resourceTypes();
    }

    public List<ManagedResourceRecord> list(ReportManagementResourceType resourceType, String tenantId, String filterText, Boolean active) {
        return store.list(resourceType, tenantId, filterText, active);
    }

    public ManagedResourceRecord get(ReportManagementResourceType resourceType, String id) {
        return store.get(resourceType, id);
    }

    public ManagedResourceRecord create(ReportManagementResourceType resourceType, ManagedResourceRequest request) {
        return store.create(resourceType, request);
    }

    public ManagedResourceRecord replace(ReportManagementResourceType resourceType, String id, ManagedResourceRequest request) {
        return store.replace(resourceType, id, request);
    }

    public void delete(ReportManagementResourceType resourceType, String id) {
        store.delete(resourceType, id);
    }
}
