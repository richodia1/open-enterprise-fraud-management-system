package org.openefms.rulemanagement.controller;

import java.util.List;
import org.openefms.rulemanagement.dto.RuleApprovalDecisionRequest;
import org.openefms.rulemanagement.entity.RuleApproval;
import org.openefms.rulemanagement.repository.BaselineRuleDefinitions;
import org.openefms.rulemanagement.service.RuleManagementService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/rule-approvals")
public class RuleApprovalController {
    private final RuleManagementService service;

    public RuleApprovalController(RuleManagementService service) {
        this.service = service;
    }

    @GetMapping(path = "/pending", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<RuleApproval> pending(
            @RequestParam(defaultValue = BaselineRuleDefinitions.GLOBAL_TENANT_ID) String tenantId) {
        return service.listPendingApprovals(tenantId);
    }

    @PostMapping(path = "/{approvalId}/approve", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public RuleApproval approve(
            @PathVariable String approvalId,
            @RequestBody RuleApprovalDecisionRequest request) {
        return service.approve(approvalId, request);
    }

    @PostMapping(path = "/{approvalId}/reject", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public RuleApproval reject(
            @PathVariable String approvalId,
            @RequestBody RuleApprovalDecisionRequest request) {
        return service.reject(approvalId, request);
    }
}
