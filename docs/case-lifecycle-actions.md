# Case Lifecycle Actions

This branch adds the first investigator action layer to Case Management Service.

## Scope

- Assign an open case to an investigator.
- Pull active case assignees from Identity Service before assignment.
- Move an assigned or open case into review, escalation, final fraud confirmation, false-positive closure, or closed state.
- Store every action in append-only `fraud_case_actions` history.
- Expose lifecycle endpoints for Swagger and the React web console.

## API Contract

```text
GET   /api/v1/cases/{caseId}/actions
GET   /api/v1/cases/assignees?tenantId=tenant-a
PATCH /api/v1/cases/{caseId}/assignment
PATCH /api/v1/cases/{caseId}/status
```

Case assignees are read from Identity Service `CUSTOM_USERS` records:

```json
[
  {
    "id": "identity-user-id",
    "tenantId": "tenant-a",
    "code": "fraud-analyst-1",
    "displayName": "Fraud Analyst One",
    "email": "fraud.analyst1@example.com",
    "roles": ["FRAUD_ANALYST"],
    "active": true
  }
]
```

The assignment request should pass the selected Identity user `id` as `assigneeId`.

Assignment request:

```json
{
  "assigneeId": "investigator-1",
  "actorId": "lead-analyst",
  "note": "Taking ownership from manual review queue.",
  "attributes": {
    "queue": "manual-review"
  }
}
```

Status request:

```json
{
  "status": "FRAUD_CONFIRMED",
  "actorId": "investigator-1",
  "note": "Confirmed mule-account pattern.",
  "attributes": {
    "decision": "fraud"
  }
}
```

Both write endpoints return:

```text
CaseActionResponse
  caseFile  updated CaseRecord
  action    saved CaseActionRecord
```

## Status Model

| Status | Meaning |
| --- | --- |
| `OPEN` | Created and waiting for assignment or review. |
| `IN_REVIEW` | Investigator is actively reviewing the case. |
| `ESCALATED` | Case requires a higher authority or specialist review. |
| `FRAUD_CONFIRMED` | Investigator confirmed fraud. Terminal. |
| `FALSE_POSITIVE` | Investigator rejected the fraud suspicion. Terminal. |
| `CLOSED` | Case is closed without a fraud/false-positive label. Terminal. |

When an `OPEN` case is assigned, it automatically moves to `IN_REVIEW`. Terminal cases cannot be reassigned or moved again in this first implementation.

## Database

Migration:

```text
services/case-management-service/src/main/resources/db/migration/postgresql/V2__add_case_lifecycle_actions.sql
```

The migration adds assignment and close timestamps to `fraud_cases`, expands case and alert status constraints, and creates `fraud_case_actions`.

## Web Console

The React console now routes from the Cases queue to:

```text
/cases/{caseId}
```

The detail screen shows case overview, decision-support attributes, assignment, status/disposition, and lifecycle history. The assignment control loads active users through Case Management, which delegates the directory lookup to Identity Service.

## Identity Integration

Case Management does not own user records. It calls Identity Service using:

```text
OEFMS_CASE_IDENTITY_SERVICE_URL=http://localhost:8087
OEFMS_CASE_IDENTITY_USER_RESOURCE_TYPE=CUSTOM_USERS
OEFMS_CASE_VALIDATE_ASSIGNEES=true
```

For local demo use, Identity Service seeds a few active `tenant-a` users. In a tenant deployment, Identity Service should synchronize or proxy users from Microsoft Entra ID, Active Directory LDAP, Okta, Keycloak, OIDC, SAML, or another configured provider, while Case Management keeps using the same assignee contract.
