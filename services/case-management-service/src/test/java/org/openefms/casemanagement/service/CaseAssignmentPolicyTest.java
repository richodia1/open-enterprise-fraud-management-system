package org.openefms.casemanagement.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.openefms.casemanagement.dto.CaseAssignableUser;
import org.openefms.casemanagement.enums.CaseAssignmentPurpose;

class CaseAssignmentPolicyTest {
    private final CaseAssignmentPolicy policy = new CaseAssignmentPolicy(
            CaseAssignmentPolicy.roles("FRAUD_ANALYST,FRAUD_SUPERVISOR"),
            CaseAssignmentPolicy.roles("FRAUD_SUPERVISOR,COMPLIANCE_OFFICER"),
            Map.of());

    @Test
    void allowsAnalystsToReceiveAssignments() {
        CaseAssignableUser analyst = user("analyst-1", "FRAUD_ANALYST");

        assertTrue(policy.allows(analyst, CaseAssignmentPurpose.ASSIGNMENT));
        assertFalse(policy.allows(analyst, CaseAssignmentPurpose.ESCALATION));
    }

    @Test
    void allowsSupervisorsToReceiveAssignmentsAndEscalations() {
        CaseAssignableUser supervisor = user("supervisor-1", "FRAUD_SUPERVISOR");

        assertTrue(policy.allows(supervisor, CaseAssignmentPurpose.ASSIGNMENT));
        assertTrue(policy.allows(supervisor, CaseAssignmentPurpose.ESCALATION));
    }

    @Test
    void allowsComplianceOfficersToReceiveEscalationsOnly() {
        CaseAssignableUser complianceOfficer = user("compliance-1", "COMPLIANCE_OFFICER");

        assertFalse(policy.allows(complianceOfficer, CaseAssignmentPurpose.ASSIGNMENT));
        assertTrue(policy.allows(complianceOfficer, CaseAssignmentPurpose.ESCALATION));
    }

    private CaseAssignableUser user(String id, String role) {
        return new CaseAssignableUser(
                id,
                "tenant-a",
                id,
                id,
                id + "@example.com",
                List.of(role),
                Set.of(),
                true,
                Map.of());
    }
}
