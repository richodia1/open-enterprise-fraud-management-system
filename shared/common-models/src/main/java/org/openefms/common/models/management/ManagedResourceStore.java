package org.openefms.common.models.management;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;

public final class ManagedResourceStore<T extends Enum<T> & ManagedResourceKind> {
    public static final String GLOBAL_TENANT_ID = "global";

    private final Class<T> resourceTypeClass;
    private final Clock clock;
    private final ConcurrentMap<T, ConcurrentMap<String, ManagedResourceRecord>> resources = new ConcurrentHashMap<>();

    public ManagedResourceStore(Class<T> resourceTypeClass, Clock clock) {
        this.resourceTypeClass = resourceTypeClass;
        this.clock = clock;
        seedDefaults();
    }

    public List<ManagedResourceTypeDescriptor> resourceTypes() {
        return Stream.of(resourceTypeClass.getEnumConstants())
                .map(type -> new ManagedResourceTypeDescriptor(type.name(), type.displayName(), type.legacyResource(), type.notes()))
                .toList();
    }

    public List<ManagedResourceRecord> list(T resourceType, String tenantId, String filterText, Boolean active) {
        String resolvedTenantId = normaliseTenantId(tenantId);
        return bucket(resourceType).values().stream()
                .filter(resource -> resource.tenantId().equals(resolvedTenantId))
                .filter(resource -> active == null || resource.active() == active)
                .filter(resource -> matchesAny(filterText, resource.code(), resource.name(), resource.description()))
                .sorted(Comparator
                        .comparing(ManagedResourceRecord::resourceType)
                        .thenComparing(ManagedResourceRecord::tenantId)
                        .thenComparing(ManagedResourceRecord::code)
                        .thenComparing(ManagedResourceRecord::id))
                .toList();
    }

    public ManagedResourceRecord get(T resourceType, String id) {
        String resolvedId = requiredText(id, "id");
        return Optional.ofNullable(bucket(resourceType).get(resolvedId))
                .orElseThrow(() -> new ManagedResourceNotFoundException(resourceType.name(), resolvedId));
    }

    public ManagedResourceRecord create(T resourceType, ManagedResourceRequest request) {
        ValidatedRequest validated = validate(request, true);
        ensureCodeAvailable(resourceType, null, validated.tenantId(), validated.code());
        OffsetDateTime now = OffsetDateTime.now(clock);
        ManagedResourceRecord resource = new ManagedResourceRecord(
                UUID.randomUUID().toString(),
                validated.tenantId(),
                resourceType.name(),
                validated.code(),
                validated.name(),
                validated.description(),
                validated.active(),
                validated.attributes(),
                now,
                now,
                validated.updatedBy());
        bucket(resourceType).put(resource.id(), resource);
        return resource;
    }

    public ManagedResourceRecord replace(T resourceType, String id, ManagedResourceRequest request) {
        ManagedResourceRecord existing = get(resourceType, id);
        ValidatedRequest validated = validate(request, true);
        ensureCodeAvailable(resourceType, existing.id(), validated.tenantId(), validated.code());
        ManagedResourceRecord resource = new ManagedResourceRecord(
                existing.id(),
                validated.tenantId(),
                resourceType.name(),
                validated.code(),
                validated.name(),
                validated.description(),
                validated.active(),
                validated.attributes(),
                existing.createdAt(),
                OffsetDateTime.now(clock),
                validated.updatedBy());
        bucket(resourceType).put(resource.id(), resource);
        return resource;
    }

    public void delete(T resourceType, String id) {
        ManagedResourceRecord existing = get(resourceType, id);
        bucket(resourceType).remove(existing.id());
    }

    private void seedDefaults() {
        for (T type : resourceTypeClass.getEnumConstants()) {
            OffsetDateTime now = OffsetDateTime.now(clock);
            String id = UUID.nameUUIDFromBytes(
                    (resourceTypeClass.getName() + ":" + type.name()).getBytes(StandardCharsets.UTF_8)).toString();
            ManagedResourceRecord seed = new ManagedResourceRecord(
                    id,
                    GLOBAL_TENANT_ID,
                    type.name(),
                    codeFrom(type.name()),
                    type.displayName(),
                    "Foundation record for " + type.legacyResource() + " from the legacy CRUD surface.",
                    true,
                    Map.of("source", "legacy-dotnet-crud-map"),
                    now,
                    now,
                    "system");
            bucket(type).put(seed.id(), seed);
        }
    }

    private ConcurrentMap<String, ManagedResourceRecord> bucket(T resourceType) {
        return resources.computeIfAbsent(resourceType, ignored -> new ConcurrentHashMap<>());
    }

    private ValidatedRequest validate(ManagedResourceRequest request, boolean codeRequired) {
        List<String> errors = new ArrayList<>();
        if (request == null) {
            throw new ManagedResourceValidationException(List.of("request body is required"));
        }
        String tenantId = normaliseTenantId(request.tenantId());
        String code = textOrNull(request.code());
        String name = textOrNull(request.name());
        String updatedBy = textOrNull(request.updatedBy());
        if (codeRequired && code == null) {
            errors.add("code is required");
        }
        if (name == null) {
            errors.add("name is required");
        }
        if (updatedBy == null) {
            errors.add("updatedBy is required");
        }
        if (!errors.isEmpty()) {
            throw new ManagedResourceValidationException(errors);
        }
        return new ValidatedRequest(
                tenantId,
                code,
                name,
                textOrNull(request.description()),
                request.active() == null || request.active(),
                immutableAttributes(request.attributes()),
                updatedBy);
    }

    private void ensureCodeAvailable(T resourceType, String existingId, String tenantId, String code) {
        boolean duplicate = bucket(resourceType).values().stream()
                .anyMatch(resource -> resource.tenantId().equals(tenantId)
                        && resource.code().equalsIgnoreCase(code)
                        && (existingId == null || !resource.id().equals(existingId)));
        if (duplicate) {
            throw new ManagedResourceConflictException(resourceType.name() + " code " + code + " already exists for tenant " + tenantId);
        }
    }

    private boolean matchesAny(String filterText, String... values) {
        String filter = textOrNull(filterText);
        if (filter == null) {
            return true;
        }
        String normalisedFilter = filter.toLowerCase(Locale.ROOT);
        for (String value : values) {
            if (value != null && value.toLowerCase(Locale.ROOT).contains(normalisedFilter)) {
                return true;
            }
        }
        return false;
    }

    private Map<String, String> immutableAttributes(Map<String, String> attributes) {
        if (attributes == null || attributes.isEmpty()) {
            return Map.of();
        }
        Map<String, String> cleaned = new LinkedHashMap<>();
        attributes.forEach((key, value) -> {
            String cleanedKey = textOrNull(key);
            if (cleanedKey != null) {
                cleaned.put(cleanedKey, value == null ? "" : value);
            }
        });
        return Map.copyOf(cleaned);
    }

    private String normaliseTenantId(String tenantId) {
        String resolved = textOrNull(tenantId);
        return resolved == null ? GLOBAL_TENANT_ID : resolved;
    }

    private String requiredText(String value, String fieldName) {
        String resolved = textOrNull(value);
        if (resolved == null) {
            throw new ManagedResourceValidationException(List.of(fieldName + " is required"));
        }
        return resolved;
    }

    private String textOrNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String codeFrom(String name) {
        return name.toLowerCase(Locale.ROOT).replace('_', '-');
    }

    private record ValidatedRequest(
            String tenantId,
            String code,
            String name,
            String description,
            boolean active,
            Map<String, String> attributes,
            String updatedBy
    ) {
    }
}
