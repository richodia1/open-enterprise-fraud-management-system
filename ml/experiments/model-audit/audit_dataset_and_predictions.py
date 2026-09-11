#!/usr/bin/env python3
"""Audit prepared fraud datasets and optional model prediction scores.

The validate-only path intentionally uses only the standard library so CI can
check the workflow shape without installing the optional Python ML stack.
"""

from __future__ import annotations

import argparse
import csv
import json
import math
from collections import Counter
from datetime import datetime, timezone
from pathlib import Path
from typing import Any


REPO_ROOT = Path(__file__).resolve().parents[3]
DEFAULT_FEATURE_MATRIX = REPO_ROOT / "data" / "processed" / "oefms-feature-matrix.csv"
DEFAULT_MANIFEST = REPO_ROOT / "ml" / "artifacts" / "dataset-prep" / "manifest.json"
DEFAULT_SCORES_DIR = REPO_ROOT / "ml" / "artifacts" / "model-comparison"
DEFAULT_OUTPUT_DIR = REPO_ROOT / "ml" / "artifacts" / "model-audit"
EXCLUDED_TRAINING_COLUMNS = {"transaction_id", "tenant_id", "split", "label"}
LABELISH_HINTS = ("label", "target", "class", "outcome", "fraud_bool", "is_fraud")
REVIEW_HINTS = ("fraud",)
SEVERITY_ORDER = {"critical": 0, "high": 1, "medium": 2, "review": 3, "low": 4}

BINARY_SLICES = {
    "foreign_request": "raw__foreign_request",
    "free_email": "raw__email_is_free",
    "home_phone_valid": "raw__phone_home_valid",
    "mobile_phone_valid": "raw__phone_mobile_valid",
    "has_other_cards": "raw__has_other_cards",
    "keep_alive_session": "raw__keep_alive_session",
}

CATEGORICAL_SLICES = {
    "payment_type": "raw__payment_type__",
    "employment_status": "raw__employment_status__",
    "housing_status": "raw__housing_status__",
    "application_source": "raw__source__",
    "device_os": "raw__device_os__",
}

REQUIRED_SCORE_COLUMNS = {"model", "split", "transaction_id", "label", "score"}


def main() -> int:
    args = parse_args()
    if args.validate_only:
        summary = validate_audit_inputs(args.feature_matrix, args.manifest, args.scores_dir)
        print(
            "Validated model-audit inputs "
            f"{relative_to_repo(resolve_repo_path(args.feature_matrix))} with {summary['rowCount']} rows, "
            f"{summary['trainingFeatureCount']} training features"
        )
        return 0

    deps = load_analysis_dependencies()
    run_audit(args, deps)
    return 0


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Audit OEFMS fraud datasets and prediction scores.")
    parser.add_argument("--feature-matrix", type=Path, default=DEFAULT_FEATURE_MATRIX)
    parser.add_argument("--manifest", type=Path, default=DEFAULT_MANIFEST)
    parser.add_argument("--scores-dir", type=Path, default=DEFAULT_SCORES_DIR)
    parser.add_argument("--output-dir", type=Path, default=DEFAULT_OUTPUT_DIR)
    parser.add_argument("--threshold", type=float, default=0.5)
    parser.add_argument("--min-slice-count", type=int, default=1000)
    parser.add_argument("--min-positive-count", type=int, default=10)
    parser.add_argument(
        "--validate-only",
        action="store_true",
        help="Validate feature matrix, manifest, and available score file shape without pandas.",
    )
    args = parser.parse_args()
    if args.threshold < 0 or args.threshold > 1:
        raise SystemExit("--threshold must be between 0 and 1.")
    return args


def validate_audit_inputs(feature_matrix: Path, manifest_path: Path, scores_dir: Path) -> dict[str, Any]:
    feature_matrix = resolve_repo_path(feature_matrix)
    manifest_path = resolve_repo_path(manifest_path)
    scores_dir = resolve_repo_path(scores_dir)
    if not feature_matrix.exists():
        raise SystemExit(f"Feature matrix was not found: {relative_to_repo(feature_matrix)}")

    with feature_matrix.open(newline="", encoding="utf-8-sig") as handle:
        reader = csv.DictReader(handle)
        feature_columns = reader.fieldnames or []
        required = {"transaction_id", "split", "label"}
        missing = sorted(required.difference(feature_columns))
        if missing:
            raise SystemExit(f"Feature matrix is missing required columns: {', '.join(missing)}")

        row_count = 0
        split_counts: Counter[str] = Counter()
        label_counts: Counter[str] = Counter()
        for row in reader:
            row_count += 1
            split_counts[row.get("split", "")] += 1
            label_counts[row.get("label", "")] += 1

    if row_count == 0:
        raise SystemExit("Feature matrix has no data rows.")

    if manifest_path.exists():
        manifest = load_json(manifest_path)
        if manifest.get("rowCount") and int(manifest["rowCount"]) != row_count:
            raise SystemExit(
                "Manifest row count does not match feature matrix: "
                f"{manifest['rowCount']} != {row_count}"
            )

    for score_file in score_files(scores_dir):
        with score_file.open(newline="", encoding="utf-8-sig") as handle:
            reader = csv.DictReader(handle)
            score_columns = set(reader.fieldnames or [])
        missing = sorted(REQUIRED_SCORE_COLUMNS.difference(score_columns))
        if missing:
            raise SystemExit(
                f"Score file {relative_to_repo(score_file)} is missing columns: {', '.join(missing)}"
            )

    training_features = [column for column in feature_columns if column not in EXCLUDED_TRAINING_COLUMNS]
    if not training_features:
        raise SystemExit("Feature matrix has no training feature columns.")

    return {
        "rowCount": row_count,
        "trainingFeatureCount": len(training_features),
        "splitCounts": dict(sorted(split_counts.items())),
        "labelCounts": dict(sorted(label_counts.items())),
    }


def load_analysis_dependencies() -> dict[str, Any]:
    try:
        import pandas as pd
    except ModuleNotFoundError as exc:
        raise SystemExit(
            "Missing analysis dependency: pandas. Install the local ML stack with "
            "`python3 -m pip install -r ml/requirements.txt`."
        ) from exc
    return {"pd": pd}


def run_audit(args: argparse.Namespace, deps: dict[str, Any]) -> None:
    feature_matrix = resolve_repo_path(args.feature_matrix)
    manifest_path = resolve_repo_path(args.manifest)
    scores_dir = resolve_repo_path(args.scores_dir)
    output_dir = resolve_repo_path(args.output_dir)
    output_dir.mkdir(parents=True, exist_ok=True)

    summary = validate_audit_inputs(feature_matrix, manifest_path, scores_dir)
    manifest = load_json(manifest_path) if manifest_path.exists() else {}
    pd = deps["pd"]
    df = pd.read_csv(feature_matrix)
    df["label"] = pd.to_numeric(df["label"], errors="coerce").fillna(0).astype(int)
    df["split"] = df["split"].astype(str).str.lower()

    slice_frame = build_slice_frame(df, pd)
    leakage_rows = leakage_flags(df, pd)
    dataset_rows = dataset_slice_rows(slice_frame, args)
    prediction_rows = prediction_slice_rows(slice_frame, scores_dir, args, pd)

    write_csv(output_dir / "feature-leakage-flags.csv", leakage_rows, leakage_columns())
    write_csv(output_dir / "dataset-slice-summary.csv", dataset_rows, dataset_slice_columns())
    write_csv(output_dir / "prediction-slice-metrics.csv", prediction_rows, prediction_slice_columns())
    write_report(
        output_dir / "audit-report.md",
        manifest=manifest,
        summary=summary,
        leakage_rows=leakage_rows,
        dataset_rows=dataset_rows,
        prediction_rows=prediction_rows,
        args=args,
    )

    print(
        "Audited ML dataset "
        f"rows={len(df)} "
        f"slices={len(slice_frame.columns) - 3} "
        f"leakageFlags={len(leakage_rows)} "
        f"predictionSliceRows={len(prediction_rows)} "
        f"report={relative_to_repo(output_dir / 'audit-report.md')}"
    )


def build_slice_frame(df: Any, pd: Any) -> Any:
    slices = df[["transaction_id", "split", "label"]].copy()

    for slice_name, source_column in BINARY_SLICES.items():
        if source_column in df:
            slices[slice_name] = df[source_column].map(binary_value)

    for slice_name, prefix in CATEGORICAL_SLICES.items():
        columns = [column for column in df.columns if column.startswith(prefix)]
        if columns:
            slices[slice_name] = categorical_value(df, columns, prefix, pd)

    add_numeric_band(
        slices,
        df,
        pd,
        "customer_age_band",
        "raw__customer_age",
        [-math.inf, 29, 39, 49, 59, math.inf],
        ["<30", "30-39", "40-49", "50-59", "60+"],
    )
    add_numeric_band(
        slices,
        df,
        pd,
        "income_band",
        "amount_value",
        [-math.inf, 0.2, 0.5, 0.8, math.inf],
        ["<0.20", "0.20-0.49", "0.50-0.79", "0.80+"],
    )
    add_numeric_band(
        slices,
        df,
        pd,
        "credit_risk_score_band",
        "raw__credit_risk_score",
        [-math.inf, 99, 149, 199, math.inf],
        ["<100", "100-149", "150-199", "200+"],
    )
    add_numeric_band(
        slices,
        df,
        pd,
        "session_length_band",
        "raw__session_length_in_minutes",
        [-math.inf, 1, 5, 15, 60, math.inf],
        ["<1m", "1-5m", "5-15m", "15-60m", "60m+"],
    )

    if "raw__device_fraud_count" in df:
        values = numeric_series(df["raw__device_fraud_count"], pd)
        slices["device_fraud_count_band"] = values.map(lambda value: "2+" if value >= 2 else str(int(max(value, 0))))

    return slices


def add_numeric_band(
    slices: Any,
    df: Any,
    pd: Any,
    slice_name: str,
    source_column: str,
    bins: list[float],
    labels: list[str],
) -> None:
    if source_column not in df:
        return
    values = numeric_series(df[source_column], pd)
    slices[slice_name] = pd.cut(values, bins=bins, labels=labels, include_lowest=True).astype(str)


def categorical_value(df: Any, columns: list[str], prefix: str, pd: Any) -> Any:
    active_counts = df[columns].apply(pd.to_numeric, errors="coerce").fillna(0)
    active_column = active_counts.idxmax(axis=1)
    active = active_counts.sum(axis=1) > 0
    return active_column.str.removeprefix(prefix).where(active, "UNKNOWN")


def binary_value(value: Any) -> str:
    try:
        number = float(value)
    except (TypeError, ValueError):
        return "UNKNOWN"
    return "YES" if number >= 0.5 else "NO"


def numeric_series(series: Any, pd: Any) -> Any:
    return pd.to_numeric(series, errors="coerce").fillna(0.0)


def leakage_flags(df: Any, pd: Any) -> list[dict[str, Any]]:
    feature_columns = [column for column in df.columns if column not in EXCLUDED_TRAINING_COLUMNS]
    rows = []

    for column in feature_columns:
        lowered = column.lower()
        if any(hint in lowered for hint in LABELISH_HINTS):
            rows.append(flag_row(
                severity="high",
                feature=column,
                check="label_like_name",
                value="",
                recommendation="Confirm the feature is available before the decision point and is not derived from the target.",
            ))
        elif any(hint in lowered for hint in REVIEW_HINTS):
            rows.append(flag_row(
                severity="review",
                feature=column,
                check="fraud_named_feature",
                value="",
                recommendation="Treat as an availability review item; historical fraud counts can be valid, future-looking fraud labels are leakage.",
            ))

        unique_count = df[column].nunique(dropna=False)
        if int(unique_count) <= 1:
            rows.append(flag_row(
                severity="low",
                feature=column,
                check="constant_feature",
                value=str(int(unique_count)),
                recommendation="Remove or keep only if the feature becomes variable in a broader dataset.",
            ))

    numeric_features = df[feature_columns].apply(pd.to_numeric, errors="coerce").fillna(0.0)
    variable_numeric_features = numeric_features.loc[:, numeric_features.nunique(dropna=False) > 1]
    label = pd.to_numeric(df["label"], errors="coerce").fillna(0).astype(int)
    correlations = variable_numeric_features.corrwith(label).dropna()
    for feature, value in correlations.items():
        absolute = abs(float(value))
        if absolute >= 0.95:
            severity = "critical"
        elif absolute >= 0.75:
            severity = "high"
        elif absolute >= 0.50:
            severity = "medium"
        else:
            continue
        rows.append(flag_row(
            severity=severity,
            feature=feature,
            check="high_label_correlation",
            value=f"{float(value):.6f}",
            recommendation="Review for leakage, target-derived fields, temporal leakage, or over-specific proxy variables.",
        ))

    return sorted(rows, key=lambda row: (SEVERITY_ORDER[row["severity"]], row["feature"], row["check"]))


def flag_row(severity: str, feature: str, check: str, value: str, recommendation: str) -> dict[str, Any]:
    return {
        "severity": severity,
        "feature": feature,
        "check": check,
        "value": value,
        "recommendation": recommendation,
    }


def dataset_slice_rows(slice_frame: Any, args: argparse.Namespace) -> list[dict[str, Any]]:
    global_count = len(slice_frame)
    global_positive = int(slice_frame["label"].sum())
    global_rate = float(global_positive / global_count) if global_count else 0.0
    rows = []
    for slice_name in slice_columns(slice_frame):
        for slice_value, group in slice_frame.groupby(slice_name, dropna=False, sort=True):
            row_count = int(len(group))
            positive_count = int(group["label"].sum())
            fraud_rate = float(positive_count / row_count) if row_count else 0.0
            rows.append({
                "slice": slice_name,
                "value": str(slice_value),
                "rowCount": row_count,
                "positiveLabelCount": positive_count,
                "fraudRate": fraud_rate,
                "fraudLift": float(fraud_rate / global_rate) if global_rate else 0.0,
                "meetsMinimumSample": row_count >= args.min_slice_count and positive_count >= args.min_positive_count,
            })
    return sorted(rows, key=lambda row: (row["slice"], row["value"]))


def prediction_slice_rows(slice_frame: Any, scores_dir: Path, args: argparse.Namespace, pd: Any) -> list[dict[str, Any]]:
    rows = []
    score_paths = score_files(scores_dir)
    if not score_paths:
        return rows

    for score_path in score_paths:
        scores = pd.read_csv(score_path)
        missing = REQUIRED_SCORE_COLUMNS.difference(scores.columns)
        if missing:
            raise SystemExit(
                f"Score file {relative_to_repo(score_path)} is missing columns: {', '.join(sorted(missing))}"
            )
        scores["transaction_id"] = scores["transaction_id"].astype(str)
        scores["score"] = pd.to_numeric(scores["score"], errors="coerce").fillna(0.0)
        scores["model"] = scores["model"].astype(str)
        scores["split"] = scores["split"].astype(str).str.lower()
        frame = scores[["model", "split", "transaction_id", "score"]].merge(
            slice_frame.drop(columns=["split"]),
            on="transaction_id",
            how="inner",
        )
        for (model_name, split_name), scored_split in frame.groupby(["model", "split"], sort=True):
            for slice_name in slice_columns(slice_frame):
                for slice_value, group in scored_split.groupby(slice_name, dropna=False, sort=True):
                    row = prediction_metrics_row(
                        model_name=str(model_name),
                        split_name=str(split_name),
                        slice_name=slice_name,
                        slice_value=str(slice_value),
                        group=group,
                        threshold=args.threshold,
                    )
                    if row["rowCount"] >= args.min_slice_count and row["positiveLabelCount"] >= args.min_positive_count:
                        rows.append(row)

    return sorted(rows, key=lambda row: (row["model"], row["split"], row["slice"], row["value"]))


def prediction_metrics_row(
    model_name: str,
    split_name: str,
    slice_name: str,
    slice_value: str,
    group: Any,
    threshold: float,
) -> dict[str, Any]:
    labels = group["label"].astype(int)
    predicted = group["score"] >= threshold
    positive = labels == 1
    negative = labels == 0
    tp = int((predicted & positive).sum())
    fp = int((predicted & negative).sum())
    fn = int((~predicted & positive).sum())
    tn = int((~predicted & negative).sum())
    row_count = int(len(group))
    positive_count = int(positive.sum())
    predicted_positive_count = int(predicted.sum())
    return {
        "model": model_name,
        "split": split_name,
        "threshold": threshold,
        "slice": slice_name,
        "value": slice_value,
        "rowCount": row_count,
        "positiveLabelCount": positive_count,
        "fraudRate": float(positive_count / row_count) if row_count else 0.0,
        "averageScore": float(group["score"].mean()) if row_count else 0.0,
        "predictedPositiveCount": predicted_positive_count,
        "predictedPositiveRate": float(predicted_positive_count / row_count) if row_count else 0.0,
        "precision": float(tp / (tp + fp)) if (tp + fp) else None,
        "recall": float(tp / (tp + fn)) if (tp + fn) else None,
        "falsePositiveRate": float(fp / (fp + tn)) if (fp + tn) else None,
        "truePositive": tp,
        "falsePositive": fp,
        "falseNegative": fn,
        "trueNegative": tn,
    }


def slice_columns(slice_frame: Any) -> list[str]:
    return [column for column in slice_frame.columns if column not in {"transaction_id", "split", "label"}]


def score_files(scores_dir: Path) -> list[Path]:
    scores_dir = resolve_repo_path(scores_dir)
    if not scores_dir.exists():
        return []
    return sorted(scores_dir.glob("*-scores.csv"))


def write_report(
    path: Path,
    manifest: dict[str, Any],
    summary: dict[str, Any],
    leakage_rows: list[dict[str, Any]],
    dataset_rows: list[dict[str, Any]],
    prediction_rows: list[dict[str, Any]],
    args: argparse.Namespace,
) -> None:
    label_counts = summary.get("labelCounts", {})
    reliable_slices = [row for row in dataset_rows if row["meetsMinimumSample"]]
    high_lift_slices = sorted(reliable_slices, key=lambda row: row["fraudLift"], reverse=True)[:10]
    prediction_hotspots = sorted(
        prediction_rows,
        key=lambda row: (
            row["predictedPositiveRate"],
            row["falsePositiveRate"] if row["falsePositiveRate"] is not None else -1,
        ),
        reverse=True,
    )[:10]

    lines = [
        "# ML Leakage And Fairness Audit",
        "",
        f"Generated: {timestamp()}",
        "",
        "## Dataset",
        "",
        f"- Dataset id: `{manifest.get('datasetId', 'unknown')}`",
        f"- Dataset name: `{manifest.get('datasetName', 'unknown')}`",
        f"- Rows: `{summary['rowCount']}`",
        f"- Training features: `{summary['trainingFeatureCount']}`",
        f"- Label counts: `{json.dumps(label_counts, sort_keys=True)}`",
        "",
        "## Leakage Review",
        "",
        f"- Flags found: `{len(leakage_rows)}`",
        "",
        "| Severity | Feature | Check | Value |",
        "| --- | --- | --- | --- |",
    ]
    for row in leakage_rows[:15]:
        lines.append(f"| {row['severity']} | `{row['feature']}` | {row['check']} | {row['value']} |")
    if not leakage_rows:
        lines.append("| low | n/a | no_flags |  |")

    lines.extend([
        "",
        "## Highest Fraud-Rate Slices",
        "",
        f"Minimum sample: `{args.min_slice_count}` rows and `{args.min_positive_count}` positive labels.",
        "",
        "| Slice | Value | Rows | Fraud Rate | Lift |",
        "| --- | --- | ---: | ---: | ---: |",
    ])
    for row in high_lift_slices:
        lines.append(
            f"| {row['slice']} | {row['value']} | {row['rowCount']} | "
            f"{format_rate(row['fraudRate'])} | {float(row['fraudLift']):.2f} |"
        )
    if not high_lift_slices:
        lines.append("| n/a | n/a | 0 | 0.00% | 0.00 |")

    lines.extend([
        "",
        "## Prediction Slice Hotspots",
        "",
        f"Threshold: `{args.threshold}`.",
        "",
        "| Model | Split | Slice | Value | Rows | Predicted Positive Rate | Recall | FPR |",
        "| --- | --- | --- | --- | ---: | ---: | ---: | ---: |",
    ])
    for row in prediction_hotspots:
        lines.append(
            f"| {row['model']} | {row['split']} | {row['slice']} | {row['value']} | "
            f"{row['rowCount']} | {format_rate(row['predictedPositiveRate'])} | "
            f"{format_optional_rate(row['recall'])} | {format_optional_rate(row['falsePositiveRate'])} |"
        )
    if not prediction_hotspots:
        lines.append("| n/a | n/a | n/a | n/a | 0 | 0.00% | n/a | n/a |")

    lines.extend([
        "",
        "## Use In OEFMS",
        "",
        "Use this audit before promoting an ML model into `ml` or `hybrid` mode. "
        "The report does not certify fairness by itself; it highlights leakage risk, "
        "high-risk dataset segments, and score behavior that should be reviewed by domain experts.",
        "",
    ])
    path.write_text("\n".join(lines), encoding="utf-8")


def write_csv(path: Path, rows: list[dict[str, Any]], columns: list[str]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("w", newline="", encoding="utf-8") as handle:
        writer = csv.DictWriter(handle, fieldnames=columns)
        writer.writeheader()
        writer.writerows(rows)


def leakage_columns() -> list[str]:
    return ["severity", "feature", "check", "value", "recommendation"]


def dataset_slice_columns() -> list[str]:
    return ["slice", "value", "rowCount", "positiveLabelCount", "fraudRate", "fraudLift", "meetsMinimumSample"]


def prediction_slice_columns() -> list[str]:
    return [
        "model",
        "split",
        "threshold",
        "slice",
        "value",
        "rowCount",
        "positiveLabelCount",
        "fraudRate",
        "averageScore",
        "predictedPositiveCount",
        "predictedPositiveRate",
        "precision",
        "recall",
        "falsePositiveRate",
        "truePositive",
        "falsePositive",
        "falseNegative",
        "trueNegative",
    ]


def load_json(path: Path) -> dict[str, Any]:
    with resolve_repo_path(path).open(encoding="utf-8") as handle:
        return json.load(handle)


def timestamp() -> str:
    return datetime.now(timezone.utc).isoformat().replace("+00:00", "Z")


def format_rate(value: float) -> str:
    return f"{float(value) * 100:.2f}%"


def format_optional_rate(value: float | None) -> str:
    return "n/a" if value is None else format_rate(value)


def resolve_repo_path(path: Path) -> Path:
    return path if path.is_absolute() else REPO_ROOT / path


def relative_to_repo(path: Path) -> str:
    try:
        return str(path.resolve().relative_to(REPO_ROOT))
    except ValueError:
        return str(path)


if __name__ == "__main__":
    raise SystemExit(main())
