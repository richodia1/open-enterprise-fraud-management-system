package org.openefms.casemanagement.workflow;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
class CaseWorkflowQueryService {
    private final CaseWorkflowRepository repository;

    CaseWorkflowQueryService(CaseWorkflowRepository repository) {
        this.repository = repository;
    }

    AlertRecord alert(UUID alertId) {
        return repository.findAlertById(alertId)
                .orElseThrow(() -> new CaseWorkflowRecordNotFoundException("Alert", alertId));
    }

    List<AlertRecord> alertsForTenant(String tenantId) {
        return repository.findAlertsByTenantId(normaliseTenantId(tenantId));
    }

    CaseRecord caseFile(UUID caseId) {
        return repository.findCaseById(caseId)
                .orElseThrow(() -> new CaseWorkflowRecordNotFoundException("Case", caseId));
    }

    List<CaseRecord> casesForTenant(String tenantId) {
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
