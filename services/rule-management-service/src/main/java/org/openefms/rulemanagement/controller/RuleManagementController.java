package org.openefms.rulemanagement.controller;

import java.net.URI;
import java.util.List;
import org.openefms.common.models.rule.FraudRuleDefinition;
import org.openefms.rulemanagement.dto.RuleApprovalRequest;
import org.openefms.rulemanagement.dto.RuleDefinitionRequest;
import org.openefms.rulemanagement.dto.RuleStatusUpdateRequest;
import org.openefms.rulemanagement.entity.RuleApproval;
import org.openefms.rulemanagement.repository.BaselineRuleDefinitions;
import org.openefms.rulemanagement.service.RuleManagementService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/rules")
public class RuleManagementController {
    private final RuleManagementService service;

    public RuleManagementController(RuleManagementService service) {
        this.service = service;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<FraudRuleDefinition> list(
            @RequestParam(defaultValue = BaselineRuleDefinitions.GLOBAL_TENANT_ID) String tenantId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category) {
        return service.list(tenantId, status, category);
    }

    @GetMapping(path = "/{ruleCode}", produces = MediaType.APPLICATION_JSON_VALUE)
    public FraudRuleDefinition get(
            @PathVariable String ruleCode,
            @RequestParam(defaultValue = BaselineRuleDefinitions.GLOBAL_TENANT_ID) String tenantId) {
        return service.get(tenantId, ruleCode);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<FraudRuleDefinition> create(@RequestBody RuleDefinitionRequest request) {
        FraudRuleDefinition definition = service.create(request);
        return ResponseEntity
                .created(URI.create("/api/v1/rules/" + definition.ruleCode() + "?tenantId=" + definition.tenantId()))
                .body(definition);
    }

    @PutMapping(path = "/{ruleCode}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public FraudRuleDefinition replace(
            @PathVariable String ruleCode,
            @RequestBody RuleDefinitionRequest request) {
        return service.replace(ruleCode, request);
    }

    @PatchMapping(path = "/{ruleCode}/status", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public FraudRuleDefinition updateStatus(
            @PathVariable String ruleCode,
            @RequestBody RuleStatusUpdateRequest request) {
        return service.updateStatus(ruleCode, request);
    }

    @DeleteMapping(path = "/{ruleCode}")
    public ResponseEntity<Void> delete(
            @PathVariable String ruleCode,
            @RequestParam(defaultValue = BaselineRuleDefinitions.GLOBAL_TENANT_ID) String tenantId,
            @RequestParam(defaultValue = "system") String updatedBy) {
        service.delete(tenantId, ruleCode, updatedBy);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(path = "/{ruleCode}/submit-for-approval", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RuleApproval> submitForApproval(
            @PathVariable String ruleCode,
            @RequestBody RuleApprovalRequest request) {
        RuleApproval approval = service.submitForApproval(ruleCode, request);
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .location(URI.create("/api/v1/rule-approvals/" + approval.approvalId()))
                .body(approval);
    }
}
