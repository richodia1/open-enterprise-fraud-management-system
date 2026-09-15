# Legacy CRUD Contract Map

This document tracks the first Java contract pass for the UI-facing CRUD surfaces found in the legacy .NET/ABP services.

The current implementation is contract-first and in-memory. It is intended to make the Swagger surface visible while the project is still in MVP foundation mode. PostgreSQL/JPA persistence, richer entity-specific validation, and workflow-specific endpoints can be added resource by resource.

## Shared Management Resource Contract

Most legacy setup screens are exposed through a service-owned resource controller:

```text
GET     /api/v1/{service}/resources/types
GET     /api/v1/{service}/resources/{resourceType}
GET     /api/v1/{service}/resources/{resourceType}/{id}
POST    /api/v1/{service}/resources/{resourceType}
PUT     /api/v1/{service}/resources/{resourceType}/{id}
DELETE  /api/v1/{service}/resources/{resourceType}/{id}
```

Each service owns its enum of supported resource types. Request bodies use `ManagedResourceRequest`, and responses use `ManagedResourceRecord`.

## Resource Coverage

| Java service | Resource types mapped from the old system |
| --- | --- |
| Administration | `PERMISSION_DEFINITIONS`, `PERMISSION_GRANTS`, `ROLES`, `ROLE_GROUPS` |
| Identity | `CUSTOM_USERS`, `TENANT_IDENTITY_GROUP_MAPPINGS`, `TENANT_IDENTITY_ROLE_MAPPINGS` |
| Configuration Management | `APPROVALS`, `BANK_FRAUD_DESK_EMAILS`, `CASE_ASSIGNMENT_POLICIES`, `CASE_ESCALATION_POLICIES`, `CHANNELS`, `DATA_POINTS`, `EVENT_TYPES`, `FRAUD_TYPES`, `GLOBAL_CHANNEL_TYPES`, `GLOBAL_DATA_POINTS`, `GLOBAL_EVENT_TYPES`, `PAYLOAD_PROPERTIES` |
| Ingestion Management | `DATA_SOURCES`, `INGESTION_JOBS`, `INGESTION_JOB_LEASES`, `INGESTION_JOB_OUTPUT_STREAM_SETTINGS`, `INGESTION_JOB_RUN_QUERY_LOGS`, `INGESTION_JOB_TEST_RUNS` |
| List Management | `APPROVALS`, `LIST_ITEMS`, `LIST_TYPES` |
| Case Management | `ACCOUNT_TRANSACTION_HISTORY`, `APPROVALS`, `ATTACHMENTS`, `CASE_ATTACHMENTS`, `CASE_AUDITS`, `CASE_COMMENTS`, `CASE_DETAILS`, `CASE_TASKS`, `CLOSURE_REASON_LOOKUPS`, `COMPLIANCE_CASES`, `ENCUMBRANCES`, `FLAGGED_CASE_RULES`, `FRAUD_CASES`, `INQUIRY_TYPE_LOOKUPS`, `NOTIFICATION_OUTBOXES`, `PREDICATE_OFFENCE_LOOKUPS`, `STR_INDICATOR_LOOKUPS` |
| Notification Management | `AUDIENCE_CONFIGURATIONS`, `AUDIENCE_ESCALATION_EMAILS`, `NOTIFICATION_AUDIENCE_TYPES`, `NOTIFICATION_CONFIGURATION_AUDITS`, `NOTIFICATION_CONFIGURATIONS`, `NOTIFICATION_MESSAGE_FORMATS`, `NOTIFICATION_MODES`, `NOTIFICATION_SEND_AUDITS`, `NOTIFICATION_TEMPLATES`, `TEMPLATE_VARIABLE_OPTIONS` |
| Report Management | `CLOSED_CASES`, `DASHBOARD_METRICS`, `EVENT_TRENDS`, `FRAUD_DASHBOARD_METRICS`, `GENERATED_REPORTS`, `LIST_REPORTS` |

Rule Management already has dedicated CRUD endpoints for rules, rule definitions, categories, escalation users, audit trails, and approvals.

## Identity Provider Integration Boundary

Identity Service also exposes a typed tenant identity-provider API:

```text
GET     /api/v1/identity/tenant-identity-providers/provider-types
GET     /api/v1/identity/tenant-identity-providers
GET     /api/v1/identity/tenant-identity-providers/{id}
POST    /api/v1/identity/tenant-identity-providers
PUT     /api/v1/identity/tenant-identity-providers/{id}
DELETE  /api/v1/identity/tenant-identity-providers/{id}
```

The provider type catalog includes Microsoft Entra ID/Azure AD, Microsoft Active Directory LDAP, Okta, Ping Identity, Keycloak, Auth0, generic OIDC, generic SAML, and generic LDAP.

Secrets must not be stored in these API records. The contract stores secret references such as a vault path, so deployments can later plug in their preferred secret manager.
