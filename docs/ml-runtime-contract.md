# ML Runtime Contract

OEFMS now has a Java-side ML scoring contract for Fraud Detection.

This branch does not add a trained model, Python runtime, model server, or production ML decisioning. It defines the contracts and runtime boundary so contributors can later plug in XGBoost, LightGBM, CatBoost, or another scorer without changing the fraud decision API.

Dataset preparation is now tracked separately from runtime scoring. See [ML Dataset Foundation](ml-dataset-foundation.md) for the catalog, sample dataset, feature matrix, and manifest path that will feed future trained scorers.

## Runtime Status Endpoint

Fraud Detection Service exposes:

```text
GET /api/v1/fraud/ml/runtime
```

The endpoint returns `MlRuntimeStatus`:

```json
{
  "provider": "disabled",
  "modelName": "xgboost-baseline",
  "state": "DISABLED",
  "supportedModes": ["ML", "HYBRID"],
  "reason": "ML scoring is disabled by configuration.",
  "checkedAt": "2026-09-11T09:00:00Z",
  "attributes": {
    "scoreThreshold": "0.80"
  }
}
```

Runtime states:

| State | Meaning |
| --- | --- |
| `DISABLED` | ML scoring is disabled by configuration. |
| `CONTRACT_ONLY` | A provider name is configured, but no production scorer adapter is registered. |
| `READY` | A scorer adapter is available and can score transactions. |

## Shared Contracts

ML contracts live in `common-models`:

| Contract | Purpose |
| --- | --- |
| `MlScoreRequest` | Transaction and derived feature map sent to a scorer. |
| `MlScoreResponse` | Score, risk band, recommended outcome, and explainable feature contributions. |
| `MlFeatureContribution` | Per-feature contribution for investigator-facing explanations. |
| `MlRuntimeStatus` | Runtime availability and supported decision modes. |
| `MlRuntimeState` | `DISABLED`, `CONTRACT_ONLY`, or `READY`. |

## Fraud Decision Modes

The existing decision endpoint remains:

```text
POST /api/v1/fraud/decisions
```

Mode is controlled by:

```text
OEFMS_FRAUD_DECISION_MODE=rules
```

Current behavior:

| Mode | Runtime behavior |
| --- | --- |
| `rules` | Runs the deterministic rule engine. This remains the default. |
| `ml` | Calls `MlScoringClient` and maps `MlScoreResponse` into `FraudDecisionResponse`. |
| `hybrid` | Runs rules and ML, then uses the higher risk posture from the two scores. |

The default production bean is contract-only. It reports status but throws `MlScoringUnavailableException` when asked to score.

## Future Adapter Direction

A production scorer should provide a Spring bean implementing:

```text
org.openefms.frauddetection.service.MlScoringClient
```

Expected adapter responsibilities:

- Convert `MlScoreRequest` into the model runtime input.
- Call a governed scorer boundary or load a vetted JVM-compatible artifact.
- Return calibrated score values between `0.00` and `1.00`.
- Return feature contributions suitable for Explainable AI.
- Include model name, version, dataset lineage, and runtime metadata.
- Fail closed when the scorer is unavailable.

## Current Boundaries

- No raw datasets are committed to Git.
- No model artifacts are committed to Git.
- No unvalidated model is enabled by default.
- `rules` remains the production-safe local default.
- `ml` and `hybrid` are contract-ready for contributor implementation and tests.
