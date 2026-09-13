package org.openefms.ingestionmanagement.controller;

import java.net.URI;
import java.util.List;
import org.openefms.common.models.management.ManagedResourceRecord;
import org.openefms.common.models.management.ManagedResourceRequest;
import org.openefms.common.models.management.ManagedResourceTypeDescriptor;
import org.openefms.ingestionmanagement.enums.IngestionManagementResourceType;
import org.openefms.ingestionmanagement.service.IngestionManagementResourceService;
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
@RequestMapping(path = "/api/v1/ingestion-management/resources")
public class IngestionManagementResourceController {
    private final IngestionManagementResourceService service;

    public IngestionManagementResourceController(IngestionManagementResourceService service) {
        this.service = service;
    }

    @GetMapping(path = "/types", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ManagedResourceTypeDescriptor> resourceTypes() {
        return service.resourceTypes();
    }

    @GetMapping(path = "/{resourceType}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ManagedResourceRecord> list(
            @PathVariable IngestionManagementResourceType resourceType,
            @RequestParam(required = false) String tenantId,
            @RequestParam(required = false) String filterText,
            @RequestParam(required = false) Boolean active) {
        return service.list(resourceType, tenantId, filterText, active);
    }

    @GetMapping(path = "/{resourceType}/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ManagedResourceRecord get(@PathVariable IngestionManagementResourceType resourceType, @PathVariable String id) {
        return service.get(resourceType, id);
    }

    @PostMapping(path = "/{resourceType}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ManagedResourceRecord> create(
            @PathVariable IngestionManagementResourceType resourceType,
            @RequestBody ManagedResourceRequest request) {
        ManagedResourceRecord resource = service.create(resourceType, request);
        return ResponseEntity
                .created(URI.create("/api/v1/ingestion-management/resources/" + resource.resourceType() + "/" + resource.id()))
                .body(resource);
    }

    @PutMapping(path = "/{resourceType}/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ManagedResourceRecord replace(
            @PathVariable IngestionManagementResourceType resourceType,
            @PathVariable String id,
            @RequestBody ManagedResourceRequest request) {
        return service.replace(resourceType, id, request);
    }

    @DeleteMapping(path = "/{resourceType}/{id}")
    public ResponseEntity<Void> delete(@PathVariable IngestionManagementResourceType resourceType, @PathVariable String id) {
        service.delete(resourceType, id);
        return ResponseEntity.noContent().build();
    }
}
