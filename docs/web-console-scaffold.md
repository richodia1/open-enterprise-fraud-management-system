# Web Console Scaffold

The first OEFMS web console lives in `apps/web-console`. It is a separate React, TypeScript, Vite, MUI, React Router, and TanStack Query project so the user interface can evolve independently from the Spring Boot service modules.

## Purpose

Swagger remains useful for contract inspection, but the product also needs a UI surface where investigators and administrators can see the system as real users will experience it. This scaffold starts that product-facing layer without changing the backend deployment model.

## Initial Scope

- Dashboard for gateway health, case volume, alert volume, rules, and recent audit traceability.
- Cases and alerts views backed by the current case-management contracts.
- Case detail view with assignment, status, final disposition, and lifecycle history.
- Identity-backed and Configuration-filtered case assignee selector for investigator assignment and escalation.
- Rule view backed by the current rule-management contract.
- Generic CRUD workspaces for configuration, list, identity, and report-management resource contracts.
- Identity provider visibility for tenant identity integrations.
- Audit-log search by tenant or correlation id.
- System-health view for gateway health and exposed route metadata.

## Runtime Contract

The console reads the API Gateway base URL from:

```text
VITE_OEFMS_API_BASE_URL
```

For local development, copy `apps/web-console/.env.example` to `apps/web-console/.env.local`.

## CI

The root CI pipeline installs the web-console dependencies with `npm ci`, then runs lint, Vitest, and a production build after the Maven and smoke checks.

## Next UI Work

- Add create/update screens for rule definitions once the rule-management API becomes fully persisted.
- Add authentication and tenant-aware navigation once the Auth and tenant identity adapters mature.
- Add charts for fraud trends and model explainability when report-management and explainable-ai storage are durable.
