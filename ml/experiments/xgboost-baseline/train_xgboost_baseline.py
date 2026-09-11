#!/usr/bin/env python3
"""Train the first OEFMS XGBoost tabular fraud baseline.

The script has a standard-library validation mode so repository smoke checks can
verify the dataset contract without installing the full ML toolchain. Actual
training requires the packages listed in ml/requirements.txt.
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
DEFAULT_OUTPUT_DIR = REPO_ROOT / "ml" / "artifacts" / "xgboost-baseline"
EXCLUDED_TRAINING_COLUMNS = {"transaction_id", "tenant_id", "split", "label"}


def main() -> int:
    args = parse_args()
    if args.validate_only:
        summary = validate_feature_matrix(args.feature_matrix, args.manifest)
        print(
            "Validated feature matrix "
            f"{relative_to_repo(args.feature_matrix)} with {summary['rowCount']} rows, "
            f"{summary['trainingFeatureCount']} training features"
        )
        return 0

    deps = load_ml_dependencies()
    train_baseline(args, deps)
    return 0


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Train an OEFMS XGBoost fraud baseline.")
    parser.add_argument("--feature-matrix", type=Path, default=DEFAULT_FEATURE_MATRIX)
    parser.add_argument("--manifest", type=Path, default=DEFAULT_MANIFEST)
    parser.add_argument("--output-dir", type=Path, default=DEFAULT_OUTPUT_DIR)
    parser.add_argument("--threshold", type=float, default=0.5)
    parser.add_argument("--min-precision", type=float, default=0.9)
    parser.add_argument("--n-estimators", type=int, default=300)
    parser.add_argument("--max-depth", type=int, default=4)
    parser.add_argument("--learning-rate", type=float, default=0.05)
    parser.add_argument("--subsample", type=float, default=0.8)
    parser.add_argument("--colsample-bytree", type=float, default=0.8)
    parser.add_argument("--explain-sample-size", type=int, default=25)
    parser.add_argument(
        "--validate-only",
        action="store_true",
        help="Validate feature matrix and manifest shape without importing ML dependencies.",
    )
    return parser.parse_args()


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


def load_ml_dependencies() -> dict[str, Any]:
    try:
        import pandas as pd
        import xgboost as xgb
        from sklearn.metrics import (
            average_precision_score,
            confusion_matrix,
            precision_recall_curve,
            precision_recall_fscore_support,
            roc_auc_score,
        )
    except ModuleNotFoundError as exc:
        raise SystemExit(
            "Missing ML dependency: "
            f"{exc.name}. Install the training stack with "
            "`python3 -m pip install -r ml/requirements.txt`."
        ) from exc

    return {
        "pd": pd,
        "xgb": xgb,
        "average_precision_score": average_precision_score,
        "confusion_matrix": confusion_matrix,
        "precision_recall_curve": precision_recall_curve,
        "precision_recall_fscore_support": precision_recall_fscore_support,
        "roc_auc_score": roc_auc_score,
    }


def train_baseline(args: argparse.Namespace, deps: dict[str, Any]) -> None:
    feature_matrix = resolve_repo_path(args.feature_matrix)
    manifest_path = resolve_repo_path(args.manifest)
    output_dir = resolve_repo_path(args.output_dir)
    summary = validate_feature_matrix(feature_matrix, manifest_path)
    manifest = load_json(manifest_path) if manifest_path.exists() else {}

    pd = deps["pd"]
    xgb = deps["xgb"]
    df = pd.read_csv(feature_matrix)
    feature_columns = [column for column in df.columns if column not in EXCLUDED_TRAINING_COLUMNS]
    split = df["split"].astype(str).str.lower()
    y = pd.to_numeric(df["label"], errors="coerce").fillna(0).astype(int)
    x = df[feature_columns].apply(pd.to_numeric, errors="coerce").fillna(0.0)

    train_mask = split == "train"
    validation_mask = split == "validation"
    test_mask = split == "test"
    ensure_split("train", train_mask)
    ensure_split("validation", validation_mask)
    ensure_split("test", test_mask)

    y_train = y[train_mask]
    positives = int((y_train == 1).sum())
    negatives = int((y_train == 0).sum())
    scale_pos_weight = float(negatives / positives) if positives else 1.0

    model = xgb.XGBClassifier(
        n_estimators=args.n_estimators,
        max_depth=args.max_depth,
        learning_rate=args.learning_rate,
        subsample=args.subsample,
        colsample_bytree=args.colsample_bytree,
        objective="binary:logistic",
        eval_metric="aucpr",
        tree_method="hist",
        scale_pos_weight=scale_pos_weight,
        random_state=42,
    )
    model.fit(
        x.loc[train_mask],
        y_train,
        eval_set=[(x.loc[validation_mask], y[validation_mask])],
        verbose=False,
    )

    output_dir.mkdir(parents=True, exist_ok=True)
    model_path = output_dir / "xgboost-baseline.ubj"
    model.save_model(model_path)

    metrics = {
        "generatedAt": datetime.now(timezone.utc).isoformat().replace("+00:00", "Z"),
        "dataset": {
            "id": manifest.get("datasetId"),
            "name": manifest.get("datasetName"),
            "rowCount": summary["rowCount"],
            "labelCounts": summary["labelCounts"],
            "splitCounts": summary["splitCounts"],
            "source": manifest.get("source", {}),
        },
        "model": {
            "name": "xgboost-baseline",
            "artifact": relative_to_repo(model_path),
            "threshold": args.threshold,
            "minimumPrecisionTarget": args.min_precision,
            "trainingFeatureCount": len(feature_columns),
            "scalePosWeight": scale_pos_weight,
            "parameters": model.get_params(),
        },
        "evaluation": {
            "validation": evaluate_split(y[validation_mask], predict_scores(model, x.loc[validation_mask]), args, deps),
            "test": evaluate_split(y[test_mask], predict_scores(model, x.loc[test_mask]), args, deps),
        },
    }

    write_json(output_dir / "metrics.json", metrics)
    write_json(output_dir / "feature-columns.json", {"featureColumns": feature_columns})
    write_feature_importance(output_dir / "feature-importance.csv", feature_columns, model)
    write_explanation_sample(
        output_dir / "feature-contributions-sample.csv",
        xgb,
        model,
        x.loc[test_mask],
        df.loc[test_mask],
        y[test_mask],
        feature_columns,
        args.explain_sample_size,
    )
    write_model_card(output_dir / "model-card.md", metrics)

    print(
        "Trained XGBoost baseline "
        f"dataset={manifest.get('datasetId', 'unknown')} "
        f"features={len(feature_columns)} "
        f"model={relative_to_repo(model_path)} "
        f"metrics={relative_to_repo(output_dir / 'metrics.json')}"
    )


def ensure_split(name: str, mask: Any) -> None:
    if int(mask.sum()) == 0:
        raise SystemExit(f"Feature matrix has no rows for split: {name}")


def predict_scores(model: Any, x: Any) -> Any:
    return model.predict_proba(x)[:, 1]


def evaluate_split(y_true: Any, scores: Any, args: argparse.Namespace, deps: dict[str, Any]) -> dict[str, Any]:
    predictions = (scores >= args.threshold).astype(int)
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
        "precision": float(precision),
        "recall": float(recall),
        "f1": float(f1),
        "falsePositiveRate": float(fp / (fp + tn)) if (fp + tn) else 0.0,
        "recallAtMinimumPrecision": recall_at_minimum_precision(y_true, scores, args.min_precision, deps),
        "confusionMatrix": {
            "trueNegative": int(tn),
            "falsePositive": int(fp),
            "falseNegative": int(fn),
            "truePositive": int(tp),
        },
    }


def safe_metric(metric: Any, y_true: Any, scores: Any) -> float | None:
    try:
        value = metric(y_true, scores)
    except ValueError:
        return None
    if isinstance(value, float) and math.isnan(value):
        return None
    return float(value)


def recall_at_minimum_precision(y_true: Any, scores: Any, minimum_precision: float, deps: dict[str, Any]) -> float:
    precision, recall, _ = deps["precision_recall_curve"](y_true, scores)
    eligible = [float(recall_value) for precision_value, recall_value in zip(precision, recall)
                if precision_value >= minimum_precision]
    return max(eligible) if eligible else 0.0


def write_feature_importance(path: Path, feature_columns: list[str], model: Any) -> None:
    rows = sorted(
        zip(feature_columns, model.feature_importances_),
        key=lambda item: item[1],
        reverse=True,
    )
    with path.open("w", newline="", encoding="utf-8") as handle:
        writer = csv.writer(handle)
        writer.writerow(["rank", "feature", "importance"])
        for index, (feature, importance) in enumerate(rows, start=1):
            writer.writerow([index, feature, f"{float(importance):.12f}"])


def write_explanation_sample(
    path: Path,
    xgb: Any,
    model: Any,
    x_test: Any,
    df_test: Any,
    y_test: Any,
    feature_columns: list[str],
    sample_size: int,
) -> None:
    if sample_size <= 0:
        return

    x_sample = x_test.head(sample_size)
    df_sample = df_test.head(sample_size)
    y_sample = y_test.head(sample_size)
    scores = predict_scores(model, x_sample)
    contributions = model.get_booster().predict(
        xgb.DMatrix(x_sample, feature_names=feature_columns),
        pred_contribs=True,
    )

    with path.open("w", newline="", encoding="utf-8") as handle:
        writer = csv.writer(handle)
        writer.writerow(["transaction_id", "actual_label", "score", "top_contributions"])
        for index, (_, row) in enumerate(df_sample.iterrows()):
            feature_contributions = contributions[index][:-1]
            ranked = sorted(
                zip(feature_columns, feature_contributions),
                key=lambda item: abs(item[1]),
                reverse=True,
            )[:5]
            writer.writerow([
                row.get("transaction_id", ""),
                int(y_sample.iloc[index]),
                f"{float(scores[index]):.8f}",
                json.dumps([
                    {"feature": feature, "contribution": float(contribution)}
                    for feature, contribution in ranked
                ]),
            ])


def write_model_card(path: Path, metrics: dict[str, Any]) -> None:
    validation = metrics["evaluation"]["validation"]
    test = metrics["evaluation"]["test"]
    top_features = read_top_features(path.parent / "feature-importance.csv", limit=10)
    lines = [
        "# XGBoost Baseline Model Card",
        "",
        f"Generated: {metrics['generatedAt']}",
        "",
        "## Dataset",
        "",
        f"- Dataset id: `{metrics['dataset']['id']}`",
        f"- Rows: `{metrics['dataset']['rowCount']}`",
        f"- Label counts: `{json.dumps(metrics['dataset']['labelCounts'], sort_keys=True)}`",
        f"- Split counts: `{json.dumps(metrics['dataset']['splitCounts'], sort_keys=True)}`",
        "",
        "## Model",
        "",
        f"- Model name: `{metrics['model']['name']}`",
        f"- Artifact: `{metrics['model']['artifact']}`",
        f"- Training features: `{metrics['model']['trainingFeatureCount']}`",
        f"- Positive class weight: `{metrics['model']['scalePosWeight']:.6f}`",
        "",
        "## Evaluation",
        "",
        "| Split | PR-AUC | ROC-AUC | Precision | Recall | F1 | False Positive Rate |",
        "| --- | --- | --- | --- | --- | --- | --- |",
        evaluation_row("validation", validation),
        evaluation_row("test", test),
        "",
        "## Top Features",
        "",
    ]
    lines.extend(f"- `{feature}`: `{importance}`" for feature, importance in top_features)
    lines.extend([
        "",
        "## Runtime Handoff",
        "",
        "A production scorer must map this model output to the Java `MlScoringClient` contract and return "
        "calibrated scores, recommended decisions, and feature-level explanations for investigator review.",
        "",
    ])
    path.write_text("\n".join(lines), encoding="utf-8")


def evaluation_row(name: str, values: dict[str, Any]) -> str:
    return (
        f"| {name} | {format_metric(values['prAuc'])} | {format_metric(values['rocAuc'])} | "
        f"{values['precision']:.4f} | {values['recall']:.4f} | {values['f1']:.4f} | "
        f"{values['falsePositiveRate']:.4f} |"
    )


def format_metric(value: float | None) -> str:
    return "n/a" if value is None else f"{value:.4f}"


def read_top_features(path: Path, limit: int) -> list[tuple[str, str]]:
    if not path.exists():
        return []

    with path.open(newline="", encoding="utf-8") as handle:
        reader = csv.DictReader(handle)
        return [
            (row["feature"], row["importance"])
            for _, row in zip(range(limit), reader)
        ]


def load_json(path: Path) -> dict[str, Any]:
    with resolve_repo_path(path).open(encoding="utf-8") as handle:
        return json.load(handle)


def write_json(path: Path, payload: dict[str, Any]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("w", encoding="utf-8") as handle:
        json.dump(payload, handle, indent=2, sort_keys=True)
        handle.write("\n")


def resolve_repo_path(path: Path) -> Path:
    return path if path.is_absolute() else REPO_ROOT / path


def relative_to_repo(path: Path) -> str:
    try:
        return str(path.resolve().relative_to(REPO_ROOT))
    except ValueError:
        return str(path)


if __name__ == "__main__":
    raise SystemExit(main())
