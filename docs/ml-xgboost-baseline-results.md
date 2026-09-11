# XGBoost Baseline Results

This note records the first local OEFMS XGBoost baseline run against the Bank Account Fraud Dataset Suite `Base.csv`.

The raw dataset, prepared feature matrix, model artifact, metrics JSON, and contribution samples remain local under ignored folders. Do not commit those files to Git.

## Run Context

| Item | Value |
| --- | --- |
| Date | 2026-09-11 |
| Dataset | Bank Account Fraud Dataset Suite `Base.csv` |
| Catalog id | `bank_account_fraud_neurips_2022` |
| Rows | 1,000,000 |
| Legitimate rows | 988,971 |
| Fraud rows | 11,029 |
| Train rows | 700,000 |
| Validation rows | 150,000 |
| Test rows | 150,000 |
| Training features | 66 |
| Model | XGBoost binary classifier |
| Trees | 50 |
| Class weighting | `scale_pos_weight=98.375355` |

## Commands

```bash
./scripts/prepare-ml-dataset.sh bank_account_fraud_neurips_2022
.venv/bin/python ml/experiments/xgboost-baseline/train_xgboost_baseline.py \
  --output-dir ml/artifacts/xgboost-baseline-baf-full \
  --n-estimators 50 \
  --explain-sample-size 25
```

## Fixed Threshold Metrics

The default decision threshold was `0.5`.

| Split | PR-AUC | ROC-AUC | Precision | Recall | F1 | False Positive Rate |
| --- | --- | --- | --- | --- | --- | --- |
| Validation | 0.1440 | 0.8740 | 0.0486 | 0.7638 | 0.0913 | 0.1869 |
| Test | 0.1615 | 0.8769 | 0.0573 | 0.7756 | 0.1067 | 0.1842 |

## Operating Points

| Split | Strategy | Threshold | Precision | Recall | F1 |
| --- | --- | --- | --- | --- | --- |
| Validation | Best F1 | 0.8063 | 0.1971 | 0.2751 | 0.2297 |
| Validation | Minimum precision target 0.90 | 0.9209 | 1.0000 | 0.0005 | 0.0011 |
| Test | Best F1 | 0.7993 | 0.2038 | 0.2885 | 0.2389 |
| Test | Minimum precision target 0.90 | 0.9239 | 1.0000 | 0.0014 | 0.0028 |

## Top Features

| Rank | Feature | Importance |
| --- | --- | --- |
| 1 | `raw__housing_status__BA` | 0.318404912949 |
| 2 | `raw__device_os__WINDOWS` | 0.123145572841 |
| 3 | `raw__housing_status__BE` | 0.060081906617 |
| 4 | `raw__current_address_months_count` | 0.032928105444 |
| 5 | `raw__prev_address_months_count` | 0.032789833844 |
| 6 | `raw__customer_age` | 0.032402783632 |
| 7 | `raw__keep_alive_session` | 0.030945699662 |
| 8 | `raw__has_other_cards` | 0.027446070686 |
| 9 | `raw__credit_risk_score` | 0.027219880372 |
| 10 | `raw__device_distinct_emails_8w` | 0.025839248672 |

## Interpretation

The baseline is useful for academic comparison because it produces a clear ranking signal, but it is not yet a production decision model.

The fixed `0.5` threshold catches many fraud cases, but precision is low. The best-F1 operating point improves precision but reduces recall. The high-precision operating point reaches very high precision only at extremely low recall, which shows that the next research step should focus on threshold tuning, calibration, feature leakage checks, fairness evaluation, and comparison against simpler models.

The generated contribution sample demonstrates how model evidence can be transformed into investigator-facing explanations through the Java `MlScoringClient` and `MlFeatureContribution` contracts.
