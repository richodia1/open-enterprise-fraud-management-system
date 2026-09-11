package org.openefms.rulemanagement.rule;

import java.net.URI;
import java.util.List;
import org.openefms.common.models.rule.FraudRuleDefinition;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
class RuleManagementController {
    private final RuleManagementService service;

    RuleManagementController(RuleManagementService service) {
        this.service = service;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    List<FraudRuleDefinition> list(
            @RequestParam(defaultValue = BaselineRuleDefinitions.GLOBAL_TENANT_ID) String tenantId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category) {
        return service.list(tenantId, status, category);
    }

    @GetMapping(path = "/{ruleCode}", produces = MediaType.APPLICATION_JSON_VALUE)
    FraudRuleDefinition get(
            @PathVariable String ruleCode,
            @RequestParam(defaultValue = BaselineRuleDefinitions.GLOBAL_TENANT_ID) String tenantId) {
        return service.get(tenantId, ruleCode);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<FraudRuleDefinition> create(@RequestBody RuleDefinitionRequest request) {
        FraudRuleDefinition definition = service.create(request);
        return ResponseEntity
                .created(URI.create("/api/v1/rules/" + definition.ruleCode() + "?tenantId=" + definition.tenantId()))
                .body(definition);
    }

    @PutMapping(path = "/{ruleCode}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    FraudRuleDefinition replace(
            @PathVariable String ruleCode,
            @RequestBody RuleDefinitionRequest request) {
        return service.replace(ruleCode, request);
    }

    @PatchMapping(path = "/{ruleCode}/status", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    FraudRuleDefinition updateStatus(
            @PathVariable String ruleCode,
            @RequestBody RuleStatusUpdateRequest request) {
        return service.updateStatus(ruleCode, request);
    }
}
