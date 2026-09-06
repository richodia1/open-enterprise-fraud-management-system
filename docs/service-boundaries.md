# Service Boundaries

The boundaries below are placeholders for discussion before implementation.

| Module | Planned Responsibility |
| --- | --- |
| API Gateway | Routing, request correlation, authentication integration, rate limiting, and edge concerns. |
| Auth Service | Authentication, token issuance, user access integration, client applications, roles, permissions, and future OIDC/OAuth2 flows. |
| Fraud Detection Service | Fraud-check orchestration, risk decisioning, scoring, and decision publication. |
| Rule Management Service | Fraud/AML rule configuration and lifecycle management. |
| List Management Service | Watchlist, blacklist, whitelist, sanctions, and internal list management. |
| Case Management Service | Case creation, assignment, investigation workflow, and status history. |
| Common Models | Shared generic primitives only. |
| Common Events | Shared event contract primitives only. |

Keep shared modules small. Business logic should live in the owning service.
