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
ingestion_service_url="${OEFMS_SMOKE_INGESTION_SERVICE_URL:-http://localhost:8089}"
fraud_service_url="${OEFMS_SMOKE_FRAUD_SERVICE_URL:-http://localhost:8081}"
rule_service_url="${OEFMS_SMOKE_RULE_SERVICE_URL:-http://localhost:8082}"
audit_service_url="${OEFMS_SMOKE_AUDIT_SERVICE_URL:-http://localhost:8090}"
gateway_url="${OEFMS_SMOKE_GATEWAY_URL:-http://localhost:8080}"
smoke_transaction_id="${OEFMS_SMOKE_TRANSACTION_ID:-118f3bd4-b208-44ff-b1ff-447c78d80d02}"
gateway_ingestion_path="${OEFMS_SMOKE_GATEWAY_INGESTION_PATH:-/api/v1/ingestion/transactions}"
gateway_ingestion_receipt_path="${OEFMS_SMOKE_GATEWAY_INGESTION_RECEIPT_PATH:-/api/v1/ingestion/transactions/${smoke_transaction_id}}"
gateway_fraud_decision_path="${OEFMS_SMOKE_GATEWAY_FRAUD_DECISION_PATH:-/api/v1/fraud/decisions}"
gateway_ml_runtime_path="${OEFMS_SMOKE_GATEWAY_ML_RUNTIME_PATH:-/api/v1/fraud/ml/runtime}"
gateway_rule_path="${OEFMS_SMOKE_GATEWAY_RULE_PATH:-/api/v1/rules}"

core_services=(
  postgres
  rabbitmq
  redis
  auth-service
  ingestion-management-service
  fraud-detection-service
  rule-management-service
  explainable-ai-service
  case-management-service
  audit-logging-service
)

usage() {
  cat <<'USAGE'
Usage: ./scripts/smoke-local.sh [options]

Options:
  --skip-build    Skip mvn clean verify.
  --live          Probe an already-running local stack.
  --start-stack   Start the core local stack, then run live probes.
  --gateway       Include API Gateway health and optional route probes.
  -h, --help      Show this help text.

Environment:
  MAVEN_CMD                                 Maven executable. Default: mvn
  OEFMS_SMOKE_ENV_FILE                     Compose env file. Default: .env.example
  OEFMS_SMOKE_INGESTION_SERVICE_URL        Ingestion service URL. Default: http://localhost:8089
  OEFMS_SMOKE_FRAUD_SERVICE_URL            Fraud service URL. Default: http://localhost:8081
  OEFMS_SMOKE_RULE_SERVICE_URL             Rule Management service URL. Default: http://localhost:8082
  OEFMS_SMOKE_AUDIT_SERVICE_URL            Audit Logging service URL. Default: http://localhost:8090
  OEFMS_SMOKE_GATEWAY_URL                  Gateway URL. Default: http://localhost:8080
  OEFMS_SMOKE_TRANSACTION_ID               Transaction id used by live probes. Default: fixed smoke UUID
  OEFMS_SMOKE_GATEWAY_INGESTION_PATH       Gateway ingestion path. Default: /api/v1/ingestion/transactions
  OEFMS_SMOKE_GATEWAY_INGESTION_RECEIPT_PATH Gateway ingestion receipt path. Default: /api/v1/ingestion/transactions/<smoke id>
  OEFMS_SMOKE_GATEWAY_FRAUD_DECISION_PATH  Gateway fraud decision path and decision-record base. Default: /api/v1/fraud/decisions
  OEFMS_SMOKE_GATEWAY_ML_RUNTIME_PATH      Gateway ML runtime path. Default: /api/v1/fraud/ml/runtime
  OEFMS_SMOKE_GATEWAY_RULE_PATH            Gateway rule catalog path. Default: /api/v1/rules
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

transaction_payload() {
  cat <<JSON
{
  "transactionId": "${smoke_transaction_id}",
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

probe_transaction_ingestion() {
  local url="$1"
  local response

  log "Posting smoke transaction to ${url}"
  response="$(curl -fsS \
    -H 'Content-Type: application/json' \
    -H 'X-Correlation-Id: smoke-correlation-1' \
    -d "$(transaction_payload)" \
    "$url")"

  printf '%s' "$response" | grep -q '"status":"RECEIVED"' || fail "Ingestion response did not acknowledge the smoke transaction"
  printf '%s' "$response" | grep -q '"eventType":"transaction.received"' || fail "Ingestion response did not prepare a transaction.received event"
  log "Transaction ingestion probe passed"
}

probe_transaction_receipt() {
  local url="$1"
  local response

  log "Reading persisted transaction receipt from ${url}"
  response="$(curl -fsS "$url")"

  printf '%s' "$response" | grep -q '"status":"PUBLISHED"' || fail "Persisted receipt was not marked PUBLISHED"
  printf '%s' "$response" | grep -q '"eventType":"transaction.received"' || fail "Persisted receipt did not retain the transaction.received event type"
  printf '%s' "$response" | grep -q '"tenantId":"smoke-tenant"' || fail "Persisted receipt did not retain the smoke tenant"
  log "Transaction receipt probe passed"
}

probe_fraud_decision() {
  local url="$1"
  local response
  local decision_id

  log "Posting smoke transaction to ${url}"
  response="$(curl -fsS \
    -H 'Content-Type: application/json' \
    -d "$(transaction_payload)" \
    "$url")"

  printf '%s' "$response" | grep -q '"mode":"RULES"' || fail "Fraud decision response did not use RULES mode"
  printf '%s' "$response" | grep -q '"outcome":"APPROVE"' || fail "Fraud decision response did not approve the low-risk smoke transaction"
  printf '%s' "$response" | grep -q '"ruleHitCount":"0"' || fail "Fraud decision response did not report zero rule hits"
  decision_id="$(printf '%s' "$response" | sed -n 's/.*"decisionId":"\([^"]*\)".*/\1/p')"
  [ -n "$decision_id" ] || fail "Fraud decision response did not include a decision id"
  log "Fraud decision probe passed"
  probe_fraud_decision_record "${url}/${decision_id}"
}

probe_fraud_decision_record() {
  local url="$1"
  local response

  log "Reading persisted fraud decision record from ${url}"
  response="$(curl -fsS "$url")"

  printf '%s' "$response" | grep -q '"outcome":"APPROVE"' || fail "Persisted fraud decision did not retain the outcome"
  printf '%s' "$response" | grep -q '"riskBand":"LOW"' || fail "Persisted fraud decision did not retain the risk band"
  printf '%s' "$response" | grep -q "\"transactionId\":\"${smoke_transaction_id}\"" || fail "Persisted fraud decision did not retain the transaction id"
  log "Fraud decision record probe passed"
}

probe_ml_runtime() {
  local url="$1"
  local response

  log "Reading ML runtime status from ${url}"
  response="$(curl -fsS "$url")"

  printf '%s' "$response" | grep -q '"modelName":"xgboost-baseline"' || fail "ML runtime did not report the default model name"
  printf '%s' "$response" | grep -q '"state":' || fail "ML runtime did not report an availability state"
  printf '%s' "$response" | grep -q '"supportedModes":' || fail "ML runtime did not report supported decision modes"
  log "ML runtime probe passed"
}

probe_rule_catalog() {
  local url="$1"
  local response

  log "Reading baseline rule catalog from ${url}"
  response="$(curl -fsS "${url}?tenantId=global&status=active")"

  printf '%s' "$response" | grep -q '"ruleCode":"HIGH_VALUE_AMOUNT"' || fail "Rule catalog did not include HIGH_VALUE_AMOUNT"
  printf '%s' "$response" | grep -q '"ruleCode":"CRITICAL_VALUE_AMOUNT"' || fail "Rule catalog did not include CRITICAL_VALUE_AMOUNT"
  log "Rule catalog probe passed"
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
    local services_to_start=("${core_services[@]}")
    if [ "$gateway_checks" = true ]; then
      services_to_start+=(api-gateway)
    fi

    docker info >/dev/null 2>&1 || fail "Docker daemon is not running; start Docker before using --start-stack"
    log "Starting local smoke-test services"
    compose up -d --build "${services_to_start[@]}"
  fi

  wait_for_http "Auth Service" "${OEFMS_SMOKE_AUTH_SERVICE_URL:-http://localhost:8085}/actuator/health"
  wait_for_http "Ingestion Management Service" "${ingestion_service_url}/actuator/health"
  wait_for_http "Fraud Detection Service" "${fraud_service_url}/actuator/health"
  wait_for_http "Rule Management Service" "${rule_service_url}/actuator/health"
  wait_for_http "Explainable AI Service" "${OEFMS_SMOKE_EXPLAINABLE_AI_SERVICE_URL:-http://localhost:8094}/actuator/health"
  wait_for_http "Case Management Service" "${OEFMS_SMOKE_CASE_SERVICE_URL:-http://localhost:8084}/actuator/health"
  wait_for_http "Audit Logging Service" "${audit_service_url}/actuator/health"
  probe_transaction_ingestion "${ingestion_service_url}/api/v1/ingestion/transactions"
  probe_transaction_receipt "${ingestion_service_url}/api/v1/ingestion/transactions/${smoke_transaction_id}"
  probe_fraud_decision "${fraud_service_url}/api/v1/fraud/decisions"
  probe_ml_runtime "${fraud_service_url}/api/v1/fraud/ml/runtime"
  probe_rule_catalog "${rule_service_url}/api/v1/rules"

  if [ "$gateway_checks" = true ]; then
    wait_for_http "API Gateway" "${gateway_url}/actuator/health"
    probe_transaction_ingestion "${gateway_url}${gateway_ingestion_path}"
    probe_transaction_receipt "${gateway_url}${gateway_ingestion_receipt_path}"
    probe_fraud_decision "${gateway_url}${gateway_fraud_decision_path}"
    probe_ml_runtime "${gateway_url}${gateway_ml_runtime_path}"
    probe_rule_catalog "${gateway_url}${gateway_rule_path}"
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
