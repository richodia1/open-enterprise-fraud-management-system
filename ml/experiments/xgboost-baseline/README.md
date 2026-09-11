# XGBoost Baseline Experiment

This folder references the first planned tabular ML experiment. It does not commit trained model artifacts.

## Dataset Input

Run the dataset preparation pipeline first:

```bash
./scripts/prepare-ml-dataset.sh oefms_synthetic_sample
```

For an external dataset, download it locally under the ignored `data/raw/` or `data/external/` folder shown in `data/catalog/fraud-datasets.json`, then pass the catalog id:

```bash
./scripts/prepare-ml-dataset.sh paysim_synthetic_financial
```

The pipeline writes:

```text
data/interim/oefms-standard-transactions.csv
data/processed/oefms-feature-matrix.csv
ml/artifacts/dataset-prep/manifest.json
```

The XGBoost experiment should train from `data/processed/oefms-feature-matrix.csv` and copy the manifest into the final model card or model registry metadata.

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
