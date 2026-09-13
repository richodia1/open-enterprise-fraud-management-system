package org.openefms.casemanagement.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.openefms.casemanagement.entity.AlertRecord;
import org.openefms.casemanagement.entity.CaseRecord;
import org.openefms.casemanagement.entity.CaseWorkflowRecord;

public interface CaseWorkflowRepository {
    public CaseWorkflowRecord save(AlertRecord alert, CaseRecord caseFile);

    public Optional<AlertRecord> findAlertById(UUID alertId);

    public List<AlertRecord> findAlertsByTenantId(String tenantId);

    public Optional<CaseRecord> findCaseById(UUID caseId);

    public List<CaseRecord> findCasesByTenantId(String tenantId);
}
