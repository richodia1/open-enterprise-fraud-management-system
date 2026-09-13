package org.openefms.identity.service;

import java.time.Clock;
import java.util.List;
import java.util.Map;
import org.openefms.common.models.management.ManagedResourceRecord;
import org.openefms.common.models.management.ManagedResourceRequest;
import org.openefms.common.models.management.ManagedResourceStore;
import org.openefms.common.models.management.ManagedResourceTypeDescriptor;
import org.openefms.identity.enums.IdentityResourceType;
import org.springframework.stereotype.Service;

@Service
public class IdentityResourceService {
    private final ManagedResourceStore<IdentityResourceType> store =
            new ManagedResourceStore<>(IdentityResourceType.class, Clock.systemUTC());

    public IdentityResourceService() {
        seedCaseAssignmentUsers();
    }

    public List<ManagedResourceTypeDescriptor> resourceTypes() {
        return store.resourceTypes();
    }

    public List<ManagedResourceRecord> list(IdentityResourceType resourceType, String tenantId, String filterText, Boolean active) {
        return store.list(resourceType, tenantId, filterText, active);
    }

    public ManagedResourceRecord get(IdentityResourceType resourceType, String id) {
        return store.get(resourceType, id);
    }

    public ManagedResourceRecord create(IdentityResourceType resourceType, ManagedResourceRequest request) {
        return store.create(resourceType, request);
    }

    public ManagedResourceRecord replace(IdentityResourceType resourceType, String id, ManagedResourceRequest request) {
        return store.replace(resourceType, id, request);
    }

    public void delete(IdentityResourceType resourceType, String id) {
        store.delete(resourceType, id);
    }

    private void seedCaseAssignmentUsers() {
        seedUser(
                "tenant-a",
                "fraud-analyst-1",
                "Fraud Analyst One",
                "fraud.analyst1@example.com",
                "FRAUD_ANALYST");
        seedUser(
                "tenant-a",
                "fraud-supervisor-1",
                "Fraud Supervisor One",
                "fraud.supervisor1@example.com",
                "FRAUD_ANALYST,FRAUD_SUPERVISOR");
        seedUser(
                "tenant-a",
                "compliance-officer-1",
                "Compliance Officer One",
                "compliance.officer1@example.com",
                "COMPLIANCE_OFFICER,FRAUD_ESCALATION_REVIEWER");
    }

    private void seedUser(String tenantId, String code, String name, String email, String roles) {
        store.create(
                IdentityResourceType.CUSTOM_USERS,
                new ManagedResourceRequest(
                        tenantId,
                        code,
                        name,
                        "Demo tenant user for case assignment and escalation.",
                        true,
                        Map.of("email", email, "roles", roles, "source", "identity-demo-seed"),
                        "system"));
    }
}
