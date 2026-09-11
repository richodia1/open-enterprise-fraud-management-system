# Model Comparison Experiment

This experiment compares tabular fraud models on the same prepared OEFMS feature matrix. It is intended for academic evaluation and model-governance discussion, not direct production deployment.

## Input

Prepare the Bank Account Fraud Dataset Suite first:

```bash
./scripts/prepare-ml-dataset.sh bank_account_fraud_neurips_2022
```

The comparison script reads:

```text
data/processed/oefms-feature-matrix.csv
ml/artifacts/dataset-prep/manifest.json
```

## Validate Inputs

Validation does not import the optional ML dependencies:

```bash
python3 ml/experiments/model-comparison/compare_tabular_models.py --validate-only
```

## Run Comparison

Install the optional ML stack:

```bash
python3 -m venv .venv
source .venv/bin/activate
python3 -m pip install -r ml/requirements.txt
```

Run the default comparison:

```bash
.venv/bin/python ml/experiments/model-comparison/compare_tabular_models.py
```

Write validation/test score files for the audit workflow:

```bash
.venv/bin/python ml/experiments/model-comparison/compare_tabular_models.py --write-scores
```

The default compares:

```text
logistic_regression
xgboost
```

Random forest can be included explicitly:

```bash
.venv/bin/python ml/experiments/model-comparison/compare_tabular_models.py \
  --models logistic_regression,random_forest,xgboost
```

For quick local iteration:

```bash
.venv/bin/python ml/experiments/model-comparison/compare_tabular_models.py \
  --sample-rows 10000 \
  --xgb-n-estimators 25
```

## Outputs

Outputs stay under ignored `ml/artifacts/`:

```text
ml/artifacts/model-comparison/
  logistic_regression-metrics.json
  xgboost-metrics.json
  model-comparison.csv
  threshold-operating-points.csv
  calibration-bins.csv
  comparison-report.md
  logistic_regression-validation-scores.csv
  logistic_regression-test-scores.csv
  xgboost-validation-scores.csv
  xgboost-test-scores.csv
```

Score files are only written when `--write-scores` is supplied.

The report focuses on PR-AUC, ROC-AUC, Brier score, fixed-threshold metrics, best-F1 thresholds, precision-target operating points, and calibration bins. This makes the experiment useful for fraud-model papers where class imbalance and investigator workload are central concerns.
