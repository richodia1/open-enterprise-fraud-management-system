# Model Audit Experiment

This experiment audits a prepared OEFMS feature matrix and optional prediction-score files before a model is considered for `ml` or `hybrid` decision mode.

It is intended for research, model governance, and investigator decision-support analysis. It does not certify fairness by itself; it highlights issues that need review.

## Input

Prepare the Bank Account Fraud Dataset Suite feature matrix first:

```bash
./scripts/prepare-ml-dataset.sh bank_account_fraud_neurips_2022
```

To include prediction-slice metrics, generate model scores from the comparison workflow:

```bash
.venv/bin/python ml/experiments/model-comparison/compare_tabular_models.py \
  --output-dir ml/artifacts/model-comparison-baf-full \
  --write-scores
```

## Validate Inputs

Validation does not import optional ML dependencies:

```bash
python3 ml/experiments/model-audit/audit_dataset_and_predictions.py --validate-only
```

## Run Audit

Install the optional ML stack:

```bash
python3 -m venv .venv
source .venv/bin/activate
python3 -m pip install -r ml/requirements.txt
```

Run the full audit:

```bash
.venv/bin/python ml/experiments/model-audit/audit_dataset_and_predictions.py \
  --scores-dir ml/artifacts/model-comparison-baf-full \
  --output-dir ml/artifacts/model-audit-baf-full
```

## Outputs

Outputs stay under ignored `ml/artifacts/`:

```text
ml/artifacts/model-audit/
  audit-report.md
  dataset-slice-summary.csv
  feature-leakage-flags.csv
  prediction-slice-metrics.csv
```

The audit reports:

- Feature-name, constant-feature, and high-label-correlation leakage checks.
- Fraud-rate and fraud-lift summaries for interpretable BAF slices.
- Prediction behavior by model, split, threshold, and slice.
- Precision, recall, false-positive rate, and predicted-positive rate for sufficiently large slices.

## Reviewed Slices

The first BAF audit covers operationally understandable features:

```text
payment_type
employment_status
housing_status
application_source
device_os
foreign_request
free_email
phone validity
other cards
keep-alive session
customer age band
income band
credit risk score band
session length band
device fraud count band
```

Any feature containing fraud terminology should be reviewed carefully. Historical fraud-count features can be valid when available before the decision point, but future-looking or target-derived fields must be removed.
