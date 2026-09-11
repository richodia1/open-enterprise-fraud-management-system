# ML Threshold Calibration And Comparison

This phase adds a repeatable model-comparison path around the prepared Bank Account Fraud Dataset Suite feature matrix.

The goal is to support academic analysis beyond a single model metric. Fraud datasets are highly imbalanced, so OEFMS should report ranking quality, threshold tradeoffs, calibration behaviour, and investigator workload implications.

## Scope

The comparison path evaluates:

- Logistic regression as a simple interpretable baseline.
- XGBoost as the first gradient-boosted tree baseline.
- Random forest as an optional additional benchmark.

The script lives at:

```text
ml/experiments/model-comparison/compare_tabular_models.py
```

## Inputs

Prepare the BAF feature matrix first:

```bash
./scripts/prepare-ml-dataset.sh bank_account_fraud_neurips_2022
```

The script reads:

```text
data/processed/oefms-feature-matrix.csv
ml/artifacts/dataset-prep/manifest.json
```

Those files are local and ignored by Git.

## Run

Validate the inputs without optional ML dependencies:

```bash
python3 ml/experiments/model-comparison/compare_tabular_models.py --validate-only
```

Run the default model comparison after installing `ml/requirements.txt`:

```bash
.venv/bin/python ml/experiments/model-comparison/compare_tabular_models.py
```

Run a quick sampled comparison:

```bash
.venv/bin/python ml/experiments/model-comparison/compare_tabular_models.py \
  --sample-rows 10000 \
  --xgb-n-estimators 25
```

## Outputs

Generated outputs are written under ignored `ml/artifacts/model-comparison/`:

```text
model-comparison.csv
threshold-operating-points.csv
calibration-bins.csv
comparison-report.md
<model-name>-metrics.json
```

## Metrics

The comparison report includes:

- PR-AUC for imbalanced fraud-ranking quality.
- ROC-AUC for broad ranking comparison.
- Brier score for probability calibration quality.
- Fixed-threshold precision, recall, F1, and false-positive rate.
- Best-F1 threshold for balanced precision/recall tradeoff.
- Precision-target thresholds for investigator workload control.
- Calibration bins comparing average predicted score with observed fraud rate.

Accuracy is intentionally not used as a primary metric because the BAF label distribution is highly imbalanced.

## First Local BAF Comparison

The first full local run used the Bank Account Fraud Dataset Suite `Base.csv` feature matrix with 1,000,000 rows and 66 training features.

Command:

```bash
.venv/bin/python ml/experiments/model-comparison/compare_tabular_models.py \
  --output-dir ml/artifacts/model-comparison-baf-full
```

Runtime was about 48 seconds on the local development machine.

### Model Metrics

| Model | Split | PR-AUC | ROC-AUC | Brier | Fixed Precision | Fixed Recall | Best F1 | Best F1 Threshold |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| Logistic regression | Validation | 0.1505 | 0.8762 | 0.0981 | 0.0629 | 0.6946 | 0.2316 | 0.8667 |
| Logistic regression | Test | 0.1637 | 0.8792 | 0.0854 | 0.0822 | 0.6740 | 0.2421 | 0.8132 |
| XGBoost | Validation | 0.1440 | 0.8740 | 0.1480 | 0.0486 | 0.7638 | 0.2297 | 0.8063 |
| XGBoost | Test | 0.1615 | 0.8769 | 0.1429 | 0.0573 | 0.7756 | 0.2389 | 0.7993 |

### Precision Target Points

| Model | Split | Target Precision | Threshold | Precision | Recall | F1 |
| --- | --- | --- | --- | --- | --- | --- |
| Logistic regression | Test | 0.50 | 0.9673 | 0.5000 | 0.0253 | 0.0481 |
| Logistic regression | Test | 0.70 | 0.9774 | 0.7143 | 0.0117 | 0.0230 |
| Logistic regression | Test | 0.80 | 0.9852 | 0.8000 | 0.0037 | 0.0075 |
| XGBoost | Test | 0.50 | 0.8948 | 0.5000 | 0.0230 | 0.0439 |
| XGBoost | Test | 0.70 | 0.9100 | 0.7037 | 0.0089 | 0.0176 |
| XGBoost | Test | 0.80 | 0.9128 | 0.8462 | 0.0052 | 0.0102 |
| XGBoost | Test | 0.90 | 0.9239 | 1.0000 | 0.0014 | 0.0028 |

### Interpretation

The first comparison shows that a simpler logistic regression baseline is competitive with the initial XGBoost configuration on this feature matrix. Logistic regression produced slightly higher test PR-AUC, ROC-AUC, and a lower Brier score, while XGBoost produced higher recall at the uncalibrated `0.5` threshold.

This is a useful paper result because it justifies a formal baseline comparison rather than assuming the more complex model is automatically better. The next research step should add calibrated probabilities, feature leakage checks, fairness analysis across BAF variants, and tuned XGBoost hyperparameters.

## Runtime Boundary

This remains an offline research workflow. Production use still requires a governed scorer adapter that maps model scores, thresholds, and feature explanations into the Java `MlScoringClient` contract.
