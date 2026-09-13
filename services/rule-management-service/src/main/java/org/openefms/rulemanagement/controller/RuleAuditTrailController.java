package org.openefms.rulemanagement.controller;

import java.net.URI;
import java.util.List;
import org.openefms.rulemanagement.dto.RuleAuditTrailRequest;
import org.openefms.rulemanagement.entity.RuleAuditTrail;
import org.openefms.rulemanagement.enums.RuleAuditAction;
import org.openefms.rulemanagement.repository.BaselineRuleDefinitions;
import org.openefms.rulemanagement.service.RuleManagementService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/rule-audit-trails")
public class RuleAuditTrailController {
    private final RuleManagementService service;

    public RuleAuditTrailController(RuleManagementService service) {
        this.service = service;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<RuleAuditTrail> list(
            @RequestParam(defaultValue = BaselineRuleDefinitions.GLOBAL_TENANT_ID) String tenantId,
            @RequestParam(required = false) String ruleId,
            @RequestParam(required = false) String ruleCode,
            @RequestParam(required = false) RuleAuditAction action) {
        return service.listAuditTrails(tenantId, ruleId, ruleCode, action);
    }

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public RuleAuditTrail get(@PathVariable String id) {
        return service.getAuditTrail(id);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RuleAuditTrail> create(@RequestBody RuleAuditTrailRequest request) {
        RuleAuditTrail auditTrail = service.createAuditTrail(request);
        return ResponseEntity
                .created(URI.create("/api/v1/rule-audit-trails/" + auditTrail.id()))
                .body(auditTrail);
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public RuleAuditTrail replace(
            @PathVariable String id,
            @RequestBody RuleAuditTrailRequest request) {
        return service.replaceAuditTrail(id, request);
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.deleteAuditTrail(id);
        return ResponseEntity.noContent().build();
    }
}
