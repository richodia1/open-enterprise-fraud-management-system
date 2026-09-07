#!/usr/bin/env bash
set -euo pipefail

tmp_dir="${TMPDIR:-/tmp}"
tmp_dir="${tmp_dir%/}"
compose_output="$(mktemp "$tmp_dir/openefms-compose-config.XXXXXX")"

docker compose config > "$compose_output"
echo "Docker Compose config is valid: $compose_output"

if command -v xmllint >/dev/null 2>&1; then
  find . -name pom.xml -print0 | xargs -0 xmllint --noout
  echo "Maven POM XML files are valid."
else
  echo "xmllint is not installed; skipped Maven POM XML validation."
fi
