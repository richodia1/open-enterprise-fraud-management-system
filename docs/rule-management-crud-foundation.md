# Rule Management CRUD Foundation

The first Rule Management admin CRUD slice exposes the configuration contracts needed by a future operations UI.

## Purpose

This branch restores the most important configuration and visibility surfaces from the older .NET implementation into the Spring Boot Java 21 service contract:

| Resource | Purpose |
| --- | --- |
| Rules | Manage baseline and institution-specific fraud rule definitions. |
| Rule categories | Maintain category lookup data used when creating or filtering rules. |
| Rule escalation users | Configure who receives escalation or review ownership for a rule. |
| Rule definitions | Store rule-builder condition rows such as operator, datapoint, value, and group connector. |
| Rule audit trails | Read configuration history and keep parity with the original admin screens. |
| Rule approvals | Support maker-checker approval and rejection of pending rule actions. |

## Current Storage

All resources are in memory for now. This is intentional for the MVP contract phase:

- Swagger can show the real REST operations.
- UI work can start against stable request and response shapes.
- Contributors can review the resource boundaries before database migrations are added.
- Multi-database support can be handled later through the shared persistence pattern.

## Package Layout

Rule Management now follows the same clean package shape expected for future services:

| Package | Responsibility |
| --- | --- |
| `controller` | REST controllers and HTTP error handling. |
| `dto` | Request payloads accepted by the REST API. |
| `entity` | Service-owned domain records used by the admin configuration API. |
| `enums` | Service-owned lifecycle and audit enums. |
| `repository` | Repository contracts, in-memory repository adapters, and seed data. |
| `service` | Business workflow, validation orchestration, approval flow, and audit writes. |

## Swagger Groups

Run the Rule Management Service and open:

```text
http://localhost:8082/swagger-ui/index.html
```

The Swagger paths should include:

```text
/api/v1/rules
/api/v1/rule-categories
/api/v1/rule-escalation-users
/api/v1/rule-definitions
/api/v1/rule-audit-trails
/api/v1/rule-approvals/pending
```

## Next Persistence Step

When this contract is accepted, the next implementation step is to replace the in-memory repository with service-owned persistence for:

- `rule_categories`
- `rule_definitions`
- `rule_escalation_users`
- `rule_approvals`
- `rule_audit_trails`

That migration should preserve the current API shapes unless a contract review deliberately changes them.
