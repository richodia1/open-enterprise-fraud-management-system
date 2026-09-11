#!/usr/bin/env python3
"""Compare tabular fraud models and threshold operating points.

The script keeps OEFMS research runs reproducible without committing datasets or
model artifacts. Use --validate-only for CI-friendly checks that do not import
the optional ML dependencies.
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
DEFAULT_OUTPUT_DIR = REPO_ROOT / "ml" / "artifacts" / "model-comparison"
EXCLUDED_TRAINING_COLUMNS = {"transaction_id", "tenant_id", "split", "label"}
DEFAULT_MODELS = ("logistic_regression", "xgboost")
SUPPORTED_MODELS = {"logistic_regression", "random_forest", "xgboost"}


def main() -> int:
    args = parse_args()
    if args.validate_only:
        summary = validate_feature_matrix(args.feature_matrix, args.manifest)
        print(
            "Validated model-comparison inputs "
            f"{relative_to_repo(resolve_repo_path(args.feature_matrix))} with {summary['rowCount']} rows, "
            f"{summary['trainingFeatureCount']} training features"
        )
        return 0

    deps = load_ml_dependencies(args.models)
    compare_models(args, deps)
    return 0


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Compare OEFMS tabular fraud models.")
    parser.add_argument("--feature-matrix", type=Path, default=DEFAULT_FEATURE_MATRIX)
    parser.add_argument("--manifest", type=Path, default=DEFAULT_MANIFEST)
    parser.add_argument("--output-dir", type=Path, default=DEFAULT_OUTPUT_DIR)
    parser.add_argument("--models", default=",".join(DEFAULT_MODELS))
    parser.add_argument("--threshold", type=float, default=0.5)
    parser.add_argument("--precision-targets", default="0.5,0.7,0.8,0.9")
    parser.add_argument("--calibration-bins", type=int, default=10)
    parser.add_argument("--random-state", type=int, default=42)
    parser.add_argument("--sample-rows", type=int, default=None)
    parser.add_argument("--n-jobs", type=int, default=-1)
    parser.add_argument("--max-iter", type=int, default=500)
    parser.add_argument("--rf-n-estimators", type=int, default=100)
    parser.add_argument("--rf-max-depth", type=int, default=12)
    parser.add_argument("--xgb-n-estimators", type=int, default=50)
    parser.add_argument("--xgb-max-depth", type=int, default=4)
    parser.add_argument("--xgb-learning-rate", type=float, default=0.05)
    parser.add_argument(
        "--validate-only",
        action="store_true",
        help="Validate feature matrix and manifest shape without importing ML dependencies.",
    )
    parsed = parser.parse_args()
    parsed.models = parse_models(parsed.models)
    parsed.precision_targets = parse_precision_targets(parsed.precision_targets)
    return parsed


def parse_models(value: str) -> list[str]:
    models = [item.strip() for item in value.split(",") if item.strip()]
    if not models:
        raise SystemExit("At least one model must be selected.")

    unsupported = sorted(set(models).difference(SUPPORTED_MODELS))
    if unsupported:
        raise SystemExit(f"Unsupported models: {', '.join(unsupported)}")
    return models


def parse_precision_targets(value: str) -> list[float]:
    targets = []
    for item in value.split(","):
        item = item.strip()
        if not item:
            continue
        target = float(item)
        if target < 0 or target > 1:
            raise SystemExit(f"Precision target must be between 0 and 1: {target}")
        targets.append(target)
    return targets or [0.9]


def validate_feature_matrix(feature_matrix: Path, manifest_path: Path) -> dict[str, Any]:
    feature_matrix = resolve_repo_path(feature_matrix)
    manifest_path = resolve_repo_path(manifest_path)
    if not feature_matrix.exists():
        raise SystemExit(f"Feature matrix was not found: {relative_to_repo(feature_matrix)}")

    with feature_matrix.open(newline="", encoding="utf-8-sig") as handle:
        reader = csv.DictReader(handle)
        columns = reader.fieldnames or []
        required = {"split", "label"}
        missing = sorted(required.difference(columns))
        if missing:
            raise SystemExit(f"Feature matrix is missing required columns: {', '.join(missing)}")

        split_counts: Counter[str] = Counter()
        label_counts: Counter[str] = Counter()
        row_count = 0
        for row in reader:
            row_count += 1
            split_counts[row.get("split", "")] += 1
            label_counts[row.get("label", "")] += 1

    if row_count == 0:
        raise SystemExit("Feature matrix has no data rows.")

    training_features = [column for column in columns if column not in EXCLUDED_TRAINING_COLUMNS]
    if not training_features:
        raise SystemExit("Feature matrix has no training feature columns.")

    manifest = load_json(manifest_path) if manifest_path.exists() else {}
    return {
        "rowCount": row_count,
        "columns": columns,
        "trainingFeatureCount": len(training_features),
        "trainingFeatures": training_features,
        "splitCounts": dict(sorted(split_counts.items())),
        "labelCounts": dict(sorted(label_counts.items())),
        "manifestDatasetId": manifest.get("datasetId"),
        "manifestRowCount": manifest.get("rowCount"),
    }


def load_ml_dependencies(models: list[str]) -> dict[str, Any]:
    try:
        import numpy as np
        import pandas as pd
        from sklearn.ensemble import RandomForestClassifier
        from sklearn.linear_model import LogisticRegression
        from sklearn.metrics import (
            average_precision_score,
            brier_score_loss,
            confusion_matrix,
            precision_recall_curve,
            precision_recall_fscore_support,
            roc_auc_score,
        )
        from sklearn.pipeline import Pipeline
        from sklearn.preprocessing import StandardScaler
    except ModuleNotFoundError as exc:
        raise SystemExit(
            "Missing ML dependency: "
            f"{exc.name}. Install the training stack with "
            "`python3 -m pip install -r ml/requirements.txt`."
        ) from exc

    deps = {
        "np": np,
        "pd": pd,
        "RandomForestClassifier": RandomForestClassifier,
        "LogisticRegression": LogisticRegression,
        "Pipeline": Pipeline,
        "StandardScaler": StandardScaler,
        "average_precision_score": average_precision_score,
        "brier_score_loss": brier_score_loss,
        "confusion_matrix": confusion_matrix,
        "precision_recall_curve": precision_recall_curve,
        "precision_recall_fscore_support": precision_recall_fscore_support,
        "roc_auc_score": roc_auc_score,
    }

    if "xgboost" in models:
        try:
            import xgboost as xgb
        except ModuleNotFoundError as exc:
            raise SystemExit(
                "Missing ML dependency: xgboost. Install the training stack with "
                "`python3 -m pip install -r ml/requirements.txt`."
            ) from exc
        deps["xgb"] = xgb

    return deps


def compare_models(args: argparse.Namespace, deps: dict[str, Any]) -> None:
    feature_matrix = resolve_repo_path(args.feature_matrix)
    manifest_path = resolve_repo_path(args.manifest)
    output_dir = resolve_repo_path(args.output_dir)
    output_dir.mkdir(parents=True, exist_ok=True)

    summary = validate_feature_matrix(feature_matrix, manifest_path)
    manifest = load_json(manifest_path) if manifest_path.exists() else {}
    pd = deps["pd"]
    df = pd.read_csv(feature_matrix)
    if args.sample_rows is not None and args.sample_rows < len(df):
        df = stratified_sample(df, args.sample_rows, args.random_state)

    feature_columns = [column for column in df.columns if column not in EXCLUDED_TRAINING_COLUMNS]
    split = df["split"].astype(str).str.lower()
    y = pd.to_numeric(df["label"], errors="coerce").fillna(0).astype(int)
    x = df[feature_columns].apply(pd.to_numeric, errors="coerce").fillna(0.0)

    masks = {
        "train": split == "train",
        "validation": split == "validation",
        "test": split == "test",
    }
    for name, mask in masks.items():
        if int(mask.sum()) == 0:
            raise SystemExit(f"Feature matrix has no rows for split: {name}")

    model_results = []
    all_threshold_rows = []
    all_calibration_rows = []
    trained_models = build_models(args, y[masks["train"]], deps)

    for model_name, model in trained_models.items():
        model.fit(x.loc[masks["train"]], y[masks["train"]])
        split_results = {}
        for split_name in ("validation", "test"):
            split_scores = predict_scores(model, x.loc[masks[split_name]])
            metrics = evaluate_scores(
                y_true=y[masks[split_name]],
                scores=split_scores,
                threshold=args.threshold,
                precision_targets=args.precision_targets,
                calibration_bins=args.calibration_bins,
                deps=deps,
            )
            split_results[split_name] = metrics
            model_results.append(comparison_row(model_name, split_name, metrics))
            all_threshold_rows.extend(threshold_rows(model_name, split_name, metrics))
            all_calibration_rows.extend(calibration_rows(model_name, split_name, metrics))

        write_json(output_dir / f"{model_name}-metrics.json", {
            "generatedAt": timestamp(),
            "dataset": dataset_context(manifest, summary, df),
            "model": model_context(model_name, model, feature_columns, args),
            "evaluation": split_results,
        })

    write_csv(output_dir / "model-comparison.csv", model_results)
    write_csv(output_dir / "threshold-operating-points.csv", all_threshold_rows)
    write_csv(output_dir / "calibration-bins.csv", all_calibration_rows)
    write_report(output_dir / "comparison-report.md", manifest, summary, df, model_results, all_threshold_rows)

    print(
        "Compared tabular models "
        f"models={','.join(trained_models)} "
        f"rows={len(df)} "
        f"features={len(feature_columns)} "
        f"report={relative_to_repo(output_dir / 'comparison-report.md')}"
    )


def stratified_sample(df: Any, sample_rows: int, random_state: int) -> Any:
    import pandas as pd

    total = len(df)
    if sample_rows <= 0:
        raise SystemExit("--sample-rows must be greater than zero.")

    pieces = []
    group_columns = ["split", "label"]
    grouped = df.groupby(group_columns, group_keys=False)
    for _, group in grouped:
        group_target = max(1, round(sample_rows * len(group) / total))
        pieces.append(group.sample(n=min(len(group), group_target), random_state=random_state))
    return pd.concat(pieces).sample(frac=1, random_state=random_state).reset_index(drop=True)


def build_models(args: argparse.Namespace, y_train: Any, deps: dict[str, Any]) -> dict[str, Any]:
    positives = int((y_train == 1).sum())
    negatives = int((y_train == 0).sum())
    scale_pos_weight = float(negatives / positives) if positives else 1.0
    models = {}

    for name in args.models:
        if name == "logistic_regression":
            models[name] = deps["Pipeline"]([
                ("scale", deps["StandardScaler"]()),
                ("model", deps["LogisticRegression"](
                    class_weight="balanced",
                    max_iter=args.max_iter,
                    n_jobs=args.n_jobs,
                    random_state=args.random_state,
                )),
            ])
        elif name == "random_forest":
            models[name] = deps["RandomForestClassifier"](
                n_estimators=args.rf_n_estimators,
                max_depth=args.rf_max_depth,
                class_weight="balanced_subsample",
                n_jobs=args.n_jobs,
                random_state=args.random_state,
            )
        elif name == "xgboost":
            models[name] = deps["xgb"].XGBClassifier(
                n_estimators=args.xgb_n_estimators,
                max_depth=args.xgb_max_depth,
                learning_rate=args.xgb_learning_rate,
                subsample=0.8,
                colsample_bytree=0.8,
                objective="binary:logistic",
                eval_metric="aucpr",
                tree_method="hist",
                scale_pos_weight=scale_pos_weight,
                n_jobs=args.n_jobs,
                random_state=args.random_state,
            )
    return models


def predict_scores(model: Any, x: Any) -> Any:
    if hasattr(model, "predict_proba"):
        return model.predict_proba(x)[:, 1]
    if hasattr(model, "decision_function"):
        scores = model.decision_function(x)
        np = load_ml_dependencies([])["np"]
        return 1 / (1 + np.exp(-scores))
    raise SystemExit(f"Model does not expose probabilities: {model}")


def evaluate_scores(
    y_true: Any,
    scores: Any,
    threshold: float,
    precision_targets: list[float],
    calibration_bins: int,
    deps: dict[str, Any],
) -> dict[str, Any]:
    predictions = (scores >= threshold).astype(int)
    precision, recall, f1, _ = deps["precision_recall_fscore_support"](
        y_true,
        predictions,
        average="binary",
        zero_division=0,
    )
    tn, fp, fn, tp = deps["confusion_matrix"](y_true, predictions, labels=[0, 1]).ravel()
    return {
        "rowCount": int(len(y_true)),
        "positiveLabelCount": int((y_true == 1).sum()),
        "negativeLabelCount": int((y_true == 0).sum()),
        "rocAuc": safe_metric(deps["roc_auc_score"], y_true, scores),
        "prAuc": safe_metric(deps["average_precision_score"], y_true, scores),
        "brierScore": safe_metric(deps["brier_score_loss"], y_true, scores),
        "fixedThreshold": {
            "threshold": threshold,
            "precision": float(precision),
            "recall": float(recall),
            "f1": float(f1),
            "falsePositiveRate": float(fp / (fp + tn)) if (fp + tn) else 0.0,
            "confusionMatrix": {
                "trueNegative": int(tn),
                "falsePositive": int(fp),
                "falseNegative": int(fn),
                "truePositive": int(tp),
            },
        },
        "operatingPoints": operating_points(y_true, scores, precision_targets, deps),
        "calibrationBins": build_calibration_bins(y_true, scores, calibration_bins, deps),
    }


def safe_metric(metric: Any, y_true: Any, scores: Any) -> float | None:
    try:
        value = metric(y_true, scores)
    except ValueError:
        return None
    if isinstance(value, float) and math.isnan(value):
        return None
    return float(value)


def operating_points(y_true: Any, scores: Any, precision_targets: list[float], deps: dict[str, Any]) -> dict[str, Any]:
    precision, recall, thresholds = deps["precision_recall_curve"](y_true, scores)
    threshold_points = [
        threshold_point(thresholds[index], precision[index], recall[index])
        for index in range(len(thresholds))
    ]
    if not threshold_points:
        empty = {"threshold": None, "precision": 0.0, "recall": 0.0, "f1": 0.0}
        return {"bestF1": empty, "precisionTargets": {str(target): empty for target in precision_targets}}

    targets = {}
    for target in precision_targets:
        eligible = [point for point in threshold_points if point["precision"] >= target]
        targets[f"{target:.2f}"] = max(eligible, key=lambda point: point["recall"]) if eligible else {
            "threshold": None,
            "precision": 0.0,
            "recall": 0.0,
            "f1": 0.0,
        }
    return {
        "bestF1": max(threshold_points, key=lambda point: point["f1"]),
        "precisionTargets": targets,
    }


def threshold_point(threshold: Any, precision: Any, recall: Any) -> dict[str, float]:
    precision_value = float(precision)
    recall_value = float(recall)
    f1 = 0.0
    if precision_value + recall_value:
        f1 = 2 * precision_value * recall_value / (precision_value + recall_value)
    return {
        "threshold": float(threshold),
        "precision": precision_value,
        "recall": recall_value,
        "f1": f1,
    }


def build_calibration_bins(y_true: Any, scores: Any, bins: int, deps: dict[str, Any]) -> list[dict[str, Any]]:
    pd = deps["pd"]
    if bins <= 0:
        return []

    frame = pd.DataFrame({"label": y_true.to_numpy(), "score": scores})
    frame["bin"] = pd.qcut(frame["score"].rank(method="first"), q=bins, labels=False)
    rows = []
    for bin_id, group in frame.groupby("bin", sort=True):
        positives = int(group["label"].sum())
        rows.append({
            "bin": int(bin_id),
            "rowCount": int(len(group)),
            "positiveLabelCount": positives,
            "averageScore": float(group["score"].mean()),
            "observedFraudRate": float(positives / len(group)) if len(group) else 0.0,
            "minimumScore": float(group["score"].min()),
            "maximumScore": float(group["score"].max()),
        })
    return rows


def comparison_row(model_name: str, split_name: str, metrics: dict[str, Any]) -> dict[str, Any]:
    fixed = metrics["fixedThreshold"]
    best_f1 = metrics["operatingPoints"]["bestF1"]
    return {
        "model": model_name,
        "split": split_name,
        "rowCount": metrics["rowCount"],
        "positiveLabelCount": metrics["positiveLabelCount"],
        "rocAuc": metrics["rocAuc"],
        "prAuc": metrics["prAuc"],
        "brierScore": metrics["brierScore"],
        "fixedThreshold": fixed["threshold"],
        "fixedPrecision": fixed["precision"],
        "fixedRecall": fixed["recall"],
        "fixedF1": fixed["f1"],
        "fixedFalsePositiveRate": fixed["falsePositiveRate"],
        "bestF1Threshold": best_f1["threshold"],
        "bestF1Precision": best_f1["precision"],
        "bestF1Recall": best_f1["recall"],
        "bestF1": best_f1["f1"],
    }


def threshold_rows(model_name: str, split_name: str, metrics: dict[str, Any]) -> list[dict[str, Any]]:
    rows = [{
        "model": model_name,
        "split": split_name,
        "strategy": "best_f1",
        "precisionTarget": "",
        **metrics["operatingPoints"]["bestF1"],
    }]
    for target, point in metrics["operatingPoints"]["precisionTargets"].items():
        rows.append({
            "model": model_name,
            "split": split_name,
            "strategy": "minimum_precision",
            "precisionTarget": target,
            **point,
        })
    return rows


def calibration_rows(model_name: str, split_name: str, metrics: dict[str, Any]) -> list[dict[str, Any]]:
    return [
        {
            "model": model_name,
            "split": split_name,
            **row,
        }
        for row in metrics["calibrationBins"]
    ]


def dataset_context(manifest: dict[str, Any], summary: dict[str, Any], df: Any) -> dict[str, Any]:
    return {
        "id": manifest.get("datasetId"),
        "name": manifest.get("datasetName"),
        "rowCount": int(len(df)),
        "sourceRowCount": summary["rowCount"],
        "labelCounts": dict(sorted(Counter(str(value) for value in df["label"]).items())),
        "splitCounts": dict(sorted(Counter(str(value) for value in df["split"]).items())),
        "source": manifest.get("source", {}),
    }


def model_context(model_name: str, model: Any, feature_columns: list[str], args: argparse.Namespace) -> dict[str, Any]:
    return {
        "name": model_name,
        "trainingFeatureCount": len(feature_columns),
        "features": feature_columns,
        "parameters": serializable_params(model),
        "threshold": args.threshold,
        "precisionTargets": args.precision_targets,
    }


def serializable_params(model: Any) -> dict[str, Any]:
    if hasattr(model, "get_params"):
        return json_safe(model.get_params())
    return {}


def json_safe(value: Any) -> Any:
    if isinstance(value, dict):
        return {key: json_safe(item) for key, item in value.items()}
    if isinstance(value, (list, tuple)):
        return [json_safe(item) for item in value]
    if isinstance(value, (str, int, float, bool)) or value is None:
        return value
    return str(value)


def write_report(
    path: Path,
    manifest: dict[str, Any],
    summary: dict[str, Any],
    df: Any,
    comparison_rows: list[dict[str, Any]],
    operating_rows: list[dict[str, Any]],
) -> None:
    lines = [
        "# Model Comparison Report",
        "",
        f"Generated: {timestamp()}",
        "",
        "## Dataset",
        "",
        f"- Dataset id: `{manifest.get('datasetId')}`",
        f"- Rows used: `{len(df)}`",
        f"- Source rows: `{summary['rowCount']}`",
        f"- Label counts: `{json.dumps(dict(sorted(Counter(str(value) for value in df['label']).items())), sort_keys=True)}`",
        f"- Training features: `{summary['trainingFeatureCount']}`",
        "",
        "## Model Comparison",
        "",
        "| Model | Split | PR-AUC | ROC-AUC | Brier | Fixed Precision | Fixed Recall | Best F1 | Best F1 Threshold |",
        "| --- | --- | --- | --- | --- | --- | --- | --- | --- |",
    ]
    for row in comparison_rows:
        lines.append(
            f"| {row['model']} | {row['split']} | {format_metric(row['prAuc'])} | "
            f"{format_metric(row['rocAuc'])} | {format_metric(row['brierScore'])} | "
            f"{float(row['fixedPrecision']):.4f} | {float(row['fixedRecall']):.4f} | "
            f"{float(row['bestF1']):.4f} | {format_metric(row['bestF1Threshold'])} |"
        )

    lines.extend([
        "",
        "## Precision Target Operating Points",
        "",
        "| Model | Split | Target | Threshold | Precision | Recall | F1 |",
        "| --- | --- | --- | --- | --- | --- | --- |",
    ])
    for row in operating_rows:
        if row["strategy"] != "minimum_precision":
            continue
        lines.append(
            f"| {row['model']} | {row['split']} | {row['precisionTarget']} | "
            f"{format_metric(row['threshold'])} | {float(row['precision']):.4f} | "
            f"{float(row['recall']):.4f} | {float(row['f1']):.4f} |"
        )

    lines.extend([
        "",
        "## Interpretation",
        "",
        "Use PR-AUC and precision-target operating points as the main fraud metrics. "
        "Accuracy is not reported because the dataset is highly imbalanced and would hide poor fraud detection.",
        "",
    ])
    path.write_text("\n".join(lines), encoding="utf-8")


def write_csv(path: Path, rows: list[dict[str, Any]]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    columns = []
    for row in rows:
        for column in row.keys():
            if column not in columns:
                columns.append(column)
    with path.open("w", newline="", encoding="utf-8") as handle:
        writer = csv.DictWriter(handle, fieldnames=columns)
        writer.writeheader()
        writer.writerows(rows)


def load_json(path: Path) -> dict[str, Any]:
    with resolve_repo_path(path).open(encoding="utf-8") as handle:
        return json.load(handle)


def write_json(path: Path, payload: dict[str, Any]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("w", encoding="utf-8") as handle:
        json.dump(payload, handle, indent=2, sort_keys=True)
        handle.write("\n")


def format_metric(value: float | None) -> str:
    return "n/a" if value is None else f"{float(value):.4f}"


def timestamp() -> str:
    return datetime.now(timezone.utc).isoformat().replace("+00:00", "Z")


def resolve_repo_path(path: Path) -> Path:
    return path if path.is_absolute() else REPO_ROOT / path


def relative_to_repo(path: Path) -> str:
    try:
        return str(path.resolve().relative_to(REPO_ROOT))
    except ValueError:
        return str(path)


if __name__ == "__main__":
    raise SystemExit(main())
