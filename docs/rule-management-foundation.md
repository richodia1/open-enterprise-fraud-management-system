# Rule Management Foundation

Rule Management now exposes the first configurable rule catalog for OEFMS.

This slice is deliberately small. It provides shared rule contracts, seeded baseline rule definitions, validation, an in-memory repository, and REST endpoints. It does not add rule persistence, approval workflow, authoring UI, or live Fraud Detection integration yet.

## Service Endpoint

Rule Management Service exposes:

```text
GET /api/v1/rules
GET /api/v1/rules/{ruleCode}
POST /api/v1/rules
PUT /api/v1/rules/{ruleCode}
PATCH /api/v1/rules/{ruleCode}/status
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

## Current Boundaries

- Rule definitions are in memory and reset on restart.
- Fraud Detection still uses its local rule configuration and baseline rule engine.
- The next rule-related step is to let Fraud Detection read active rule definitions through a service boundary or cached catalog adapter.
- Persistence should be added inside Rule Management Service with its own Flyway migrations when the lifecycle model is settled.
