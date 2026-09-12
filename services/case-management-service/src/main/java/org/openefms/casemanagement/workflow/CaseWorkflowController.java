package org.openefms.casemanagement.workflow;

import java.util.List;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1")
class CaseWorkflowController {
    private final CaseWorkflowQueryService queryService;

    CaseWorkflowController(CaseWorkflowQueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping(path = "/alerts/{alertId}", produces = MediaType.APPLICATION_JSON_VALUE)
    AlertRecord alert(@PathVariable UUID alertId) {
        return queryService.alert(alertId);
    }

    @GetMapping(path = "/alerts", params = "tenantId", produces = MediaType.APPLICATION_JSON_VALUE)
    List<AlertRecord> alertsForTenant(@RequestParam String tenantId) {
        return queryService.alertsForTenant(tenantId);
    }

    @GetMapping(path = "/cases/{caseId}", produces = MediaType.APPLICATION_JSON_VALUE)
    CaseRecord caseFile(@PathVariable UUID caseId) {
        return queryService.caseFile(caseId);
    }

    @GetMapping(path = "/cases", params = "tenantId", produces = MediaType.APPLICATION_JSON_VALUE)
    List<CaseRecord> casesForTenant(@RequestParam String tenantId) {
        return queryService.casesForTenant(tenantId);
    }
}
