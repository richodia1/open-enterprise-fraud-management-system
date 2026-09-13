# Persistence Entity Conventions

OEFMS services own their business entities and database schema. Shared persistence code exists only for repeated technical columns and repository infrastructure.

## Shared Persistence Foundations

Common JPA foundations live in:

```text
shared/common-persistence/src/main/java/org/openefms/common/persistence/entity
```

Current shared foundations:

| Type | Purpose |
| --- | --- |
| `IdentifiableEntity<ID>` | Simple contract for entities that expose a stable application identity. |
| `UuidPrimaryKeyEntity` | Optional future base for new tables that use a default `id` UUID primary key. |
| `TenantScopedEntity` | Provides the shared `tenant_id` column. |
| `TenantScopedAttributesEntity` | Adds the shared `attributes_text` column for flexible metadata. |
| `TenantScopedEventAttributesEntity` | Adds event metadata columns such as `event_type`, `event_id`, `correlation_id`, and `trace_id`. |
| `TenantScopedCausalEventAttributesEntity` | Adds `causation_id` for records created from an upstream event. |

## Service-Owned Entities

Concrete business entities stay in their owning service:

```text
services/{service-name}/src/main/java/org/openefms/{service}/entity
```

Examples:

```text
FraudDecisionEntity        fraud-detection-service
ReceivedTransactionEntity  ingestion-management-service
FraudAlertEntity           case-management-service
AuditLogEntity             audit-logging-service
```

Do not move these entities into `common-persistence` or `common-models`. That would make services share database models and increase coupling between deployable services.

## Contributor Rule

When adding a new entity:

1. Put the entity in the owning service's `entity` package.
2. Use `common-persistence` only for shared technical columns.
3. Keep request and response contracts in `dto` or `common-models` only when the contract is intentionally shared.
4. Keep repositories, mappers, and migrations inside the owning service.

This preserves independent service evolution while still avoiding repeated JPA boilerplate.
