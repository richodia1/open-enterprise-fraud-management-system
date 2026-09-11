# ML Workspace

This folder is reserved for future fraud-detection experiments, training pipelines, feature engineering, and model evaluation.

The first experiments should focus on reproducible tabular baselines:

```text
ml/
  experiments/
  pipelines/
  notebooks/
  evaluation/
  artifacts/
```

Model artifacts should not be committed directly to Git. Use `ml/artifacts/` locally or a governed model registry later.

Runtime integration should target the Java contracts documented in `docs/ml-runtime-contract.md`. A production scorer should implement `org.openefms.frauddetection.ml.MlScoringClient` from the Fraud Detection Service and return calibrated scores plus explainable feature contributions.

Recommended first model path:

1. Prepare PaySim and Credit Card Fraud datasets locally.
2. Build logistic regression and random forest baselines.
3. Train an XGBoost baseline.
4. Evaluate with precision-recall, ROC-AUC, PR-AUC, false-positive rate, and recall at fixed precision.
5. Generate feature contribution explanations for investigator support.
