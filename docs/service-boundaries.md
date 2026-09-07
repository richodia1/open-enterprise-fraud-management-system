# Service Boundaries

The boundaries below are placeholders for discussion before implementation.

| Module | Planned Responsibility |
| --- | --- |
| API Gateway | Routing, request correlation, authentication integration, rate limiting, and edge concerns. |
| Auth Service | Authentication, token issuance, user access integration, client applications, roles, permissions, and future OIDC/OAuth2 flows. |
| Identity Service | User, tenant, profile, and identity lifecycle integration outside token issuance. |
| Administration Service | Administrative operations, operational settings, tenant administration, and back-office workflows. |
| Configuration Management Service | Runtime configuration, feature flags, service settings, and fraud platform policy configuration. |
| Ingestion Management Service | Transaction, customer, account, channel, and event ingestion from banking and payment systems. |
| Fraud Detection Service | Fraud-check orchestration, risk decisioning, scoring, and decision publication. |
| Rule Management Service | Fraud/AML rule configuration and lifecycle management. |
| List Management Service | Watchlist, blacklist, whitelist, sanctions, and internal list management. |
| Case Management Service | Case creation, assignment, investigation workflow, and status history. |
| Audit Logging Service | Tamper-aware audit capture, event trails, operational history, and compliance evidence. |
| Notification Management Service | Alert, case, workflow, and operational notification delivery. |
| Report Management Service | Operational, compliance, investigation, and fraud analytics reporting. |
| GDPR Service | Data privacy workflows, retention support, subject requests, and regulatory data handling. |
| Common Models | Shared generic primitives only. |
| Common Events | Shared event contract primitives only. |

Keep shared modules small. Business logic should live in the owning service.
