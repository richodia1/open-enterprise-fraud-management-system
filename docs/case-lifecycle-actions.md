# Case Lifecycle Actions

This branch adds the first investigator action layer to Case Management Service.

## Scope

- Assign an open case to an investigator.
- Move an assigned or open case into review, escalation, final fraud confirmation, false-positive closure, or closed state.
- Store every action in append-only `fraud_case_actions` history.
- Expose lifecycle endpoints for Swagger and the React web console.

## API Contract

```text
GET   /api/v1/cases/{caseId}/actions
PATCH /api/v1/cases/{caseId}/assignment
PATCH /api/v1/cases/{caseId}/status
```

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

The detail screen shows case overview, decision-support attributes, assignment, status/disposition, and lifecycle history.
