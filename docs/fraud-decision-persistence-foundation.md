# Fraud Decision Persistence Foundation

This branch adds the first durable Fraud Detection Service record.

Fraud Detection Service now owns a `fraud_decisions` table in `openefms_fraud`. The record captures the decision outcome, score, risk band, evidence summary, decision attributes, and event metadata needed to trace the result through the Phase 2 workflow.

## Scope

- Add the `fraud_decisions` table through Flyway.
- Persist direct HTTP fraud decisions.
- Persist event-driven fraud decisions before publishing `fraud.decision.produced`.
- Expose read endpoints for a decision id, transaction id, and tenant id.
- Keep full rule-hit tables, model-signal tables, idempotency storage, and outbox-backed publication for later branches.

## Runtime Flow

```text
TransactionIngestionRequest
        |
        v
FraudDecisionOrchestrator
        |
        v
Rule, ML, or hybrid scoring
        |
        v
Save fraud_decisions row
        |
        v
Return HTTP decision or publish fraud.decision.produced
```

Event-driven decisions keep the produced fraud-decision event metadata on the record so later services can trace the decision back to the source `transaction.received` event.

## Database Ownership

| Item | Value |
| --- | --- |
| Service | Fraud Detection Service |
| Database | `openefms_fraud` |
| Table | `fraud_decisions` |
| Migration | `services/fraud-detection-service/src/main/resources/db/migration/postgresql/V1__create_fraud_decisions.sql` |

## API Reads

```text
GET /api/v1/fraud/decisions/{decisionId}
GET /api/v1/fraud/decisions?transactionId={transactionId}
GET /api/v1/fraud/decisions?tenantId={tenantId}
```

The response is a `FraudDecisionRecord`, not the original nested scoring response. It is shaped for traceability and early investigator support while richer evidence normalization remains open for future work.

## Next Persistence Candidates

- Explainable AI explanation records.
- Case Management case and alert records.
- Audit Logging append-only event records.
- Rule Management rule definitions and version history.
