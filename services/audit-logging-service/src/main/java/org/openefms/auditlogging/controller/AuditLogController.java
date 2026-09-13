package org.openefms.auditlogging.controller;

import java.util.List;
import java.util.UUID;
import org.openefms.auditlogging.entity.AuditLogRecord;
import org.openefms.auditlogging.service.AuditLogQueryService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/audit-events")
public class AuditLogController {
    private final AuditLogQueryService queryService;

    public AuditLogController(AuditLogQueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping(path = "/{auditEventId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public AuditLogRecord auditEvent(@PathVariable UUID auditEventId) {
        return queryService.auditEvent(auditEventId);
    }

    @GetMapping(
            params = {"tenantId", "!resourceType", "!resourceId", "!correlationId"},
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<AuditLogRecord> auditEventsForTenant(@RequestParam String tenantId) {
        return queryService.auditEventsForTenant(tenantId);
    }

    @GetMapping(
            params = {"correlationId", "!tenantId", "!resourceType", "!resourceId"},
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<AuditLogRecord> auditEventsForCorrelation(@RequestParam String correlationId) {
        return queryService.auditEventsForCorrelation(correlationId);
    }

    @GetMapping(
            params = {"tenantId", "resourceType", "resourceId"},
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<AuditLogRecord> auditEventsForResource(
            @RequestParam String tenantId,
            @RequestParam String resourceType,
            @RequestParam String resourceId) {
        return queryService.auditEventsForResource(tenantId, resourceType, resourceId);
    }
}
