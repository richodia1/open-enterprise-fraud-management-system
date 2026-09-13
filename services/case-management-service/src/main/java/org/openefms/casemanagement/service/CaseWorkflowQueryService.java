package org.openefms.casemanagement.service;

import java.util.List;
import java.util.UUID;
import org.openefms.casemanagement.entity.AlertRecord;
import org.openefms.casemanagement.entity.CaseRecord;
import org.openefms.casemanagement.repository.CaseWorkflowRepository;
import org.springframework.stereotype.Service;

@Service
public class CaseWorkflowQueryService {
    private final CaseWorkflowRepository repository;

    public CaseWorkflowQueryService(CaseWorkflowRepository repository) {
        this.repository = repository;
    }

    public AlertRecord alert(UUID alertId) {
        return repository.findAlertById(alertId)
                .orElseThrow(() -> new CaseWorkflowRecordNotFoundException("Alert", alertId));
    }

    public List<AlertRecord> alertsForTenant(String tenantId) {
        return repository.findAlertsByTenantId(normaliseTenantId(tenantId));
    }

    public CaseRecord caseFile(UUID caseId) {
        return repository.findCaseById(caseId)
                .orElseThrow(() -> new CaseWorkflowRecordNotFoundException("Case", caseId));
    }

    public List<CaseRecord> casesForTenant(String tenantId) {
        return repository.findCasesByTenantId(normaliseTenantId(tenantId));
    }

    private String normaliseTenantId(String tenantId) {
        String normalisedTenantId = tenantId == null ? "" : tenantId.trim();
        if (normalisedTenantId.isBlank()) {
            throw new InvalidCaseWorkflowQueryException(List.of("tenantId must not be blank"));
        }
        return normalisedTenantId;
    }
}
