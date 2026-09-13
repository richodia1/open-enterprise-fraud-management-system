package org.openefms.identity.controller;

import java.net.URI;
import java.util.List;
import org.openefms.identity.dto.TenantIdentityProviderRequest;
import org.openefms.identity.dto.TenantIdentityProviderTypeDescriptor;
import org.openefms.identity.entity.TenantIdentityProvider;
import org.openefms.identity.enums.TenantIdentityProviderType;
import org.openefms.identity.service.TenantIdentityProviderService;
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
@RequestMapping(path = "/api/v1/identity/tenant-identity-providers")
public class TenantIdentityProviderController {
    private final TenantIdentityProviderService service;

    public TenantIdentityProviderController(TenantIdentityProviderService service) {
        this.service = service;
    }

    @GetMapping(path = "/provider-types", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<TenantIdentityProviderTypeDescriptor> providerTypes() {
        return service.providerTypes();
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<TenantIdentityProvider> list(
            @RequestParam(required = false) String tenantId,
            @RequestParam(required = false) TenantIdentityProviderType providerType,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(required = false) String filterText) {
        return service.list(tenantId, providerType, enabled, filterText);
    }

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public TenantIdentityProvider get(@PathVariable String id) {
        return service.get(id);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TenantIdentityProvider> create(@RequestBody TenantIdentityProviderRequest request) {
        TenantIdentityProvider provider = service.create(request);
        return ResponseEntity
                .created(URI.create("/api/v1/identity/tenant-identity-providers/" + provider.id()))
                .body(provider);
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public TenantIdentityProvider replace(@PathVariable String id, @RequestBody TenantIdentityProviderRequest request) {
        return service.replace(id, request);
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
