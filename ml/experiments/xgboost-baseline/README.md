# XGBoost Baseline Experiment

This folder contains the first reproducible tabular ML experiment for OEFMS. It trains an XGBoost binary classifier from the prepared feature matrix and writes local, ignored model artifacts for research and runtime handoff.

## Dataset Input

For the academic baseline, use the Bank Account Fraud Dataset Suite `Base.csv` file:

```bash
./scripts/prepare-ml-dataset.sh bank_account_fraud_neurips_2022
```

For a quick local check, limit the preparation size:

```bash
./scripts/prepare-ml-dataset.sh bank_account_fraud_neurips_2022 --limit 10000
```

The pipeline writes:

```text
data/interim/oefms-standard-transactions.csv
data/processed/oefms-feature-matrix.csv
ml/artifacts/dataset-prep/manifest.json
```

The BAF mapping keeps core explainable columns such as `credit_risk_score`, `velocity_6h`, `customer_age`, `payment_type`, `employment_status`, `housing_status`, `source`, and `device_os` as model-ready `raw__...` features.

## Training

Install the optional Python training stack:

```bash
python3 -m venv .venv
source .venv/bin/activate
python3 -m pip install -r ml/requirements.txt
```

Validate the prepared feature matrix without importing ML dependencies:

```bash
python3 ml/experiments/xgboost-baseline/train_xgboost_baseline.py --validate-only
```

Train the baseline:

```bash
python3 ml/experiments/xgboost-baseline/train_xgboost_baseline.py
```

For a faster first full-dataset baseline:

```bash
python3 ml/experiments/xgboost-baseline/train_xgboost_baseline.py \
  --n-estimators 50 \
  --n-jobs -1
```

Outputs are written under the ignored folder:

```text
ml/artifacts/xgboost-baseline/
  xgboost-baseline.ubj
  metrics.json
  feature-columns.json
  feature-importance.csv
  feature-contributions-sample.csv
  model-card.md
```

The training script automatically sets `scale_pos_weight` from the training split class imbalance and evaluates validation/test data with PR-AUC, ROC-AUC, precision, recall, F1, false-positive rate, recall at the configured minimum precision, and threshold operating points.

## Explainability

The generated `feature-contributions-sample.csv` uses XGBoost contribution values to show the highest-impact features per scored record. A later production scorer adapter should map those contribution records into `MlFeatureContribution` values for investigator-facing explanations.

## Runtime Reference

The trained scorer must return results through the Java runtime contract:

```text
org.openefms.frauddetection.ml.MlScoringClient
org.openefms.common.models.ml.MlScoreRequest
org.openefms.common.models.ml.MlScoreResponse
org.openefms.common.models.ml.MlFeatureContribution
```

The Fraud Detection Service switches into ML or hybrid mode through:

```text
OEFMS_FRAUD_DECISION_MODE=ml
OEFMS_FRAUD_DECISION_MODE=hybrid
```
