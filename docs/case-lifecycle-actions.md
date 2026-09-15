# Case Lifecycle Actions

This branch adds the first investigator action layer to Case Management Service.

## Scope

- Assign an open case to an investigator.
- Pull active case assignees from Identity Service before assignment.
- Pull case assignment and escalation role policy from Configuration Management Service.
- Move an assigned or open case into review, escalation, final fraud confirmation, false-positive closure, or closed state.
- Store every action in append-only `fraud_case_actions` history.
- Expose lifecycle endpoints for Swagger and the React web console.

## API Contract

```text
GET   /api/v1/cases/{caseId}/actions
GET   /api/v1/cases/assignees?tenantId=tenant-a&purpose=ASSIGNMENT
GET   /api/v1/cases/assignees?tenantId=tenant-a&purpose=ESCALATION
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
    "eligiblePurposes": ["ASSIGNMENT"],
    "active": true
  }
]
```

The assignment request should pass the selected Identity user `id` as `assigneeId`. `purpose` is optional and defaults to `ASSIGNMENT`.

Assignment request:

```json
{
  "assigneeId": "investigator-1",
  "actorId": "lead-analyst",
  "note": "Taking ownership from manual review queue.",
  "purpose": "ASSIGNMENT",
  "attributes": {
    "queue": "manual-review"
  }
}
```

Escalation request:

```json
{
  "assigneeId": "fraud-supervisor-1",
  "actorId": "lead-analyst",
  "note": "Escalating for supervisor review.",
  "purpose": "ESCALATION",
  "attributes": {
    "queue": "supervisor-review"
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

When an `OPEN` case is assigned for normal assignment, it automatically moves to `IN_REVIEW`. When a case is assigned for escalation, it moves to `ESCALATED`. Terminal cases cannot be reassigned or moved again in this first implementation.

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

## Identity And Configuration Integration

Case Management does not own user records or tenant-specific assignment policy. It calls Identity Service for active users and Configuration Management Service for role policy.

Identity settings:

```text
OEFMS_CASE_IDENTITY_SERVICE_URL=http://localhost:8087
OEFMS_CASE_IDENTITY_USER_RESOURCE_TYPE=CUSTOM_USERS
OEFMS_CASE_VALIDATE_ASSIGNEES=true
```

Configuration settings:

```text
OEFMS_CASE_CONFIGURATION_SERVICE_URL=http://localhost:8088
OEFMS_CASE_ASSIGNMENT_POLICY_RESOURCE_TYPE=CASE_ASSIGNMENT_POLICIES
OEFMS_CASE_CONFIGURATION_ENABLED=true
OEFMS_CASE_REQUIRE_ASSIGNMENT_POLICY=false
OEFMS_CASE_FALLBACK_ASSIGNMENT_ROLES=FRAUD_ANALYST,FRAUD_SUPERVISOR
OEFMS_CASE_FALLBACK_ESCALATION_ROLES=FRAUD_SUPERVISOR,FRAUD_ESCALATION_REVIEWER,COMPLIANCE_OFFICER
```

Configuration Management exposes the policy records through:

```text
GET /api/v1/configuration-management/resources/CASE_ASSIGNMENT_POLICIES?tenantId=tenant-a&active=true
```

The default policy attributes are:

```json
{
  "assignmentRoles": "FRAUD_ANALYST,FRAUD_SUPERVISOR",
  "escalationRoles": "FRAUD_SUPERVISOR,FRAUD_ESCALATION_REVIEWER,COMPLIANCE_OFFICER"
}
```

For local demo use, Identity Service seeds a few active `tenant-a` users. In a tenant deployment, Identity Service should synchronize or proxy users from Microsoft Entra ID, Active Directory LDAP, Okta, Keycloak, OIDC, SAML, or another configured provider, while Case Management keeps using the same assignee contract.
