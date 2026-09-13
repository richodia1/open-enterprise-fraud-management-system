# ML Dataset Foundation

This branch shows how fraud datasets are referenced, manipulated, and connected to the Java ML runtime contract.

It does not train a model yet. The goal is to make the dataset path visible and reproducible before contributors add XGBoost, model evaluation, SHAP explanations, and a production scorer adapter.

## Reference Catalog

Dataset references live in:

```text
data/catalog/fraud-datasets.json
```

The catalog stores source URLs, local ignored paths, dataset task type, schema mappings, default values, split ratios, and license-review reminders.

Tracked catalog ids:

| Catalog ID | Purpose |
| --- | --- |
| `oefms_synthetic_sample` | Tiny committed sample used by CI and contributors to verify the pipeline. |
| `paysim_synthetic_financial` | Transaction-flow fraud experiments and first XGBoost baseline. |
| `credit_card_fraud_detection` | Imbalanced binary-classification baseline. |
| `ieee_cis_fraud_detection` | Larger feature-engineering benchmark. |
| `bank_account_fraud_neurips_2022` | Account-opening fraud, imbalance, bias, and temporal evaluation. |
| `fifar_alert_review` | Human-in-the-loop alert review and learning-to-defer research. |

Raw external datasets are not committed. Download them locally under the catalog path in `data/raw/` or `data/external/`.

## Manipulation Flow

```text
Catalog entry
        |
        v
Raw CSV or sample CSV
        |
        v
Standardized transaction-like CSV
        |
        v
Feature matrix CSV
        |
        v
Experiment manifest
        |
        v
Future XGBoost training and Java scorer adapter
```

Run the sample:

```bash
./scripts/prepare-ml-dataset.sh oefms_synthetic_sample
```

The pipeline writes ignored outputs:

```text
data/interim/oefms-standard-transactions.csv
data/processed/oefms-feature-matrix.csv
ml/artifacts/dataset-prep/manifest.json
```

## Standardized Schema

The interim CSV normalizes raw datasets into:

```text
transaction_id
external_reference
tenant_id
transaction_type
channel
direction
amount
currency
originator_country
beneficiary_country
initiated_at
label
source_dataset
source_row_number
```

This allows very different datasets to be made comparable before feature engineering.

## Feature Matrix

The processed CSV adds model-ready features:

- `amount_value`
- `amount_log10`
- `is_high_value`
- `is_critical_value`
- `is_cross_border`
- one-hot encoded transaction type, channel, direction, currency, country, and amount bucket columns
- optional raw passthrough columns for datasets such as Credit Card Fraud `V*` fields
- explicit raw numeric columns for BAF such as `raw__credit_risk_score`, `raw__velocity_6h`, `raw__customer_age`, and `raw__device_fraud_count`
- explicit raw categorical one-hot columns for BAF such as `raw__payment_type__AA`, `raw__employment_status__CB`, and `raw__device_os__LINUX`
- `split`
- `label`

The first split strategy is ordered train/validation/test. A later branch can add time-aware splitting, leakage checks, class-imbalance sampling, and model evaluation reports.

## Bank Account Fraud Baseline

The preferred single academic dataset for the first paper path is the Bank Account Fraud Dataset Suite `Base.csv` file. Place it locally at:

```text
data/raw/bank-account-fraud/Base.csv
```

Then prepare it with:

```bash
./scripts/prepare-ml-dataset.sh bank_account_fraud_neurips_2022
```

The catalog keeps the raw data out of Git while preserving BAF modelling signals in the generated feature matrix. This gives XGBoost meaningful, explainable inputs instead of relying only on generic amount and default transaction fields.

## Manifest Link To Java

Each preparation run writes a manifest that records dataset lineage and Java runtime references:

```text
org.openefms.frauddetection.service.MlScoringClient
org.openefms.common.models.ml.MlScoreRequest
org.openefms.common.models.ml.MlScoreResponse
org.openefms.common.models.ml.MlFeatureContribution
```

The Java Fraud Detection Service does not read the CSV files directly. A future model-serving adapter will train from the feature matrix, register or serve the model artifact, and expose calibrated scores plus feature contributions through `MlScoringClient`.

## XGBoost Direction

The first XGBoost branch should:

- read `data/processed/oefms-feature-matrix.csv`
- exclude identifiers and `split` from training features
- train only on `split=train`
- tune class imbalance using the positive-to-negative label ratio
- evaluate on validation and test splits using PR-AUC, ROC-AUC, precision, recall, F1, and recall at fixed precision
- write a model card, metrics JSON, feature importance CSV, and contribution samples under `ml/artifacts/xgboost-baseline/`
- keep trained artifacts under ignored `ml/artifacts/` or a governed registry
