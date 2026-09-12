package org.openefms.casemanagement.workflow;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface CaseWorkflowRepository {
    CaseWorkflowRecord save(AlertRecord alert, CaseRecord caseFile);

    Optional<AlertRecord> findAlertById(UUID alertId);

    List<AlertRecord> findAlertsByTenantId(String tenantId);

    Optional<CaseRecord> findCaseById(UUID caseId);

    List<CaseRecord> findCasesByTenantId(String tenantId);
}
