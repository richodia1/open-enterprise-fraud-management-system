#!/usr/bin/env python3
"""Prepare fraud datasets into the OEFMS feature-matrix shape.

This script intentionally uses only the Python standard library so the first
dataset manipulation path can run in CI without a heavyweight ML environment.
Training code can later add pandas, scikit-learn, XGBoost, SHAP, and a model
registry around the stable outputs created here.
"""

from __future__ import annotations

import argparse
import csv
import json
import math
import uuid
from collections import Counter
from datetime import datetime, timedelta, timezone
from pathlib import Path
from typing import Any


REPO_ROOT = Path(__file__).resolve().parents[2]
DEFAULT_CATALOG = REPO_ROOT / "data" / "catalog" / "fraud-datasets.json"
DEFAULT_INTERIM = REPO_ROOT / "data" / "interim" / "oefms-standard-transactions.csv"
DEFAULT_PROCESSED = REPO_ROOT / "data" / "processed" / "oefms-feature-matrix.csv"
DEFAULT_MANIFEST = REPO_ROOT / "ml" / "artifacts" / "dataset-prep" / "manifest.json"
STANDARD_COLUMNS = [
    "transaction_id",
    "external_reference",
    "tenant_id",
    "transaction_type",
    "channel",
    "direction",
    "amount",
    "currency",
    "originator_country",
    "beneficiary_country",
    "initiated_at",
    "label",
    "source_dataset",
    "source_row_number",
]
CATEGORICAL_COLUMNS = [
    "transaction_type",
    "channel",
    "direction",
    "currency",
    "originator_country",
    "beneficiary_country",
    "amount_bucket",
]
PASSTHROUGH_EXCLUDE = set(STANDARD_COLUMNS)


def main() -> int:
    args = parse_args()
    catalog = load_json(args.catalog)
    dataset = select_dataset(catalog, args.dataset_id)
    raw_path = resolve_repo_path(args.raw or dataset["local"]["rawPath"])
    interim_output = resolve_repo_path(args.interim_output)
    processed_output = resolve_repo_path(args.processed_output)
    manifest_output = resolve_repo_path(args.manifest_output)

    raw_rows = read_csv(raw_path, args.limit)
    standard_rows = standardize_rows(dataset, raw_rows)
    feature_rows, feature_columns = engineer_features(dataset, standard_rows)

    write_csv(interim_output, STANDARD_COLUMNS, standard_rows)
    write_csv(processed_output, feature_columns, feature_rows)
    manifest = build_manifest(
        dataset=dataset,
        catalog_path=args.catalog,
        raw_path=raw_path,
        interim_output=interim_output,
        processed_output=processed_output,
        manifest_output=manifest_output,
        standard_rows=standard_rows,
        feature_columns=feature_columns,
    )
    write_json(manifest_output, manifest)

    print(
        "Prepared dataset "
        f"{dataset['id']} with {len(standard_rows)} rows, "
        f"{len(feature_columns)} feature-matrix columns, "
        f"manifest={relative_to_repo(manifest_output)}"
    )
    return 0


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Prepare an OEFMS fraud dataset for ML experiments.")
    parser.add_argument("--dataset-id", default=None, help="Dataset id from data/catalog/fraud-datasets.json.")
    parser.add_argument("--catalog", type=Path, default=DEFAULT_CATALOG, help="Dataset catalog path.")
    parser.add_argument("--raw", type=Path, default=None, help="Override raw CSV path.")
    parser.add_argument("--interim-output", type=Path, default=DEFAULT_INTERIM, help="Standardized CSV output.")
    parser.add_argument("--processed-output", type=Path, default=DEFAULT_PROCESSED, help="Feature matrix CSV output.")
    parser.add_argument("--manifest-output", type=Path, default=DEFAULT_MANIFEST, help="Experiment manifest output.")
    parser.add_argument("--limit", type=int, default=None, help="Optional max row count for smoke checks.")
    return parser.parse_args()


def load_json(path: Path) -> dict[str, Any]:
    with path.open(encoding="utf-8") as handle:
        return json.load(handle)


def select_dataset(catalog: dict[str, Any], dataset_id: str | None) -> dict[str, Any]:
    requested_id = dataset_id or catalog["defaultDatasetId"]
    for dataset in catalog["datasets"]:
        if dataset["id"] == requested_id:
            return dataset
    raise SystemExit(f"Dataset id was not found in catalog: {requested_id}")


def resolve_repo_path(path: str | Path) -> Path:
    path = Path(path)
    return path if path.is_absolute() else REPO_ROOT / path


def read_csv(path: Path, limit: int | None) -> list[dict[str, str]]:
    if not path.exists():
        raise SystemExit(
            f"Raw dataset file was not found: {relative_to_repo(path)}. "
            "Download the dataset locally or pass --raw."
        )

    with path.open(newline="", encoding="utf-8-sig") as handle:
        reader = csv.DictReader(handle)
        rows = []
        for row in reader:
            rows.append({key: clean(value) for key, value in row.items()})
            if limit is not None and len(rows) >= limit:
                break
        return rows


def standardize_rows(dataset: dict[str, Any], raw_rows: list[dict[str, str]]) -> list[dict[str, str]]:
    schema = dataset["schema"]
    column_map = schema.get("columnMap", {})
    defaults = schema.get("defaults", {})
    transforms = schema.get("transforms", {})

    standard_rows = []
    for index, row in enumerate(raw_rows, start=1):
        standard_row = {}
        for field in STANDARD_COLUMNS:
            if field == "source_dataset":
                standard_row[field] = dataset["id"]
            elif field == "source_row_number":
                standard_row[field] = str(index)
            else:
                standard_row[field] = standard_value(
                    dataset=dataset,
                    row=row,
                    field=field,
                    row_number=index,
                    column_map=column_map,
                    defaults=defaults,
                    transforms=transforms,
                )
        preserve_passthrough_features(standard_row, row, schema.get("passthroughFeaturePrefixes", []))
        standard_row["label"] = normalize_label(standard_row["label"])
        standard_rows.append(standard_row)
    return standard_rows


def preserve_passthrough_features(
    standard_row: dict[str, str],
    raw_row: dict[str, str],
    prefixes: list[str],
) -> None:
    for column, value in raw_row.items():
        if any(column.startswith(prefix) for prefix in prefixes):
            standard_row[column] = value


def standard_value(
    dataset: dict[str, Any],
    row: dict[str, str],
    field: str,
    row_number: int,
    column_map: dict[str, str],
    defaults: dict[str, str],
    transforms: dict[str, str],
) -> str:
    mapped_column = column_map.get(field)
    value = row.get(mapped_column, "") if mapped_column else ""
    if value == "":
        value = defaults.get(field, "")

    if field == "transaction_id" and value == "":
        value = str(uuid.uuid5(uuid.NAMESPACE_URL, f"oefms:{dataset['id']}:{row_number}"))
    if field == "initiated_at" and value == "":
        value = generated_timestamp(row_number)

    transform = transforms.get(field)
    if transform == "hour_step_to_timestamp":
        return hour_step_to_timestamp(value)
    if transform == "seconds_offset_to_timestamp":
        return seconds_offset_to_timestamp(value)
    return clean(value)


def normalize_label(value: str) -> str:
    normalized = value.strip().lower()
    if normalized in {"1", "true", "yes", "y", "fraud", "fraudulent"}:
        return "1"
    if normalized in {"0", "false", "no", "n", "legitimate", "genuine"}:
        return "0"
    return normalized


def hour_step_to_timestamp(value: str) -> str:
    try:
        hours = int(float(value))
    except ValueError:
        hours = 0
    return timestamp_from_offset(timedelta(hours=hours))


def seconds_offset_to_timestamp(value: str) -> str:
    try:
        seconds = int(float(value))
    except ValueError:
        seconds = 0
    return timestamp_from_offset(timedelta(seconds=seconds))


def generated_timestamp(row_number: int) -> str:
    return timestamp_from_offset(timedelta(seconds=row_number - 1))


def timestamp_from_offset(offset: timedelta) -> str:
    base = datetime(2026, 1, 1, tzinfo=timezone.utc)
    return (base + offset).isoformat().replace("+00:00", "Z")


def engineer_features(
    dataset: dict[str, Any],
    standard_rows: list[dict[str, str]],
) -> tuple[list[dict[str, str]], list[str]]:
    category_values = {
        column: sorted({safe_token(row[column]) for row in standard_rows if row.get(column)})
        for column in CATEGORICAL_COLUMNS
        if column != "amount_bucket"
    }
    category_values["amount_bucket"] = ["LOW", "MEDIUM", "HIGH", "CRITICAL"]
    passthrough_columns = passthrough_columns_from(dataset, standard_rows)

    feature_columns = [
        "transaction_id",
        "tenant_id",
        "split",
        "amount_value",
        "amount_log10",
        "is_high_value",
        "is_critical_value",
        "is_cross_border",
    ]
    for column in CATEGORICAL_COLUMNS:
        for value in category_values.get(column, []):
            feature_columns.append(f"{column}__{value}")
    feature_columns.extend(passthrough_columns)
    feature_columns.append("label")

    splits = assign_splits(dataset, len(standard_rows))
    feature_rows = []
    for index, row in enumerate(standard_rows):
        amount = parse_float(row["amount"])
        amount_bucket = amount_bucket_for(amount)
        feature_row = {
            "transaction_id": row["transaction_id"],
            "tenant_id": row["tenant_id"],
            "split": splits[index],
            "amount_value": f"{amount:.2f}",
            "amount_log10": f"{math.log10(max(amount, 0.0) + 1.0):.6f}",
            "is_high_value": "1" if amount >= 10000 else "0",
            "is_critical_value": "1" if amount >= 100000 else "0",
            "is_cross_border": is_cross_border(row),
        }

        enriched_row = dict(row)
        enriched_row["amount_bucket"] = amount_bucket
        for column in CATEGORICAL_COLUMNS:
            active_value = safe_token(enriched_row.get(column, ""))
            for value in category_values.get(column, []):
                feature_row[f"{column}__{value}"] = "1" if active_value == value else "0"

        for column in passthrough_columns:
            feature_row[column] = normalize_numeric(row.get(column.removeprefix("raw__"), ""))

        feature_row["label"] = row["label"]
        feature_rows.append(feature_row)

    return feature_rows, feature_columns


def passthrough_columns_from(dataset: dict[str, Any], rows: list[dict[str, str]]) -> list[str]:
    prefixes = dataset["schema"].get("passthroughFeaturePrefixes", [])
    if not prefixes or not rows:
        return []

    source_columns = sorted(rows[0].keys())
    passthrough = []
    for column in source_columns:
        if column in PASSTHROUGH_EXCLUDE:
            continue
        if any(column.startswith(prefix) for prefix in prefixes):
            passthrough.append(f"raw__{column}")
    return passthrough


def assign_splits(dataset: dict[str, Any], row_count: int) -> list[str]:
    if row_count == 0:
        return []

    split = dataset.get("split", {})
    train_ratio = float(split.get("trainRatio", 0.7))
    validation_ratio = float(split.get("validationRatio", 0.15))
    train_count = max(1, int(row_count * train_ratio))
    validation_count = int(row_count * validation_ratio)
    if row_count >= 3:
        validation_count = max(1, validation_count)
    if train_count + validation_count >= row_count and row_count >= 3:
        validation_count = max(1, row_count - train_count - 1)

    splits = []
    for index in range(row_count):
        if index < train_count:
            splits.append("train")
        elif index < train_count + validation_count:
            splits.append("validation")
        else:
            splits.append("test")
    return splits


def build_manifest(
    dataset: dict[str, Any],
    catalog_path: Path,
    raw_path: Path,
    interim_output: Path,
    processed_output: Path,
    manifest_output: Path,
    standard_rows: list[dict[str, str]],
    feature_columns: list[str],
) -> dict[str, Any]:
    labels = Counter(row["label"] for row in standard_rows)
    splits = Counter(assign_splits(dataset, len(standard_rows)))
    return {
        "manifestVersion": "2026-09-11",
        "generatedAt": datetime.now(timezone.utc).isoformat().replace("+00:00", "Z"),
        "datasetId": dataset["id"],
        "datasetName": dataset["name"],
        "taskType": dataset["taskType"],
        "source": dataset["source"],
        "inputs": {
            "catalog": relative_to_repo(catalog_path),
            "raw": relative_to_repo(raw_path),
        },
        "outputs": {
            "standardized": relative_to_repo(interim_output),
            "featureMatrix": relative_to_repo(processed_output),
            "manifest": relative_to_repo(manifest_output),
        },
        "rowCount": len(standard_rows),
        "labelCounts": dict(sorted(labels.items())),
        "splitCounts": dict(sorted(splits.items())),
        "featureColumns": feature_columns,
        "javaRuntimeReference": {
            "decisionModeEnv": "OEFMS_FRAUD_DECISION_MODE=ml|hybrid",
            "scoringClient": "org.openefms.frauddetection.ml.MlScoringClient",
            "scoreRequest": "org.openefms.common.models.ml.MlScoreRequest",
            "scoreResponse": "org.openefms.common.models.ml.MlScoreResponse",
            "featureContribution": "org.openefms.common.models.ml.MlFeatureContribution"
        }
    }


def write_csv(path: Path, columns: list[str], rows: list[dict[str, str]]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("w", newline="", encoding="utf-8") as handle:
        writer = csv.DictWriter(handle, fieldnames=columns, extrasaction="ignore")
        writer.writeheader()
        writer.writerows(rows)


def write_json(path: Path, payload: dict[str, Any]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("w", encoding="utf-8") as handle:
        json.dump(payload, handle, indent=2, sort_keys=True)
        handle.write("\n")


def is_cross_border(row: dict[str, str]) -> str:
    originator = row["originator_country"].upper()
    beneficiary = row["beneficiary_country"].upper()
    if not originator or not beneficiary or "UNKNOWN" in {originator, beneficiary}:
        return "0"
    return "1" if originator != beneficiary else "0"


def amount_bucket_for(amount: float) -> str:
    if amount >= 100000:
        return "CRITICAL"
    if amount >= 10000:
        return "HIGH"
    if amount >= 1000:
        return "MEDIUM"
    return "LOW"


def parse_float(value: str) -> float:
    try:
        return float(value)
    except ValueError:
        return 0.0


def normalize_numeric(value: str) -> str:
    try:
        return str(float(value))
    except ValueError:
        return "0.0"


def safe_token(value: str) -> str:
    cleaned = "".join(character if character.isalnum() else "_" for character in value.upper())
    return "_".join(part for part in cleaned.split("_") if part) or "UNKNOWN"


def clean(value: Any) -> str:
    return "" if value is None else str(value).strip()


def relative_to_repo(path: Path) -> str:
    try:
        return str(path.resolve().relative_to(REPO_ROOT))
    except ValueError:
        return str(path)


if __name__ == "__main__":
    raise SystemExit(main())
