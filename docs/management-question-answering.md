# Management Question Answering

Explainable AI Service now exposes the first fraud-intelligence question contract:

```text
POST /api/v1/explanations/management/questions
```

This is the foundation for management-facing questions such as:

- What is the latest fraud case?
- How many fraud cases came from ATM this year?
- How many fraud cases resulted from a specific rule or fraud type?
- Explain this case to management.
- Summarise fraud trends for this period.

## MVP Position

The first implementation is intentionally safe and deterministic. It classifies the question, returns an answer status, and includes the structured data plan needed to ground the response.

Counts, latest-case queries, and trends should be answered from structured services such as Case Management, Fraud Detection, and Audit Logging. Narrative case explanations can use supplied case context and explanation evidence.

## Next Step

The next hardening step is to add data adapters that execute the returned query plan, then optionally add a vector index for case notes, policies, decision explanations, and audit narratives.
