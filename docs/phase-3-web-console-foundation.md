# Phase 3 Web Console Foundation

Phase 3 turns the backend MVP into a usable OEFMS Web Console for investigators, administrators, configuration teams, and future contributors.

Swagger proves that contracts exist. The Web Console proves that users can actually operate the system: view cases, inspect alerts, manage rules, configure channels, maintain data points, review audit trails, and understand why a transaction was flagged.

## Goals

- Add a separate frontend app inside the monorepo.
- Route UI API traffic through the API Gateway.
- Build the first authenticated console shell.
- Provide navigation for investigator and administration workflows.
- Show the first case, alert, explanation, audit, and dashboard screens.
- Add generic CRUD screens for the contract-first management resources.
- Prepare typed screens for high-value resources such as global data points, data points, channels, event types, fraud types, lists, rules, and tenant identity providers.
- Keep the UI modular so contributors can own feature areas without stepping on each other.

## Proposed App Location

```text
apps/
  web-console/
```

The frontend should remain separate from backend services. It should call backend APIs through the API Gateway:

```text
OEFMS Web Console
        |
        v
API Gateway
        |
        +-- Auth Service
        +-- Identity Service
        +-- Configuration Management Service
        +-- Rule Management Service
        +-- List Management Service
        +-- Case Management Service
        +-- Audit Logging Service
        +-- Report Management Service
```

## Recommended Frontend Stack

| Concern | Recommendation |
| --- | --- |
| App framework | React with TypeScript and Vite. |
| Routing | React Router. |
| API data fetching | TanStack Query. |
| UI components | MUI or Ant Design. |
| Forms | React Hook Form. |
| Validation | Zod. |
| API client | Start handwritten; later generate from OpenAPI. |
| Testing | Vitest and React Testing Library. |
| End-to-end checks | Playwright after the main flows exist. |

The first console should be functional and clear, not a marketing site.

## Initial Navigation

```text
Dashboard
Cases
Alerts
Rules
Configuration
  Global Data Points
  Data Points
  Channels
  Event Types
  Fraud Types
  Payload Properties
Lists
Identity
Audit Logs
Reports
System Health
```

## First Screens

| Screen | Purpose |
| --- | --- |
| Dashboard | Show open cases, high-risk alerts, recent decisions, and system health. |
| Cases | List fraud cases and filter by tenant, priority, status, transaction id, and date. |
| Case Detail | Show case summary, alert, transaction reference, explanation, audit trail, and investigator actions. |
| Alerts | Show generated alerts and whether they have become cases. |
| Rules | Manage rules, categories, condition definitions, escalation users, approvals, and audit trails. |
| Configuration | Manage global data points, data points, channels, event types, fraud types, and payload properties. |
| Lists | Manage watchlists, blocklists, allowlists, and list item imports. |
| Identity | Manage local users, role mappings, group mappings, and tenant identity-provider records. |
| Audit Logs | Search by tenant, correlation id, resource type, resource id, and actor. |
| Reports | Show available report views before background report generation is fully ported. |

## CRUD Strategy

The Java backend currently exposes generic management-resource endpoints for many old .NET CRUD surfaces:

```text
GET     /api/v1/{service}/resources/types
GET     /api/v1/{service}/resources/{resourceType}
GET     /api/v1/{service}/resources/{resourceType}/{id}
POST    /api/v1/{service}/resources/{resourceType}
PUT     /api/v1/{service}/resources/{resourceType}/{id}
DELETE  /api/v1/{service}/resources/{resourceType}/{id}
```

The first UI can use these generic endpoints to build resource tables and forms quickly.

As important resources become real typed Java slices, the UI should move them from generic forms into dedicated screens with better validation and workflows.

## Priority Resource Screens

Build these before lower-value setup pages:

| Priority | Resource | Reason |
| --- | --- | --- |
| 1 | Cases and alerts | Core investigator workflow. |
| 2 | Rule Management | Controls how fraud decisions are produced. |
| 3 | Global Data Points and Data Points | Needed for configurable detection logic and future ML features. |
| 4 | Channels and Event Types | Needed for event ingestion and transaction classification. |
| 5 | Fraud Types | Needed for case classification and reporting. |
| 6 | Lists and List Items | Needed for whitelist, blacklist, and watchlist style detection. |
| 7 | Tenant Identity Providers | Needed for enterprise identity integration. |
| 8 | Audit Logs | Needed for investigation traceability. |
| 9 | Reports | Useful after case/list/configuration data becomes richer. |

## Branch Plan

Use one branch per focused UI step.

| Order | Branch | Purpose |
| --- | --- | --- |
| 1 | `feature/phase-3-web-console-plan` | Capture the Web Console phase plan and UI architecture direction. |
| 2 | `feature/web-console-scaffold` | Add `apps/web-console` with React, TypeScript, Vite, linting, and test setup. |
| 3 | `feature/web-console-shell` | Add app layout, navigation, route skeletons, and environment-driven API Gateway base URL. |
| 4 | `feature/web-console-api-client` | Add shared HTTP client, error handling, correlation IDs, and TanStack Query setup. |
| 5 | `feature/web-console-dashboard` | Add first operational dashboard using available backend health and summary APIs. |
| 6 | `feature/web-console-cases-alerts` | Add case and alert list/detail screens. |
| 7 | `feature/web-console-rule-management` | Add rule-management screens against the existing rule APIs. |
| 8 | `feature/web-console-generic-crud` | Add generic resource screens for management-resource endpoints. |
| 9 | `feature/web-console-configuration-screens` | Add dedicated configuration screens for data points, channels, event types, fraud types, and payload properties. |
| 10 | `feature/web-console-identity-screens` | Add tenant identity-provider, role mapping, and group mapping screens. |
| 11 | `feature/web-console-audit-reports` | Add audit-log search and first report views. |
| 12 | `feature/web-console-e2e-smoke` | Add Playwright smoke checks for the main UI flows. |

## API Gateway Requirement

The frontend should use one base URL:

```text
VITE_OEFMS_API_BASE_URL=http://localhost:8080
```

The UI should not hardcode direct service ports such as `8082`, `8087`, or `8091`. Those ports are useful for Swagger, but the product UI should depend on the Gateway contract.

## Auth And Identity Direction

The first UI can start with local development auth assumptions while the backend identity stack matures.

The Identity area should still expose the enterprise direction:

- Local user and role visibility.
- Tenant identity-provider registry.
- Microsoft Entra ID/Azure AD configuration.
- Microsoft Active Directory LDAP configuration.
- Generic OIDC, SAML, and LDAP provider records.
- Secret references only, not raw secrets.

Real login federation should be added after the Auth Service and Identity Service contracts settle.

## Out Of Scope For First UI Pass

- Full design system polish.
- Full report designer.
- Complex role-based frontend authorization.
- Production identity federation login.
- Case attachment upload/download.
- STR XML generation.
- Full compliance workflow.
- Real-time websocket updates.

These should come later after the first console is usable.

## Exit Criteria

Phase 3 foundation is complete when:

- `apps/web-console` exists and runs locally.
- The console uses the API Gateway base URL from environment configuration.
- The main navigation is present.
- Dashboard, cases, alerts, rules, configuration, identity, audit, and reports routes exist.
- Case and alert read screens call real backend APIs.
- Rule-management screens call real backend APIs.
- Generic CRUD screens can list resource types and perform create/update/delete against the management-resource endpoints.
- The UI build passes.
- Basic component tests pass.
- A local smoke check confirms the console loads and at least one backend-backed screen renders data.
