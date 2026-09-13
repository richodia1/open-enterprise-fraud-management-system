package org.openefms.rulemanagement.controller;

import java.net.URI;
import java.util.List;
import org.openefms.rulemanagement.dto.RuleEscalationUserRequest;
import org.openefms.rulemanagement.entity.RuleEscalationUser;
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
@RequestMapping(path = "/api/v1/rule-escalation-users")
public class RuleEscalationUserController {
    private final RuleManagementService service;

    public RuleEscalationUserController(RuleManagementService service) {
        this.service = service;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<RuleEscalationUser> list(
            @RequestParam(defaultValue = BaselineRuleDefinitions.GLOBAL_TENANT_ID) String tenantId,
            @RequestParam(required = false) String filterText,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String ruleId,
            @RequestParam(required = false) String ruleCode,
            @RequestParam(required = false) String name) {
        return service.listEscalationUsers(tenantId, filterText, email, userId, ruleId, ruleCode, name);
    }

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public RuleEscalationUser get(@PathVariable String id) {
        return service.getEscalationUser(id);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RuleEscalationUser> create(@RequestBody RuleEscalationUserRequest request) {
        RuleEscalationUser user = service.createEscalationUser(request);
        return ResponseEntity
                .created(URI.create("/api/v1/rule-escalation-users/" + user.id()))
                .body(user);
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public RuleEscalationUser replace(
            @PathVariable String id,
            @RequestBody RuleEscalationUserRequest request) {
        return service.replaceEscalationUser(id, request);
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.deleteEscalationUser(id);
        return ResponseEntity.noContent().build();
    }
}
