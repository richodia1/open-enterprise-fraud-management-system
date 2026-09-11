# ML Leakage And Fairness Audit

This phase adds a repeatable audit workflow for the prepared Bank Account Fraud Dataset Suite feature matrix and optional model prediction scores.

The goal is to move OEFMS model research beyond aggregate PR-AUC and ROC-AUC. A fraud model also needs leakage review, slice-level behavior checks, and evidence that investigators can understand before using the score as decision support.

## Scope

The audit workflow checks:

- Accidental label-like feature names.
- Fraud-named features that require availability review.
- Constant features that add no signal in the current dataset.
- Unusually high label correlations that may indicate leakage or proxy risk.
- Fraud-rate and fraud-lift differences across interpretable data slices.
- Model precision, recall, false-positive rate, and predicted-positive rate by slice.

The script lives at:

```text
ml/experiments/model-audit/audit_dataset_and_predictions.py
```

## Inputs

Prepare the BAF feature matrix first:

```bash
./scripts/prepare-ml-dataset.sh bank_account_fraud_neurips_2022
```

Generate model score files from the comparison workflow:

```bash
.venv/bin/python ml/experiments/model-comparison/compare_tabular_models.py \
  --output-dir ml/artifacts/model-comparison-baf-full \
  --write-scores
```

The audit reads:

```text
data/processed/oefms-feature-matrix.csv
ml/artifacts/dataset-prep/manifest.json
ml/artifacts/model-comparison-baf-full/*-scores.csv
```

Those files are local and ignored by Git.

## Run

Validate the inputs without optional ML dependencies:

```bash
python3 ml/experiments/model-audit/audit_dataset_and_predictions.py --validate-only
```

Run the full audit after installing `ml/requirements.txt`:

```bash
.venv/bin/python ml/experiments/model-audit/audit_dataset_and_predictions.py \
  --scores-dir ml/artifacts/model-comparison-baf-full \
  --output-dir ml/artifacts/model-audit-baf-full
```

## Outputs

Generated outputs are written under ignored `ml/artifacts/model-audit/`:

```text
audit-report.md
dataset-slice-summary.csv
feature-leakage-flags.csv
prediction-slice-metrics.csv
```

## First Local BAF Audit

The first full local audit used the Bank Account Fraud Dataset Suite `Base.csv` feature matrix with 1,000,000 rows, 11,029 fraud labels, and 66 training features.

Command:

```bash
.venv/bin/python ml/experiments/model-audit/audit_dataset_and_predictions.py \
  --scores-dir ml/artifacts/model-comparison-baf-full \
  --output-dir ml/artifacts/model-audit-baf-full
```

The audit generated 16 slice dimensions, 15 leakage/review flags, and 200 prediction-slice metric rows.

### Leakage Review

No critical high-correlation leakage flag was found in the first run.

The main review flag was `raw__device_fraud_count`, because the feature name contains fraud terminology. This may be valid if it is a historical count known before the application decision, but it must be reviewed before the feature is used in a production model.

Most other flags were low-severity constant features caused by the BAF `Base.csv` mapping defaults, such as `channel__WEB`, `direction__INBOUND`, `currency__USD`, and `transaction_type__ACCOUNT_OPENING`.

### Highest Fraud-Rate Slices

Minimum sample for this report was 1,000 rows and 10 positive labels.

| Slice | Value | Rows | Fraud Rate | Lift |
| --- | --- | ---: | ---: | ---: |
| housing_status | BA | 169,675 | 3.75% | 3.40 |
| customer_age_band | 60+ | 42,660 | 3.47% | 3.15 |
| credit_risk_score_band | 200+ | 173,937 | 2.61% | 2.37 |
| device_os | WINDOWS | 263,506 | 2.47% | 2.24 |
| employment_status | CC | 37,758 | 2.47% | 2.24 |
| foreign_request | YES | 25,242 | 2.20% | 1.99 |
| income_band | 0.80+ | 221,419 | 2.16% | 1.96 |
| customer_age_band | 50-59 | 140,353 | 2.00% | 1.81 |
| keep_alive_session | NO | 423,053 | 1.72% | 1.56 |
| payment_type | AC | 252,071 | 1.67% | 1.51 |

### Prediction Slice Hotspots

At the uncalibrated `0.5` threshold, XGBoost marked some high-lift slices at a very high rate. That is useful for recall research, but it also shows why thresholds and calibration must be reviewed before investigator-facing use.

| Model | Split | Slice | Value | Rows | Predicted Positive Rate | Recall | FPR |
| --- | --- | --- | --- | ---: | ---: | ---: | ---: |
| XGBoost | Validation | housing_status | BA | 26,050 | 71.58% | 97.37% | 70.48% |
| XGBoost | Test | housing_status | BA | 29,274 | 69.23% | 96.65% | 67.91% |
| Logistic regression | Validation | housing_status | BA | 26,050 | 47.48% | 88.91% | 45.72% |
| XGBoost | Validation | device_os | WINDOWS | 37,169 | 44.78% | 91.16% | 43.43% |
| XGBoost | Test | device_os | WINDOWS | 37,662 | 42.49% | 90.74% | 40.92% |

## Interpretation

This first audit gives OEFMS a stronger academic and governance story:

- The project can explain which dataset segments carry elevated fraud risk.
- The project can identify feature availability questions before production use.
- The project can compare model behavior by segment instead of trusting one aggregate metric.
- The project can show why uncalibrated `0.5` thresholds are not enough for operational fraud review.

This is still an offline research workflow. It should inform model cards, threshold selection, explainable AI output, and governance review before any model is used in live decisioning.
