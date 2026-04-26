#!/usr/bin/env bash
set -euo pipefail

BACKEND_URL="${BACKEND_URL:-http://localhost:8080}"
PROMETHEUS_URL="${PROMETHEUS_URL:-http://localhost:9090}"
GRAFANA_URL="${GRAFANA_URL:-http://localhost:3000}"
SKIP_HTTP="${SKIP_HTTP:-false}"
EVIDENCE_FILE="${EVIDENCE_FILE:-build/reports/observability-smoke.jsonl}"

mkdir -p "$(dirname "$EVIDENCE_FILE")"
: > "$EVIDENCE_FILE"

json_escape() {
  printf '%s' "$1" | sed 's/\\/\\\\/g; s/"/\\"/g'
}

record_evidence() {
  local label="$1"
  local target="$2"
  local status="$3"
  local message="$4"
  local checked_at
  checked_at="$(date -u +"%Y-%m-%dT%H:%M:%SZ")"
  printf '{"checkedAt":"%s","label":"%s","target":"%s","status":"%s","message":"%s"}\n' \
    "$checked_at" \
    "$(json_escape "$label")" \
    "$(json_escape "$target")" \
    "$(json_escape "$status")" \
    "$(json_escape "$message")" >> "$EVIDENCE_FILE"
}

if [[ "$SKIP_HTTP" == "true" || "$SKIP_HTTP" == "1" || "$SKIP_HTTP" == "yes" ]]; then
  echo "[smoke-observability] SKIP_HTTP=true; validated script wiring only."
  record_evidence "script wiring" "local" "SKIPPED" "SKIP_HTTP=true; HTTP endpoint checks were intentionally skipped."
  echo "[smoke-observability] evidence: $EVIDENCE_FILE"
  exit 0
fi

request() {
  local label="$1"
  local url="$2"
  local contains="${3:-}"

  echo "[smoke-observability] GET $label -> $url"
  local body
  if ! body="$(curl -fsS --max-time 10 "$url")"; then
    record_evidence "$label" "$url" "FAIL" "HTTP request failed."
    exit 1
  fi
  if [[ -n "$contains" && "$body" != *"$contains"* ]]; then
    echo "[smoke-observability] expected response for $label to contain: $contains" >&2
    record_evidence "$label" "$url" "FAIL" "Response did not contain expected marker: $contains"
    exit 1
  fi
  record_evidence "$label" "$url" "PASS" "Response contained expected marker: ${contains:-HTTP 2xx}"
}

request "backend health" "$BACKEND_URL/actuator/health" "UP"
request "backend metrics index" "$BACKEND_URL/actuator/metrics" "names"
request "backend prometheus scrape" "$BACKEND_URL/actuator/prometheus" "jvm_memory_used_bytes"
request "prometheus health" "$PROMETHEUS_URL/-/healthy" "Prometheus"
request "prometheus active targets" "$PROMETHEUS_URL/api/v1/targets?state=active" "ssafy-backend"
request "grafana health" "$GRAFANA_URL/api/health" "database"

echo "[smoke-observability] observability endpoints are reachable."
echo "[smoke-observability] evidence: $EVIDENCE_FILE"
