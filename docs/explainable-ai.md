# Explainable AI

The Explainable AI Service is a decision-support boundary for fraud investigators and operational users.

It should not be the final authority that labels a transaction as fraud. Its role is to explain the evidence behind a fraud decision so a human user can understand what happened before confirming, rejecting, or escalating the case.

## Intended Flow

```text
Ingestion Management Service
    |
    v
Fraud Detection Service
    |
    v
Explainable AI Service
    |
    v
Case Management Service
    |
    v
Investigator final decision
```

## Current Implementation

The service now consumes `fraud.decision.produced` events and prepares an investigator-facing explanation pack.

The first implementation is deterministic decision support:

- Converts rule hits into `RULE` evidence items.
- Converts model signals into `MODEL_SIGNAL` evidence items when present.
- Adds baseline evidence when no elevated rule or model evidence is present.
- Preserves tenant, correlation, causation, and trace metadata.
- Publishes `explanation.prepared` for downstream case and audit workflows.
- Publishes `fraud.feedback.ready` for transaction-core consumers.

This is not production ML explainability yet. It is the first clear explanation boundary that later SHAP, counterfactual, model-card, and feature-contribution work can extend.

See [Explanation Decision Support](explanation-decision-support.md) for implementation details.

## Future Responsibilities

- Explain matched fraud rules in plain language.
- Summarise model signals that contributed to a risk score.
- Show feature contributions such as amount, velocity, location, channel, device, beneficiary, account age, and previous behaviour.
- Highlight missing or low-quality data that may reduce confidence.
- Produce an evidence pack for case review.
- Publish explanation events for audit logging.
- Support multiple explanation methods over time, including rule-based narratives, feature contribution summaries, counterfactual hints, and model-specific explainability techniques.

## Important Boundary

Fraud Detection Service owns risk scoring and decision orchestration.

Explainable AI Service owns reasoning support and explanation generation.

Case Management Service owns the investigator workflow and final user action.

This separation keeps the platform auditable and makes it easier for financial institutions to adopt different AI or model-serving stacks later.
