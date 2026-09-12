package org.openefms.auditlogging.audit;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
class AuditLogQueryService {
    private final AuditLogRepository repository;

    AuditLogQueryService(AuditLogRepository repository) {
        this.repository = repository;
    }

    AuditLogRecord auditEvent(UUID auditEventId) {
        return repository.findByAuditEventId(auditEventId)
                .orElseThrow(() -> new AuditLogRecordNotFoundException(auditEventId));
    }

    List<AuditLogRecord> auditEventsForTenant(String tenantId) {
        return repository.findByTenantId(normaliseRequired("tenantId", tenantId));
    }

    List<AuditLogRecord> auditEventsForCorrelation(String correlationId) {
        return repository.findByCorrelationId(normaliseRequired("correlationId", correlationId));
    }

    List<AuditLogRecord> auditEventsForResource(String tenantId, String resourceType, String resourceId) {
        List<String> errors = new ArrayList<>();
        String normalisedTenantId = normalise("tenantId", tenantId, errors);
        String normalisedResourceType = normalise("resourceType", resourceType, errors);
        String normalisedResourceId = normalise("resourceId", resourceId, errors);
        if (!errors.isEmpty()) {
            throw new InvalidAuditLogQueryException(errors);
        }
        return repository.findByResource(normalisedTenantId, normalisedResourceType, normalisedResourceId);
    }

    private String normaliseRequired(String field, String value) {
        List<String> errors = new ArrayList<>();
        String normalised = normalise(field, value, errors);
        if (!errors.isEmpty()) {
            throw new InvalidAuditLogQueryException(errors);
        }
        return normalised;
    }

    private String normalise(String field, String value, List<String> errors) {
        String normalised = value == null ? "" : value.trim();
        if (normalised.isBlank()) {
            errors.add(field + " must not be blank");
        }
        return normalised;
    }
}
