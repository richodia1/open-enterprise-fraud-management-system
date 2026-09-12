#!/usr/bin/env bash
set -euo pipefail

script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
project_dir="$(cd "${script_dir}/.." && pwd)"

gateway_url="${OEFMS_DEMO_GATEWAY_URL:-http://localhost:8080}"
tenant_id="${OEFMS_DEMO_TENANT_ID:-demo-bank}"
transaction_id="${OEFMS_DEMO_TRANSACTION_ID:-}"
correlation_id="${OEFMS_DEMO_CORRELATION_ID:-}"
timeout_seconds="${OEFMS_DEMO_TIMEOUT_SECONDS:-180}"
interval_seconds="${OEFMS_DEMO_INTERVAL_SECONDS:-3}"
startup_timeout_seconds="${OEFMS_DEMO_STARTUP_TIMEOUT_SECONDS:-360}"
python_cmd="${PYTHON_CMD:-python3}"
env_file="${OEFMS_DEMO_ENV_FILE:-.env.example}"
start_stack=false
stop_stack=false
show_json=false

infrastructure_services=(postgres rabbitmq redis)

domain_services=(
  "ingestion-management-service|Ingestion Management Service|http://localhost:8089/actuator/health"
  "fraud-detection-service|Fraud Detection Service|http://localhost:8081/actuator/health"
  "explainable-ai-service|Explainable AI Service|http://localhost:8094/actuator/health"
  "case-management-service|Case Management Service|http://localhost:8084/actuator/health"
  "audit-logging-service|Audit Logging Service|http://localhost:8090/actuator/health"
)

usage() {
  cat <<'USAGE'
Usage: ./scripts/run-mvp-demo-flow.sh [options]

Options:
  --start-stack   Start the local MVP services before running the demo flow.
  --stop-stack    Stop the local stack after the demo flow finishes.
  --show-json     Print the captured JSON responses after the summary.
  -h, --help      Show this help text.

Environment:
  OEFMS_DEMO_GATEWAY_URL       API Gateway URL. Default: http://localhost:8080
  OEFMS_DEMO_TENANT_ID         Tenant used by the demo transaction. Default: demo-bank
  OEFMS_DEMO_TRANSACTION_ID    Optional fixed transaction UUID. Default: generated UUID
  OEFMS_DEMO_CORRELATION_ID    Optional fixed correlation ID. Default: demo-flow-<transaction-id>
  OEFMS_DEMO_TIMEOUT_SECONDS   Wait timeout for event-driven records. Default: 180
  OEFMS_DEMO_STARTUP_TIMEOUT_SECONDS Startup timeout used with --start-stack. Default: 360
  OEFMS_DEMO_INTERVAL_SECONDS  Poll interval for event-driven records. Default: 3
  OEFMS_DEMO_OUTPUT_DIR        Output directory for captured JSON. Default: target/mvp-demo-flow/<transaction-id>
  OEFMS_DEMO_ENV_FILE          Compose env file used with --stop-stack. Default: .env.example
  PYTHON_CMD                   Python executable used for JSON parsing. Default: python3
USAGE
}

log() {
  printf '[demo] %s\n' "$*"
}

fail() {
  printf '[demo] ERROR: %s\n' "$*" >&2
  exit 1
}

require_command() {
  command -v "$1" >/dev/null 2>&1 || fail "Required command not found: $1"
}

parse_args() {
  while [ "$#" -gt 0 ]; do
    case "$1" in
      --start-stack)
        start_stack=true
        ;;
      --stop-stack)
        stop_stack=true
        ;;
      --show-json)
        show_json=true
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

new_uuid() {
  if command -v uuidgen >/dev/null 2>&1; then
    uuidgen | tr '[:upper:]' '[:lower:]'
    return
  fi

  "$python_cmd" -c 'import uuid; print(uuid.uuid4())'
}

pretty_json() {
  "$python_cmd" -m json.tool "$1"
}

json_field() {
  local file="$1"
  local path="$2"

  "$python_cmd" - "$file" "$path" <<'PY'
import json
import sys

with open(sys.argv[1], encoding="utf-8") as handle:
    value = json.load(handle)

for part in sys.argv[2].split("."):
    if isinstance(value, list):
        if not part.isdigit() or int(part) >= len(value):
            value = ""
            break
        value = value[int(part)]
    elif isinstance(value, dict):
        value = value.get(part, "")
    else:
        value = ""
        break

if isinstance(value, (dict, list)):
    print(json.dumps(value, separators=(",", ":")))
elif value is None:
    print("")
else:
    print(value)
PY
}

json_find_transaction_field() {
  local file="$1"
  local tx_id="$2"
  local path="$3"

  "$python_cmd" - "$file" "$tx_id" "$path" <<'PY'
import json
import sys

with open(sys.argv[1], encoding="utf-8") as handle:
    data = json.load(handle)

items = data if isinstance(data, list) else [data]
match = next((item for item in items if str(item.get("transactionId", "")) == sys.argv[2]), None)
if match is None:
    sys.exit(1)

value = match
for part in sys.argv[3].split("."):
    if isinstance(value, dict):
        value = value.get(part, "")
    else:
        value = ""
        break

if isinstance(value, (dict, list)):
    print(json.dumps(value, separators=(",", ":")))
elif value is None:
    print("")
else:
    print(value)
PY
}

json_count() {
  local file="$1"

  "$python_cmd" - "$file" <<'PY'
import json
import sys

with open(sys.argv[1], encoding="utf-8") as handle:
    data = json.load(handle)

print(len(data) if isinstance(data, list) else 1)
PY
}

json_has_transaction_action() {
  local file="$1"
  local tx_id="$2"
  local action="$3"

  "$python_cmd" - "$file" "$tx_id" "$action" <<'PY'
import json
import sys

with open(sys.argv[1], encoding="utf-8") as handle:
    data = json.load(handle)

items = data if isinstance(data, list) else [data]
for item in items:
    attributes = item.get("attributes") or {}
    summary = item.get("summary") or ""
    if item.get("action") == sys.argv[3] and (
        attributes.get("transactionId") == sys.argv[2] or sys.argv[2] in summary
    ):
        sys.exit(0)

sys.exit(1)
PY
}

wait_for_http() {
  local name="$1"
  local url="$2"
  local deadline
  deadline=$((SECONDS + timeout_seconds))

  log "Waiting for ${name}: ${url}"
  until curl -fsS "$url" >/dev/null 2>&1; do
    if [ "$SECONDS" -ge "$deadline" ]; then
      fail "${name} did not become available within ${timeout_seconds}s"
    fi
    sleep "$interval_seconds"
  done
}

wait_for_container_healthy() {
  local name="$1"
  local deadline
  local status
  deadline=$((SECONDS + startup_timeout_seconds))

  log "Waiting for container health: ${name}"
  until status="$(docker inspect --format='{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}' "$name" 2>/dev/null)" \
    && [ "$status" = "healthy" ]; do
    if [ "$SECONDS" -ge "$deadline" ]; then
      fail "Container ${name} did not become healthy within ${startup_timeout_seconds}s"
    fi
    sleep "$interval_seconds"
  done
}

compose() {
  docker compose --env-file "$env_file" "$@"
}

wait_for_transaction_record() {
  local name="$1"
  local url="$2"
  local output_file="$3"
  local deadline
  deadline=$((SECONDS + timeout_seconds))

  log "Waiting for ${name}: ${url}"
  until curl -fsS "$url" -o "$output_file" 2>/dev/null \
    && json_find_transaction_field "$output_file" "$transaction_id" transactionId >/dev/null 2>&1; do
    if [ "$SECONDS" -ge "$deadline" ]; then
      fail "${name} did not contain transaction ${transaction_id} within ${timeout_seconds}s"
    fi
    sleep "$interval_seconds"
  done
}

wait_for_audit_action() {
  local action="$1"
  local output_file="$2"
  local audit_url="${gateway_url}/api/v1/audit-events?tenantId=${tenant_id}"
  local deadline
  deadline=$((SECONDS + timeout_seconds))

  log "Waiting for audit action ${action}: ${audit_url}"
  until curl -fsS "$audit_url" -o "$output_file" 2>/dev/null \
    && json_has_transaction_action "$output_file" "$transaction_id" "$action" >/dev/null 2>&1; do
    if [ "$SECONDS" -ge "$deadline" ]; then
      fail "Audit action ${action} did not arrive for transaction ${transaction_id} within ${timeout_seconds}s"
    fi
    sleep "$interval_seconds"
  done
}

start_local_stack() {
  require_command docker
  local service_entry
  local service_name
  local service_label
  local service_health_url

  docker info >/dev/null 2>&1 || fail "Docker daemon is not running; start Docker before using --start-stack"

  log "Starting MVP infrastructure"
  compose up -d --build "${infrastructure_services[@]}"
  wait_for_container_healthy openefms-postgres
  wait_for_container_healthy openefms-rabbitmq
  wait_for_container_healthy openefms-redis

  log "Starting MVP event-flow services one at a time"
  for service_entry in "${domain_services[@]}"; do
    IFS='|' read -r service_name service_label service_health_url <<<"$service_entry"
    compose up -d --build "$service_name"
    wait_for_http "$service_label" "$service_health_url"
  done

  log "Starting API Gateway for MVP routes"
  compose up -d --no-deps --build api-gateway
  wait_for_http "API Gateway" "${gateway_url}/actuator/health"
}

stop_local_stack() {
  log "Stopping local stack"
  compose down -v --remove-orphans
}

write_payload() {
  local payload_file="$1"
  local initiated_at
  initiated_at="$(date -u +"%Y-%m-%dT%H:%M:%SZ")"

  cat > "$payload_file" <<JSON
{
  "transactionId": "${transaction_id}",
  "externalReference": "DEMO-REVIEW-${transaction_id}",
  "tenantId": "${tenant_id}",
  "transactionType": "TRANSFER",
  "channel": "MOBILE",
  "direction": "OUTBOUND",
  "amount": {
    "value": 25000.00,
    "currency": "GBP"
  },
  "originator": {
    "partyReference": "originator-demo",
    "customerReference": "originator-demo-customer",
    "accountReference": "originator-demo-account",
    "accountNumberHash": "originator-demo-account-hash",
    "institutionCode": "DEMO",
    "countryCode": "GB",
    "attributes": {}
  },
  "beneficiary": {
    "partyReference": "beneficiary-demo",
    "customerReference": "beneficiary-demo-customer",
    "accountReference": "beneficiary-demo-account",
    "accountNumberHash": "beneficiary-demo-account-hash",
    "institutionCode": "DEMO",
    "countryCode": "NG",
    "attributes": {}
  },
  "initiatedAt": "${initiated_at}",
  "attributes": {
    "velocityCount": "6"
  }
}
JSON
}

print_summary() {
  local decision_id
  local outcome
  local risk_band
  local risk_score
  local reason
  local case_id
  local case_priority
  local case_summary
  local alert_id
  local alert_priority
  local alert_summary
  local audit_count

  decision_id="$(json_find_transaction_field "$decision_file" "$transaction_id" decisionId)"
  outcome="$(json_find_transaction_field "$decision_file" "$transaction_id" outcome)"
  risk_band="$(json_find_transaction_field "$decision_file" "$transaction_id" riskBand)"
  risk_score="$(json_find_transaction_field "$decision_file" "$transaction_id" riskScoreValue)"
  reason="$(json_find_transaction_field "$decision_file" "$transaction_id" riskReason)"
  case_id="$(json_find_transaction_field "$case_file" "$transaction_id" caseId)"
  case_priority="$(json_find_transaction_field "$case_file" "$transaction_id" priority)"
  case_summary="$(json_find_transaction_field "$case_file" "$transaction_id" summary)"
  alert_id="$(json_find_transaction_field "$alert_file" "$transaction_id" alertId)"
  alert_priority="$(json_find_transaction_field "$alert_file" "$transaction_id" priority)"
  alert_summary="$(json_find_transaction_field "$alert_file" "$transaction_id" summary)"
  audit_count="$(json_count "$audit_file")"

  cat <<SUMMARY

MVP demo flow complete
Transaction ID: ${transaction_id}
Tenant ID: ${tenant_id}
Correlation ID: ${correlation_id}

Fraud decision
  Decision ID: ${decision_id}
  Outcome: ${outcome}
  Risk band: ${risk_band}
  Risk score: ${risk_score}
  Reason: ${reason}

Investigation handoff
  Case ID: ${case_id}
  Case priority: ${case_priority}
  Case summary: ${case_summary}
  Alert ID: ${alert_id}
  Alert priority: ${alert_priority}
  Alert summary: ${alert_summary}

Audit
  Captured tenant audit records: ${audit_count}
  Required actions observed: ALERT_CREATED, CASE_CREATED

Captured JSON files: ${output_dir}

Useful follow-up URLs
  ${gateway_url}/api/v1/fraud/decisions?transactionId=${transaction_id}
  ${gateway_url}/api/v1/cases?tenantId=${tenant_id}
  ${gateway_url}/api/v1/alerts?tenantId=${tenant_id}
  ${gateway_url}/api/v1/audit-events?tenantId=${tenant_id}
SUMMARY
}

print_json_outputs() {
  printf '\n%s\n' '--- ingestion response ---'
  pretty_json "$ingestion_file"
  printf '\n%s\n' '--- fraud decisions ---'
  pretty_json "$decision_file"
  printf '\n%s\n' '--- cases ---'
  pretty_json "$case_file"
  printf '\n%s\n' '--- alerts ---'
  pretty_json "$alert_file"
  printf '\n%s\n' '--- audit events ---'
  pretty_json "$audit_file"
}

parse_args "$@"
require_command curl
require_command "$python_cmd"
cd "$project_dir"

if [ -z "$transaction_id" ]; then
  transaction_id="$(new_uuid)"
fi
if [ -z "$correlation_id" ]; then
  correlation_id="demo-flow-${transaction_id}"
fi

output_dir="${OEFMS_DEMO_OUTPUT_DIR:-target/mvp-demo-flow/${transaction_id}}"
mkdir -p "$output_dir"

payload_file="${output_dir}/transaction-request.json"
ingestion_file="${output_dir}/ingestion-response.json"
receipt_file="${output_dir}/transaction-receipt.json"
decision_file="${output_dir}/fraud-decisions.json"
case_file="${output_dir}/cases.json"
alert_file="${output_dir}/alerts.json"
audit_file="${output_dir}/audit-events.json"

if [ "$start_stack" = true ]; then
  start_local_stack
else
  wait_for_http "API Gateway" "${gateway_url}/actuator/health"
fi

write_payload "$payload_file"

log "Submitting reviewable transaction ${transaction_id}"
curl -fsS \
  -H 'Content-Type: application/json' \
  -H "X-Correlation-Id: ${correlation_id}" \
  -d @"$payload_file" \
  "${gateway_url}/api/v1/ingestion/transactions" \
  -o "$ingestion_file"

[ "$(json_field "$ingestion_file" status)" = "RECEIVED" ] \
  || fail "Ingestion did not acknowledge transaction ${transaction_id}"

curl -fsS "${gateway_url}/api/v1/ingestion/transactions/${transaction_id}" -o "$receipt_file"
[ "$(json_field "$receipt_file" status)" = "PUBLISHED" ] \
  || fail "Transaction receipt was not marked PUBLISHED"

wait_for_transaction_record \
  "event-driven fraud decision" \
  "${gateway_url}/api/v1/fraud/decisions?transactionId=${transaction_id}" \
  "$decision_file"

wait_for_transaction_record \
  "case record" \
  "${gateway_url}/api/v1/cases?tenantId=${tenant_id}" \
  "$case_file"

wait_for_transaction_record \
  "alert record" \
  "${gateway_url}/api/v1/alerts?tenantId=${tenant_id}" \
  "$alert_file"

wait_for_audit_action ALERT_CREATED "$audit_file"
wait_for_audit_action CASE_CREATED "$audit_file"

print_summary

if [ "$show_json" = true ]; then
  print_json_outputs
fi

if [ "$stop_stack" = true ]; then
  stop_local_stack
fi
