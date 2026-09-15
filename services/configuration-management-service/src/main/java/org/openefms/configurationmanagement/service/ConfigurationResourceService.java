package org.openefms.configurationmanagement.service;

import java.time.Clock;
import java.util.List;
import java.util.Map;
import org.openefms.common.models.management.ManagedResourceRecord;
import org.openefms.common.models.management.ManagedResourceRequest;
import org.openefms.common.models.management.ManagedResourceStore;
import org.openefms.common.models.management.ManagedResourceTypeDescriptor;
import org.openefms.configurationmanagement.enums.ConfigurationResourceType;
import org.springframework.stereotype.Service;

@Service
public class ConfigurationResourceService {
    private final ManagedResourceStore<ConfigurationResourceType> store =
            new ManagedResourceStore<>(ConfigurationResourceType.class, Clock.systemUTC());

    public ConfigurationResourceService() {
        seedCaseAssignmentPolicies();
    }

    public List<ManagedResourceTypeDescriptor> resourceTypes() {
        return store.resourceTypes();
    }

    public List<ManagedResourceRecord> list(ConfigurationResourceType resourceType, String tenantId, String filterText, Boolean active) {
        return store.list(resourceType, tenantId, filterText, active);
    }

    public ManagedResourceRecord get(ConfigurationResourceType resourceType, String id) {
        return store.get(resourceType, id);
    }

    public ManagedResourceRecord create(ConfigurationResourceType resourceType, ManagedResourceRequest request) {
        return store.create(resourceType, request);
    }

    public ManagedResourceRecord replace(ConfigurationResourceType resourceType, String id, ManagedResourceRequest request) {
        return store.replace(resourceType, id, request);
    }

    public void delete(ConfigurationResourceType resourceType, String id) {
        store.delete(resourceType, id);
    }

    private void seedCaseAssignmentPolicies() {
        store.create(
                ConfigurationResourceType.CASE_ASSIGNMENT_POLICIES,
                new ManagedResourceRequest(
                        "tenant-a",
                        "tenant-a-default-case-assignment-policy",
                        "Tenant A Default Case Assignment Policy",
                        "Demo policy that controls who can receive case assignments and escalations.",
                        true,
                        Map.of(
                                "assignmentRoles",
                                "FRAUD_ANALYST,FRAUD_SUPERVISOR",
                                "escalationRoles",
                                "FRAUD_SUPERVISOR,FRAUD_ESCALATION_REVIEWER,COMPLIANCE_OFFICER",
                                "source",
                                "configuration-demo-seed"),
                        "system"));
        store.create(
                ConfigurationResourceType.CASE_ESCALATION_POLICIES,
                new ManagedResourceRequest(
                        "tenant-a",
                        "tenant-a-default-case-escalation-policy",
                        "Tenant A Default Case Escalation Policy",
                        "Demo policy kept visible for UI configuration screens that manage escalation separately.",
                        true,
                        Map.of(
                                "eligibleRoles",
                                "FRAUD_SUPERVISOR,FRAUD_ESCALATION_REVIEWER,COMPLIANCE_OFFICER",
                                "source",
                                "configuration-demo-seed"),
                        "system"));
    }
}
