# ML and AI Strategy

OEFMS should support both deterministic fraud rules and machine-learning risk scoring.

The first production-safe posture is rule-based decisioning, because it is deterministic and easy to audit. ML/AI should be introduced as a scored decision-support mode after datasets, validation, explainability, and model governance are in place.

## Decision Modes

The Fraud Detection Service exposes the configured decision mode through configuration:

```yaml
oefms:
  fraud:
    decision:
      mode: "${OEFMS_FRAUD_DECISION_MODE:rules}"
      ml:
        provider: "${OEFMS_FRAUD_ML_PROVIDER:disabled}"
        model-name: "${OEFMS_FRAUD_ML_MODEL_NAME:xgboost-baseline}"
        score-threshold: "${OEFMS_FRAUD_ML_SCORE_THRESHOLD:0.80}"
```

Supported configured modes:

| Mode | Purpose |
| --- | --- |
| `rules` | Use deterministic configured rules only. This is the default while the project has no trained model runtime. |
| `ml` | Use a trained ML model as the primary risk-scoring mechanism. |
| `hybrid` | Combine rule hits and ML score so institutions can keep hard policy rules while using ML for pattern detection. |

`ml` and `hybrid` now route through the Java `MlScoringClient` contract. The default runtime reports `DISABLED` or `CONTRACT_ONLY` through `GET /api/v1/fraud/ml/runtime` and refuses live scoring until a production scorer adapter is registered.

## Dataset Shortlist

Raw datasets should not be committed to this repository. Contributors should download them locally under `data/raw/` or `data/external/`, which are ignored by Git.

Dataset references and column mappings are tracked in:

```text
data/catalog/fraud-datasets.json
```

| Dataset | Good For | Notes |
| --- | --- | --- |
| PaySim Synthetic Financial Dataset | Synthetic transaction-flow experiments and XGBoost baseline work. | Useful because it is synthetic and transaction-oriented. Source: https://www.kaggle.com/datasets/ealaxi/paysim1 |
| Credit Card Fraud Detection | First imbalanced binary-classification baseline. | Contains 284,807 transactions and 492 fraud cases, so it is useful for precision/recall and class-imbalance practice. Source: https://www.kaggle.com/datasets/mlg-ulb/creditcardfraud |
| IEEE-CIS Fraud Detection | Larger feature-engineering benchmark. | Useful for high-dimensional transaction and identity features. Source: https://www.kaggle.com/competitions/ieee-fraud-detection |
| Bank Account Fraud Dataset Suite | Bank account opening fraud, bias, imbalance, and temporal evaluation. | A privacy-preserving synthetic tabular fraud suite published with NeurIPS 2022. Source: https://papers.nips.cc/paper_files/paper/2022/hash/126b93dcc2683fdaea4bb6d3be3727e0-Abstract-Datasets_and_Benchmarks.html |
| Financial Fraud Alert Review Dataset | Human review and investigator decision-support research. | Useful later for human-in-the-loop fraud operations and learning-to-defer experiments. Source: https://springernature.figshare.com/articles/dataset/Financial_Fraud_Alert_Review_Dataset/28351172 |

The first serious OEFMS academic experiment should use the Bank Account Fraud Dataset Suite `Base.csv` file because it has meaningful tabular features that can be explained to investigators. The catalog maps numeric features such as `credit_risk_score`, `velocity_6h`, `customer_age`, and `device_fraud_count`, plus categorical features such as `payment_type`, `employment_status`, `housing_status`, `source`, and `device_os`.

## Dataset Preparation Foundation

The first dataset manipulation path is:

```text
data/catalog/fraud-datasets.json
        |
        v
data/raw/ or data/samples/
        |
        v
ml/pipelines/prepare_tabular_dataset.py
        |
        +--> data/interim/oefms-standard-transactions.csv
        +--> data/processed/oefms-feature-matrix.csv
        +--> ml/artifacts/dataset-prep/manifest.json
```

Run the committed synthetic sample:

```bash
./scripts/prepare-ml-dataset.sh oefms_synthetic_sample
```

Run the BAF baseline preparation after placing `Base.csv` under `data/raw/bank-account-fraud/`:

```bash
./scripts/prepare-ml-dataset.sh bank_account_fraud_neurips_2022
python3 ml/experiments/xgboost-baseline/train_xgboost_baseline.py --validate-only
```

The standardized output normalizes different dataset shapes into transaction-like fields such as transaction id, tenant id, type, channel, direction, amount, countries, timestamp, label, and source lineage.

The processed feature matrix adds numeric and one-hot encoded features such as amount log scale, high-value flags, cross-border flags, channel indicators, currency indicators, amount buckets, and the supervised label.

The manifest records the dataset id, source reference, generated outputs, label counts, split counts, feature columns, and Java scoring contract references.

See [ML Dataset Foundation](ml-dataset-foundation.md) for the detailed contributor workflow.

See [XGBoost Baseline Results](ml-xgboost-baseline-results.md) for the first local full-dataset BAF training summary.

## Recommended First Experiments

Start with tabular gradient-boosting models before deep learning.

1. Logistic regression baseline.
2. Random forest baseline.
3. XGBoost baseline.
4. LightGBM or CatBoost comparison.
5. Hybrid rules plus ML score experiment.

XGBoost is a strong first choice because fraud datasets are usually tabular, sparse, noisy, and imbalanced. For imbalanced labels, tune parameters such as `scale_pos_weight`, then evaluate with precision-recall metrics rather than relying on accuracy.

## Evaluation Principles

- Use train/validation/test splits that avoid leakage.
- Prefer time-based splits when the dataset supports transaction time.
- Track precision, recall, F1, ROC-AUC, PR-AUC, false-positive rate, and recall at fixed precision.
- Calibrate scores before exposing them as operational risk percentages.
- Tune thresholds using business cost, not accuracy alone.
- Keep model cards and experiment metadata with each trained model.
- Use SHAP or another explainability method for investigator-facing feature contribution summaries.

## Runtime Direction

The Java services should not depend on notebooks or raw Python scripts at runtime.

Recommended future options:

- Prepare datasets in `ml/pipelines/` using the catalog-driven transformation path.
- Train models in `ml/` using Python tooling.
- Store model artifacts outside Git or through a governed artifact store.
- Serve models through a dedicated model-serving boundary or a JVM-compatible artifact format.
- Feed ML scores into Fraud Detection Service.
- Feed rule hits, model score, feature contributions, and supporting evidence into Explainable AI and Case Management workflows.

This keeps the project open to multiple AI stacks while preserving a stable Java service contract.

See [ML Runtime Contract](ml-runtime-contract.md) for the shared request/response contracts and Fraud Detection runtime behavior.
