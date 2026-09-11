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

The first full local BAF baseline summary is recorded in `docs/ml-xgboost-baseline-results.md`.
Threshold calibration and baseline comparison guidance is recorded in `docs/ml-threshold-calibration-and-comparison.md`.

Recommended first model path:

1. Prepare the committed synthetic sample to verify the pipeline.
2. Download Bank Account Fraud Dataset Suite `Base.csv` locally and prepare `bank_account_fraud_neurips_2022`.
3. Train the XGBoost baseline in `ml/experiments/xgboost-baseline/`.
4. Evaluate with precision-recall, ROC-AUC, PR-AUC, false-positive rate, and recall at fixed precision.
5. Generate feature contribution explanations for investigator support.
6. Compare logistic regression, XGBoost, and optional random forest with threshold and calibration reports.
7. Add LightGBM, CatBoost, fairness analysis, and leakage checks.

## Optional Training Stack

Install the Python ML dependencies only when training locally:

```bash
python3 -m venv .venv
source .venv/bin/activate
python3 -m pip install -r ml/requirements.txt
```

The repository smoke checks use standard-library validation paths, so contributors do not need the full ML stack to build the Java services.
