# Rule Management Foundation

Rule Management now exposes the first configurable rule catalog for OEFMS.

This slice provides shared rule contracts, seeded baseline rule definitions, validation, in-memory repositories, REST endpoints, and the first UI-facing configuration CRUD surface. It does not add rule persistence, authoring UI, or live Fraud Detection integration yet.

## Code Layout

The service code is separated by responsibility:

| Package | Responsibility |
| --- | --- |
| `controller` | Versioned REST API endpoints and HTTP error mapping. |
| `dto` | Create, update, approval, and audit request payloads. |
| `entity` | Rule admin configuration records owned by Rule Management. |
| `enums` | Approval and audit lifecycle values. |
| `repository` | Repository ports, in-memory adapters, and baseline seed rules. |
| `service` | Rule catalog workflow, maker-checker decisions, validation, and audit trail behavior. |

## Service Endpoint

Rule Management Service exposes:

```text
GET /api/v1/rules
GET /api/v1/rules/{ruleCode}
POST /api/v1/rules
PUT /api/v1/rules/{ruleCode}
PATCH /api/v1/rules/{ruleCode}/status
DELETE /api/v1/rules/{ruleCode}
POST /api/v1/rules/{ruleCode}/submit-for-approval
GET /api/v1/rule-approvals/pending
POST /api/v1/rule-approvals/{approvalId}/approve
POST /api/v1/rule-approvals/{approvalId}/reject
GET /api/v1/rule-categories
GET /api/v1/rule-categories/{id}
POST /api/v1/rule-categories
PUT /api/v1/rule-categories/{id}
DELETE /api/v1/rule-categories/{id}
GET /api/v1/rule-escalation-users
GET /api/v1/rule-escalation-users/{id}
POST /api/v1/rule-escalation-users
PUT /api/v1/rule-escalation-users/{id}
DELETE /api/v1/rule-escalation-users/{id}
GET /api/v1/rule-definitions
GET /api/v1/rule-definitions/{id}
POST /api/v1/rule-definitions
PUT /api/v1/rule-definitions/{id}
DELETE /api/v1/rule-definitions/{id}
GET /api/v1/rule-audit-trails
GET /api/v1/rule-audit-trails/{id}
POST /api/v1/rule-audit-trails
PUT /api/v1/rule-audit-trails/{id}
DELETE /api/v1/rule-audit-trails/{id}
```

The default tenant is:

```text
global
```

The list endpoint supports case-insensitive filters:

```text
GET /api/v1/rules?tenantId=global&status=active&category=amount
```

## Shared Contracts

Rule contracts live in `common-models`:

| Contract | Purpose |
| --- | --- |
| `FraudRuleDefinition` | Rule catalog definition returned by the Rule Management API. |
| `FraudRuleCategory` | Rule grouping for filtering and governance. |
| `FraudRuleStatus` | `DRAFT`, `ACTIVE`, or `INACTIVE` lifecycle state. |

`FraudRuleDefinition` includes:

- Stable `ruleId`.
- `tenantId` and `ruleCode`.
- Human-readable name and explanation.
- Category, status, severity, and risk-score contribution.
- Configurable string parameters.
- Version, updated timestamp, and updater reference.

## Seeded Baseline Rules

The in-memory catalog starts with the rule codes already emitted by Fraud Detection:

| Rule Code | Category | Status |
| --- | --- | --- |
| `HIGH_VALUE_AMOUNT` | `AMOUNT` | `ACTIVE` |
| `CRITICAL_VALUE_AMOUNT` | `AMOUNT` | `ACTIVE` |
| `VELOCITY_REVIEW_THRESHOLD` | `VELOCITY` | `ACTIVE` |
| `VELOCITY_BLOCK_THRESHOLD` | `VELOCITY` | `ACTIVE` |
| `CROSS_BORDER_PARTIES` | `GEOGRAPHY` | `ACTIVE` |
| `HIGH_RISK_HINT` | `SIGNAL` | `ACTIVE` |
| `UNKNOWN_TRANSACTION_CHANNEL` | `CHANNEL` | `ACTIVE` |

## Request Shape

Creating or replacing a rule uses:

```json
{
  "tenantId": "global",
  "ruleCode": "CUSTOM_RULE",
  "ruleName": "Custom rule",
  "category": "SIGNAL",
  "status": "ACTIVE",
  "severity": "HIGH",
  "contribution": 0.25,
  "explanation": "Custom rule explanation.",
  "parameters": {
    "source": "manual"
  },
  "updatedBy": "analyst"
}
```

Updating only the status uses:

```json
{
  "tenantId": "global",
  "status": "INACTIVE",
  "updatedBy": "analyst"
}
```

Submitting a rule into the maker-checker workflow uses:

```json
{
  "tenantId": "global",
  "requestedAction": "ACTIVATE",
  "requestedBy": "maker",
  "note": "Reactivate the threshold rule after governance review."
}
```

Approving or rejecting a pending approval uses:

```json
{
  "decidedBy": "checker",
  "decisionNote": "Approved for monitoring."
}
```

Rule category configuration uses:

```json
{
  "tenantId": "global",
  "name": "Sanctions",
  "description": "Sanctions screening rules.",
  "updatedBy": "analyst"
}
```

Rule escalation user configuration uses:

```json
{
  "tenantId": "global",
  "ruleCode": "HIGH_VALUE_AMOUNT",
  "email": "fraud.team@example.com",
  "userId": "user-001",
  "name": "Fraud Team",
  "updatedBy": "analyst"
}
```

Rule condition definitions map the older rule-builder condition rows into the Java API:

```json
{
  "tenantId": "global",
  "ruleCode": "HIGH_VALUE_AMOUNT",
  "operator": "GREATER_THAN",
  "datapointValue": "10000",
  "groupId": "group-1",
  "groupConnector": "AND",
  "sourceDataPointId": "source-amount",
  "sourceDataPointCode": "TRANSACTION_AMOUNT",
  "destinationDataPointCode": "CONFIGURED_LIMIT",
  "datapointDataType": "Number",
  "destinationDataType": "Number",
  "updatedBy": "analyst"
}
```

Manual audit entries are available for parity with the original admin screens:

```json
{
  "tenantId": "global",
  "ruleCode": "HIGH_VALUE_AMOUNT",
  "action": "UPDATED",
  "fromStatus": "DRAFT",
  "toStatus": "ACTIVE",
  "note": "Imported from governance board.",
  "correlationId": "corr-001",
  "createdBy": "analyst"
}
```

## Current Boundaries

- Rule definitions and admin configuration resources are in memory and reset on restart.
- The CRUD surface is intended to unblock Swagger inspection, UI design, and contract review before the persistence model is locked.
- Audit-trail write/update/delete endpoints exist only for initial parity with the original admin service. A production implementation should favour append-only audit storage.
- Fraud Detection still uses its local rule configuration and baseline rule engine.
- The next rule-related step is to let Fraud Detection read active rule definitions through a service boundary or cached catalog adapter.
- Persistence should be added inside Rule Management Service with its own Flyway migrations when the lifecycle model is settled.
