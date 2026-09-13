package org.openefms.notificationmanagement.service;

import java.time.Clock;
import java.util.List;
import org.openefms.common.models.management.ManagedResourceRecord;
import org.openefms.common.models.management.ManagedResourceRequest;
import org.openefms.common.models.management.ManagedResourceStore;
import org.openefms.common.models.management.ManagedResourceTypeDescriptor;
import org.openefms.notificationmanagement.enums.NotificationManagementResourceType;
import org.springframework.stereotype.Service;

@Service
public class NotificationManagementResourceService {
    private final ManagedResourceStore<NotificationManagementResourceType> store =
            new ManagedResourceStore<>(NotificationManagementResourceType.class, Clock.systemUTC());

    public List<ManagedResourceTypeDescriptor> resourceTypes() {
        return store.resourceTypes();
    }

    public List<ManagedResourceRecord> list(NotificationManagementResourceType resourceType, String tenantId, String filterText, Boolean active) {
        return store.list(resourceType, tenantId, filterText, active);
    }

    public ManagedResourceRecord get(NotificationManagementResourceType resourceType, String id) {
        return store.get(resourceType, id);
    }

    public ManagedResourceRecord create(NotificationManagementResourceType resourceType, ManagedResourceRequest request) {
        return store.create(resourceType, request);
    }

    public ManagedResourceRecord replace(NotificationManagementResourceType resourceType, String id, ManagedResourceRequest request) {
        return store.replace(resourceType, id, request);
    }

    public void delete(NotificationManagementResourceType resourceType, String id) {
        store.delete(resourceType, id);
    }
}
