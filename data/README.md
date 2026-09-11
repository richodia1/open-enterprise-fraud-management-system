# Data Workspace

This folder is for dataset instructions, source references, schema mappings, and tiny synthetic samples only.

Do not commit raw, processed, or external datasets to Git. Use local folders such as:

```text
data/raw/
data/interim/
data/processed/
data/external/
```

Those folders are ignored because public fraud datasets can be large, licensed separately, or unsuitable for redistribution.

## Tracked Dataset References

Dataset references live in:

```text
data/catalog/fraud-datasets.json
```

The catalog records:

- public source URL
- local ignored file path
- dataset task type
- source-to-OEFMS column mapping
- default values needed to standardize different datasets
- train/validation/test split ratios
- Java runtime contract references emitted in generated manifests

The committed sample dataset is:

```text
data/samples/oefms-synthetic-transactions.csv
```

It is intentionally tiny and exists only to demonstrate the dataset preparation flow in CI.

## Preparation Flow

Run:

```bash
./scripts/prepare-ml-dataset.sh oefms_synthetic_sample
```

Outputs are generated under ignored folders:

```text
data/interim/oefms-standard-transactions.csv
data/processed/oefms-feature-matrix.csv
ml/artifacts/dataset-prep/manifest.json
```

See [ML Dataset Foundation](../docs/ml-dataset-foundation.md) and [ML and AI Strategy](../docs/ml-ai-strategy.md) for the full dataset direction.
