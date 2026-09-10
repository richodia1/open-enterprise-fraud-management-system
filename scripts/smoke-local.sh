#!/usr/bin/env bash
set -euo pipefail

build_project=true
live_checks=false
start_stack=false
gateway_checks=false

env_file="${OEFMS_SMOKE_ENV_FILE:-.env.example}"
maven_cmd="${MAVEN_CMD:-mvn}"
health_timeout_seconds="${OEFMS_SMOKE_HEALTH_TIMEOUT_SECONDS:-180}"
health_interval_seconds="${OEFMS_SMOKE_HEALTH_INTERVAL_SECONDS:-3}"
fraud_service_url="${OEFMS_SMOKE_FRAUD_SERVICE_URL:-http://localhost:8081}"
gateway_url="${OEFMS_SMOKE_GATEWAY_URL:-http://localhost:8080}"
gateway_fraud_decision_path="${OEFMS_SMOKE_GATEWAY_FRAUD_DECISION_PATH:-/api/v1/fraud/decisions}"

core_services=(
  postgres
  rabbitmq
  redis
  auth-service
  ingestion-management-service
  fraud-detection-service
  explainable-ai-service
  case-management-service
)

usage() {
  cat <<'USAGE'
Usage: ./scripts/smoke-local.sh [options]

Options:
  --skip-build    Skip mvn clean verify.
  --live          Probe an already-running local stack.
  --start-stack   Start the core Phase 1 stack, then run live probes.
  --gateway       Include API Gateway health and optional route probes.
  -h, --help      Show this help text.

Environment:
  MAVEN_CMD                                 Maven executable. Default: mvn
  OEFMS_SMOKE_ENV_FILE                     Compose env file. Default: .env.example
  OEFMS_SMOKE_FRAUD_SERVICE_URL            Fraud service URL. Default: http://localhost:8081
  OEFMS_SMOKE_GATEWAY_URL                  Gateway URL. Default: http://localhost:8080
  OEFMS_SMOKE_GATEWAY_FRAUD_DECISION_PATH  Gateway fraud decision path. Default: /api/v1/fraud/decisions
  OEFMS_SMOKE_HEALTH_TIMEOUT_SECONDS       Health wait timeout. Default: 180
  OEFMS_SMOKE_HEALTH_INTERVAL_SECONDS      Health poll interval. Default: 3
USAGE
}

log() {
  printf '[smoke] %s\n' "$*"
}

fail() {
  printf '[smoke] ERROR: %s\n' "$*" >&2
  exit 1
}

require_command() {
  command -v "$1" >/dev/null 2>&1 || fail "Required command not found: $1"
}

parse_args() {
  while [ "$#" -gt 0 ]; do
    case "$1" in
      --skip-build)
        build_project=false
        ;;
      --live)
        live_checks=true
        ;;
      --start-stack)
        start_stack=true
        live_checks=true
        ;;
      --gateway)
        gateway_checks=true
        ;;
      -h|--help)
        usage
        exit 0
        ;;
      *)
        fail "Unknown option: $1"
        ;;
    esac
    shift
  done
}

compose() {
  docker compose --env-file "$env_file" "$@"
}

wait_for_http() {
  local name="$1"
  local url="$2"
  local deadline
  deadline=$((SECONDS + health_timeout_seconds))

  log "Waiting for ${name}: ${url}"
  until curl -fsS "$url" >/dev/null 2>&1; do
    if [ "$SECONDS" -ge "$deadline" ]; then
      fail "${name} did not become healthy within ${health_timeout_seconds}s"
    fi
    sleep "$health_interval_seconds"
  done
  log "${name} is healthy"
}

decision_payload() {
  cat <<'JSON'
{
  "transactionId": "118f3bd4-b208-44ff-b1ff-447c78d80d02",
  "externalReference": "SMOKE-LOW-RISK-001",
  "tenantId": "smoke-tenant",
  "transactionType": "TRANSFER",
  "channel": "MOBILE",
  "direction": "OUTBOUND",
  "amount": {
    "value": 25.00,
    "currency": "GBP"
  },
  "originator": {
    "partyReference": "originator-smoke",
    "customerReference": "originator-customer",
    "accountReference": "originator-account",
    "accountNumberHash": "originator-account-hash",
    "institutionCode": "SMOKE",
    "countryCode": "GB",
    "attributes": {}
  },
  "beneficiary": {
    "partyReference": "beneficiary-smoke",
    "customerReference": "beneficiary-customer",
    "accountReference": "beneficiary-account",
    "accountNumberHash": "beneficiary-account-hash",
    "institutionCode": "SMOKE",
    "countryCode": "GB",
    "attributes": {}
  },
  "initiatedAt": "2026-09-08T09:00:00Z",
  "attributes": {}
}
JSON
}

probe_fraud_decision() {
  local url="$1"
  local response

  log "Posting smoke transaction to ${url}"
  response="$(curl -fsS \
    -H 'Content-Type: application/json' \
    -d "$(decision_payload)" \
    "$url")"

  printf '%s' "$response" | grep -q '"mode":"RULES"' || fail "Fraud decision response did not use RULES mode"
  printf '%s' "$response" | grep -q '"outcome":"APPROVE"' || fail "Fraud decision response did not approve the low-risk smoke transaction"
  printf '%s' "$response" | grep -q '"ruleHitCount":"0"' || fail "Fraud decision response did not report zero rule hits"
  log "Fraud decision probe passed"
}

run_static_checks() {
  require_command docker

  [ -f "$env_file" ] || fail "Environment file not found: $env_file"
  log "Validating Docker Compose config with ${env_file}"
  compose config >/dev/null

  if [ "$build_project" = true ]; then
    require_command "$maven_cmd"
    log "Running Maven verification"
    "$maven_cmd" clean verify
  else
    log "Skipping Maven verification"
  fi
}

run_live_checks() {
  require_command curl

  if [ "$start_stack" = true ]; then
    docker info >/dev/null 2>&1 || fail "Docker daemon is not running; start Docker before using --start-stack"
    log "Starting core Phase 1 services"
    compose up -d --build "${core_services[@]}"
  fi

  wait_for_http "Auth Service" "${OEFMS_SMOKE_AUTH_SERVICE_URL:-http://localhost:8085}/actuator/health"
  wait_for_http "Ingestion Management Service" "${OEFMS_SMOKE_INGESTION_SERVICE_URL:-http://localhost:8089}/actuator/health"
  wait_for_http "Fraud Detection Service" "${fraud_service_url}/actuator/health"
  wait_for_http "Explainable AI Service" "${OEFMS_SMOKE_EXPLAINABLE_AI_SERVICE_URL:-http://localhost:8094}/actuator/health"
  wait_for_http "Case Management Service" "${OEFMS_SMOKE_CASE_SERVICE_URL:-http://localhost:8084}/actuator/health"
  probe_fraud_decision "${fraud_service_url}/api/v1/fraud/decisions"

  if [ "$gateway_checks" = true ]; then
    wait_for_http "API Gateway" "${gateway_url}/actuator/health"
    probe_fraud_decision "${gateway_url}${gateway_fraud_decision_path}"
  fi
}

parse_args "$@"
run_static_checks

if [ "$live_checks" = true ]; then
  run_live_checks
else
  log "Skipping live HTTP probes. Use --live or --start-stack to run them."
fi

log "Local smoke checks passed"
