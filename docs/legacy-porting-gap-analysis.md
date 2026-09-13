# Legacy Porting Gap Analysis

Last reviewed: 13 September 2026.

This document answers whether the current Java/Spring Boot OEFMS project has everything needed from the previous .NET implementation.

## Short Answer

The source material needed to continue the port is available locally, and the Java monorepo now has the correct service shells and a first contract-first CRUD surface.

The Java project is not yet a full clone of the previous .NET implementation. It currently contains the platform foundation, selected real workflows, and generic management-resource CRUD contracts. It does not yet contain every old entity, entity-specific DTO, validation rule, database migration, custom workflow endpoint, background job, import/export action, or integration adapter.

## Old Source Material Available Locally

The old source folders are:

```text
/Users/richard/Documents/FraudDetectionSystem/EFAMSStudioServices
/Users/richard/Documents/FraudDetectionSystem/Fraud Detection Service
```

The old platform services found under `EFAMSStudioServices/services` are:

| Old service | Current Java equivalent |
| --- | --- |
| `administration` | `administration-service` |
| `audit-logging` | `audit-logging-service` |
| `case-management` | `case-management-service` |
| `configuration-management` | `configuration-management-service` |
| `gdpr` | `gdpr-service` |
| `identity` | `identity-service` |
| `ingestion-management` | `ingestion-management-service` |
| `list-management` | `list-management-service` |
| `notification-management` | `notification-management-service` |
| `report-management` | `report-management-service` |
| `rule-management` | `rule-management-service` |

The old standalone fraud detection implementation is represented in the current Java project by `fraud-detection-service`.

## What Is Present In Java Now

The current Java project has:

| Area | Current status |
| --- | --- |
| Maven monorepo | Present. |
| Java 21 / Spring Boot services | Present. |
| API Gateway | Present. |
| Local Docker infrastructure | Present. |
| PostgreSQL local provider | Present. |
| RabbitMQ event spine | Present. |
| Kafka alternative direction | Planned, not fully implemented. |
| Shared models/events/persistence modules | Present. |
| Clean service package shape | Present. |
| Contract-first management CRUD | Present for the main old management resources. |
| Rule Management CRUD | Present as dedicated Java endpoints. |
| Transaction ingestion | Present as an OEFMS transaction ingestion API and event publisher. |
| Fraud decisioning | Present for rule mode, with ML/hybrid contracts. |
| Explainable AI decision support | Present as first decision-support workflow. |
| Case/alert handoff | Present as first persisted workflow. |
| Audit logging | Present as first persisted workflow. |
| Identity provider registry | Present as a contract and in-memory foundation. |
| Full Microsoft AD/Entra/Okta/Keycloak login integration | Not yet implemented. |

## What Is Only Partially Cloned

The old ABP/.NET services expose many entity-specific application services and DTOs. The Java project currently exposes many of those resources through a generic CRUD contract:

```text
GET     /api/v1/{service}/resources/types
GET     /api/v1/{service}/resources/{resourceType}
GET     /api/v1/{service}/resources/{resourceType}/{id}
POST    /api/v1/{service}/resources/{resourceType}
PUT     /api/v1/{service}/resources/{resourceType}/{id}
DELETE  /api/v1/{service}/resources/{resourceType}/{id}
```

That makes Swagger and UI planning possible, but it is not the same as a full Java port. Each important old resource still needs to become a real Java slice when we are ready:

```text
entity
enum
repository
service
controller
Flyway migration
tests
```

## Old Details Still To Port

The following old areas are not fully cloned yet:

| Area | Gap |
| --- | --- |
| Entity-specific DTOs | Old create/update/list/filter DTOs are not fully recreated in Java. |
| Entity-specific validation | Old ABP constants and validation constraints are not fully ported. |
| PostgreSQL migrations | Many old EF Core tables are not yet recreated as Flyway migrations. |
| Approval workflows | Generic approval resources exist, but old approval actions and state transitions are not fully ported. |
| Case workflows | Assign, reassign, escalate, close, reopen, event history, account statement, and export actions need fuller Java equivalents. |
| Compliance/STR workflows | Reviewer queue, supervisor queue, reporting desk, STR recommendation, branch inquiry, XML generation, surveillance, and historical explorer actions are not yet ported. |
| Attachments | Upload/download/blob storage behavior is not yet fully ported. |
| Encumbrances | PND/lien place and lift actions are not yet fully ported. |
| List import | CSV sample/download/import behavior is not yet fully ported. |
| Data-source testing | Old test-connection and ingestion query-test behavior needs typed Java endpoints. |
| Notification workflows | Template variables, send audits, configuration audits, escalation emails, and dispatch behavior are not fully ported. |
| Reporting workflows | Dashboard metrics, generated reports, report export, and background report jobs are not fully ported. |
| Old fraud enrichment models | Account, customer, beneficiary, whitelist, blacklist, event aggregation, data-point aggregation, and generic list enrichment tables are not fully ported. |
| Authorization parity | Old ABP permission model is mapped only at foundation level. |
| Multi-tenant identity integrations | Provider registry exists, but real tenant IdP adapters and token validation flows are still future work. |

## Decision

We should not call the current Java project a complete clone yet.

The better status is:

```text
The Java project has the service foundation, Swagger-visible CRUD contracts, and first real workflow slices.
The old .NET system remains the reference source for detailed entity models, DTOs, validations, migrations, and workflow behavior.
```

## Recommended Next Porting Order

1. Convert the generic management resources into real typed Java slices one service at a time.
2. Start with UI-critical configuration/list/rule resources because they unblock admin screens.
3. Then deepen case-management workflows because they drive investigator experience.
4. Then port notification/reporting workflows because they depend on case and configuration data.
5. Then port old fraud enrichment models and connect them to ML/rule/hybrid scoring.
6. Add real tenant identity-provider integration after the local identity/auth contracts settle.

This keeps the MVP moving while preserving a clean architecture for contributors.
