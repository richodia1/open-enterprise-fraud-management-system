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
@RequestMapping(path = "/api/v1/rule-definitions")
class RuleConditionDefinitionController {
    private final RuleManagementService service;

    RuleConditionDefinitionController(RuleManagementService service) {
        this.service = service;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    List<RuleConditionDefinition> list(
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
    RuleConditionDefinition get(@PathVariable String id) {
        return service.getConditionDefinition(id);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<RuleConditionDefinition> create(@RequestBody RuleConditionDefinitionRequest request) {
        RuleConditionDefinition definition = service.createConditionDefinition(request);
        return ResponseEntity
                .created(URI.create("/api/v1/rule-definitions/" + definition.id()))
                .body(definition);
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    RuleConditionDefinition replace(
            @PathVariable String id,
            @RequestBody RuleConditionDefinitionRequest request) {
        return service.replaceConditionDefinition(id, request);
    }

    @DeleteMapping(path = "/{id}")
    ResponseEntity<Void> delete(@PathVariable String id) {
        service.deleteConditionDefinition(id);
        return ResponseEntity.noContent().build();
    }
}
