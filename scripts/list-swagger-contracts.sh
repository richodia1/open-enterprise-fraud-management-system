#!/usr/bin/env bash
set -euo pipefail

host="${OEFMS_SWAGGER_HOST:-localhost}"
scheme="${OEFMS_SWAGGER_SCHEME:-http}"
check=false

usage() {
  cat <<'USAGE'
Usage: ./scripts/list-swagger-contracts.sh [options]

Options:
  --check      Probe each /v3/api-docs endpoint and show availability.
  -h, --help   Show this help text.

Environment:
  OEFMS_SWAGGER_HOST     Host used for generated URLs. Default: localhost
  OEFMS_SWAGGER_SCHEME   URL scheme used for generated URLs. Default: http
USAGE
}

parse_args() {
  while [ "$#" -gt 0 ]; do
    case "$1" in
      --check)
        check=true
        ;;
      -h|--help)
        usage
        exit 0
        ;;
      *)
        printf 'Unknown option: %s\n' "$1" >&2
        usage >&2
        exit 2
        ;;
    esac
    shift
  done
}

service_contracts=(
  "API Gateway|8080"
  "Fraud Detection Service|8081"
  "Rule Management Service|8082"
  "List Management Service|8083"
  "Case Management Service|8084"
  "Auth Service|8085"
  "Administration Service|8086"
  "Identity Service|8087"
  "Configuration Management Service|8088"
  "Ingestion Management Service|8089"
  "Audit Logging Service|8090"
  "Notification Management Service|8091"
  "Report Management Service|8092"
  "GDPR Service|8093"
  "Explainable AI Service|8094"
)

availability() {
  local swagger_url="$1"
  local api_docs_url="$2"

  if curl -fsSL "$swagger_url" >/dev/null 2>&1 && curl -fsS "$api_docs_url" >/dev/null 2>&1; then
    printf 'available'
    return 0
  fi

  printf 'not reachable'
  return 1
}

parse_args "$@"

if [ "$check" = true ]; then
  command -v curl >/dev/null 2>&1 || {
    printf 'Required command not found: curl\n' >&2
    exit 1
  }
fi

printf '| Service | Swagger UI | OpenAPI JSON'
if [ "$check" = true ]; then
  printf ' | Status'
fi
printf ' |\n'

printf '| --- | --- | ---'
if [ "$check" = true ]; then
  printf ' | ---'
fi
printf ' |\n'

exit_code=0
for contract in "${service_contracts[@]}"; do
  IFS='|' read -r service port <<<"$contract"
  swagger_url="${scheme}://${host}:${port}/swagger-ui.html"
  api_docs_url="${scheme}://${host}:${port}/v3/api-docs"

  printf '| %s | %s | %s' "$service" "$swagger_url" "$api_docs_url"
  if [ "$check" = true ]; then
    status="$(availability "$swagger_url" "$api_docs_url")" || exit_code=1
    printf ' | %s' "$status"
  fi
  printf ' |\n'
done

exit "$exit_code"
