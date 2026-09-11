#!/usr/bin/env bash
set -euo pipefail

dataset_id="${1:-oefms_synthetic_sample}"
python_cmd="${PYTHON_CMD:-python3}"

"$python_cmd" ml/pipelines/prepare_tabular_dataset.py --dataset-id "$dataset_id"
