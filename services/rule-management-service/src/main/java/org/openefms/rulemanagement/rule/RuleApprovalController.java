package org.openefms.rulemanagement.rule;

import java.util.List;
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
class RuleApprovalController {
    private final RuleManagementService service;

    RuleApprovalController(RuleManagementService service) {
        this.service = service;
    }

    @GetMapping(path = "/pending", produces = MediaType.APPLICATION_JSON_VALUE)
    List<RuleApproval> pending(
            @RequestParam(defaultValue = BaselineRuleDefinitions.GLOBAL_TENANT_ID) String tenantId) {
        return service.listPendingApprovals(tenantId);
    }

    @PostMapping(path = "/{approvalId}/approve", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    RuleApproval approve(
            @PathVariable String approvalId,
            @RequestBody RuleApprovalDecisionRequest request) {
        return service.approve(approvalId, request);
    }

    @PostMapping(path = "/{approvalId}/reject", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    RuleApproval reject(
            @PathVariable String approvalId,
            @RequestBody RuleApprovalDecisionRequest request) {
        return service.reject(approvalId, request);
    }
}
