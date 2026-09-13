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
@RequestMapping(path = "/api/v1/rule-categories")
class RuleCategoryController {
    private final RuleManagementService service;

    RuleCategoryController(RuleManagementService service) {
        this.service = service;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    List<RuleCategory> list(
            @RequestParam(defaultValue = BaselineRuleDefinitions.GLOBAL_TENANT_ID) String tenantId,
            @RequestParam(required = false) String filterText,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description) {
        return service.listCategories(tenantId, filterText, name, description);
    }

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    RuleCategory get(@PathVariable String id) {
        return service.getCategory(id);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<RuleCategory> create(@RequestBody RuleCategoryRequest request) {
        RuleCategory category = service.createCategory(request);
        return ResponseEntity
                .created(URI.create("/api/v1/rule-categories/" + category.id()))
                .body(category);
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    RuleCategory replace(
            @PathVariable String id,
            @RequestBody RuleCategoryRequest request) {
        return service.replaceCategory(id, request);
    }

    @DeleteMapping(path = "/{id}")
    ResponseEntity<Void> delete(@PathVariable String id) {
        service.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
