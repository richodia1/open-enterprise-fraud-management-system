package org.openefms.identity.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.openefms.identity.enums.IdentityResourceType;

class IdentityResourceServiceTest {
    @Test
    void seedsActiveTenantUsersForCaseAssignment() {
        IdentityResourceService service = new IdentityResourceService();

        var users = service.list(IdentityResourceType.CUSTOM_USERS, "tenant-a", null, true);

        assertEquals(3, users.size());
        assertTrue(users.stream().anyMatch(user -> user.code().equals("fraud-analyst-1")));
        assertTrue(users.stream().anyMatch(user -> user.attributes().get("roles").contains("FRAUD_SUPERVISOR")));
    }
}
