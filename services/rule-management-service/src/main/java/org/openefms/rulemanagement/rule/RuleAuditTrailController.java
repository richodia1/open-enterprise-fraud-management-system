package org.openefms.rulemanagement.rule;

import java.net.URI;
import java.util.List;
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
class RuleAuditTrailController {
    private final RuleManagementService service;

    RuleAuditTrailController(RuleManagementService service) {
        this.service = service;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    List<RuleAuditTrail> list(
            @RequestParam(defaultValue = BaselineRuleDefinitions.GLOBAL_TENANT_ID) String tenantId,
            @RequestParam(required = false) String ruleId,
            @RequestParam(required = false) String ruleCode,
            @RequestParam(required = false) RuleAuditAction action) {
        return service.listAuditTrails(tenantId, ruleId, ruleCode, action);
    }

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    RuleAuditTrail get(@PathVariable String id) {
        return service.getAuditTrail(id);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<RuleAuditTrail> create(@RequestBody RuleAuditTrailRequest request) {
        RuleAuditTrail auditTrail = service.createAuditTrail(request);
        return ResponseEntity
                .created(URI.create("/api/v1/rule-audit-trails/" + auditTrail.id()))
                .body(auditTrail);
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    RuleAuditTrail replace(
            @PathVariable String id,
            @RequestBody RuleAuditTrailRequest request) {
        return service.replaceAuditTrail(id, request);
    }

    @DeleteMapping(path = "/{id}")
    ResponseEntity<Void> delete(@PathVariable String id) {
        service.deleteAuditTrail(id);
        return ResponseEntity.noContent().build();
    }
}
