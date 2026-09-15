package org.openefms.casemanagement.service;

public interface CaseAssignmentPolicyProvider {
    public CaseAssignmentPolicy policy(String tenantId);
}
