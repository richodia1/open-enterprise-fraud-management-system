# ML Workspace

This folder owns fraud-detection experiments, training pipelines, feature engineering, model evaluation, and runtime handoff metadata.

The first reproducible tabular baseline path is now scaffolded:

```text
ml/
  experiments/
  pipelines/
  notebooks/
  evaluation/
  artifacts/
```

Model artifacts should not be committed directly to Git. Use `ml/artifacts/` locally or a governed model registry later.

## Dataset Preparation

Run the sample pipeline:

```bash
./scripts/prepare-ml-dataset.sh oefms_synthetic_sample
```

The pipeline reads `data/catalog/fraud-datasets.json`, standardizes a raw dataset into an OEFMS transaction-like shape, engineers a feature matrix, and writes a manifest that references the Java ML runtime contracts.

The core implementation lives in:

```text
ml/pipelines/prepare_tabular_dataset.py
```

Runtime integration should target the Java contracts documented in `docs/ml-runtime-contract.md`. A production scorer should implement `org.openefms.frauddetection.ml.MlScoringClient` from the Fraud Detection Service and return calibrated scores plus explainable feature contributions.

Recommended first model path:

1. Prepare the committed synthetic sample to verify the pipeline.
2. Download PaySim or Credit Card Fraud locally and run the same preparation path.
3. Build logistic regression and random forest baselines.
4. Train an XGBoost baseline.
5. Evaluate with precision-recall, ROC-AUC, PR-AUC, false-positive rate, and recall at fixed precision.
6. Generate feature contribution explanations for investigator support.
