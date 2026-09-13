package org.openefms.rulemanagement.controller;

import java.net.URI;
import java.util.List;
import org.openefms.rulemanagement.dto.RuleConditionDefinitionRequest;
import org.openefms.rulemanagement.entity.RuleConditionDefinition;
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
@RequestMapping(path = "/api/v1/rule-definitions")
public class RuleConditionDefinitionController {
    private final RuleManagementService service;

    public RuleConditionDefinitionController(RuleManagementService service) {
        this.service = service;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<RuleConditionDefinition> list(
            @RequestParam(defaultValue = BaselineRuleDefinitions.GLOBAL_TENANT_ID) String tenantId,
            @RequestParam(required = false) String filterText,
            @RequestParam(required = false) String ruleId,
            @RequestParam(required = false) String ruleCode,
            @RequestParam(required = false) String sourceDataPointCode,
            @RequestParam(required = false) String destinationDataPointCode,
            @RequestParam(required = false) String operator) {
        return service.listConditionDefinitions(
                tenantId,
                filterText,
                ruleId,
                ruleCode,
                sourceDataPointCode,
                destinationDataPointCode,
                operator);
    }

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public RuleConditionDefinition get(@PathVariable String id) {
        return service.getConditionDefinition(id);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RuleConditionDefinition> create(@RequestBody RuleConditionDefinitionRequest request) {
        RuleConditionDefinition definition = service.createConditionDefinition(request);
        return ResponseEntity
                .created(URI.create("/api/v1/rule-definitions/" + definition.id()))
                .body(definition);
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public RuleConditionDefinition replace(
            @PathVariable String id,
            @RequestBody RuleConditionDefinitionRequest request) {
        return service.replaceConditionDefinition(id, request);
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.deleteConditionDefinition(id);
        return ResponseEntity.noContent().build();
    }
}
