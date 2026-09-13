package org.openefms.casemanagement.controller;

import java.util.List;
import java.util.UUID;
import org.openefms.casemanagement.entity.AlertRecord;
import org.openefms.casemanagement.entity.CaseRecord;
import org.openefms.casemanagement.service.CaseWorkflowQueryService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1")
public class CaseWorkflowController {
    private final CaseWorkflowQueryService queryService;

    public CaseWorkflowController(CaseWorkflowQueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping(path = "/alerts/{alertId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public AlertRecord alert(@PathVariable UUID alertId) {
        return queryService.alert(alertId);
    }

    @GetMapping(path = "/alerts", params = "tenantId", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<AlertRecord> alertsForTenant(@RequestParam String tenantId) {
        return queryService.alertsForTenant(tenantId);
    }

    @GetMapping(path = "/cases/{caseId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CaseRecord caseFile(@PathVariable UUID caseId) {
        return queryService.caseFile(caseId);
    }

    @GetMapping(path = "/cases", params = "tenantId", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<CaseRecord> casesForTenant(@RequestParam String tenantId) {
        return queryService.casesForTenant(tenantId);
    }
}
