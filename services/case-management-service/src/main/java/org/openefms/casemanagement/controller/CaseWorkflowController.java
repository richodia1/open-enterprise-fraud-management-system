package org.openefms.casemanagement.controller;

import java.util.List;
import java.util.UUID;
import org.openefms.casemanagement.dto.CaseActionResponse;
import org.openefms.casemanagement.dto.CaseAssignableUser;
import org.openefms.casemanagement.dto.CaseAssignmentRequest;
import org.openefms.casemanagement.dto.CaseStatusUpdateRequest;
import org.openefms.casemanagement.entity.AlertRecord;
import org.openefms.casemanagement.entity.CaseActionRecord;
import org.openefms.casemanagement.entity.CaseRecord;
import org.openefms.casemanagement.enums.CaseAssignmentPurpose;
import org.openefms.casemanagement.service.CaseAssignmentDirectory;
import org.openefms.casemanagement.service.CaseLifecycleService;
import org.openefms.casemanagement.service.CaseWorkflowQueryService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1")
public class CaseWorkflowController {
    private final CaseAssignmentDirectory assignmentDirectory;
    private final CaseLifecycleService lifecycleService;
    private final CaseWorkflowQueryService queryService;

    public CaseWorkflowController(
            CaseAssignmentDirectory assignmentDirectory,
            CaseLifecycleService lifecycleService,
            CaseWorkflowQueryService queryService) {
        this.assignmentDirectory = assignmentDirectory;
        this.lifecycleService = lifecycleService;
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

    @GetMapping(path = "/cases/{caseId}/actions", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<CaseActionRecord> caseActions(@PathVariable UUID caseId) {
        return lifecycleService.history(caseId);
    }

    @GetMapping(path = "/cases/assignees", params = "tenantId", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<CaseAssignableUser> caseAssignees(
            @RequestParam String tenantId,
            @RequestParam(required = false) CaseAssignmentPurpose purpose,
            @RequestParam(required = false) String filterText) {
        CaseAssignmentPurpose resolvedPurpose = purpose == null ? CaseAssignmentPurpose.ASSIGNMENT : purpose;
        return assignmentDirectory.listAssignableUsers(tenantId, filterText, resolvedPurpose);
    }

    @PatchMapping(
            path = "/cases/{caseId}/assignment",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public CaseActionResponse assignCase(
            @PathVariable UUID caseId,
            @RequestBody CaseAssignmentRequest request) {
        return lifecycleService.assign(caseId, request);
    }

    @PatchMapping(
            path = "/cases/{caseId}/status",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public CaseActionResponse updateCaseStatus(
            @PathVariable UUID caseId,
            @RequestBody CaseStatusUpdateRequest request) {
        return lifecycleService.updateStatus(caseId, request);
    }

    @GetMapping(path = "/cases", params = "tenantId", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<CaseRecord> casesForTenant(@RequestParam String tenantId) {
        return queryService.casesForTenant(tenantId);
    }
}
