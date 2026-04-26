#!/usr/bin/env bash
set -euo pipefail

BACKEND_URL="${BACKEND_URL:-http://localhost:8080}"
PROMETHEUS_URL="${PROMETHEUS_URL:-http://localhost:9090}"
GRAFANA_URL="${GRAFANA_URL:-http://localhost:3000}"
SKIP_HTTP="${SKIP_HTTP:-false}"

if [[ "$SKIP_HTTP" == "true" || "$SKIP_HTTP" == "1" || "$SKIP_HTTP" == "yes" ]]; then
  echo "[smoke-observability] SKIP_HTTP=true; validated script wiring only."
  exit 0
fi

request() {
  local label="$1"
  local url="$2"
  local contains="${3:-}"

  echo "[smoke-observability] GET $label -> $url"
  local body
  body="$(curl -fsS --max-time 10 "$url")"
  if [[ -n "$contains" && "$body" != *"$contains"* ]]; then
    echo "[smoke-observability] expected response for $label to contain: $contains" >&2
    exit 1
  fi
}

request "backend health" "$BACKEND_URL/actuator/health" "UP"
request "backend metrics index" "$BACKEND_URL/actuator/metrics" "names"
request "backend prometheus scrape" "$BACKEND_URL/actuator/prometheus" "jvm_memory_used_bytes"
request "prometheus health" "$PROMETHEUS_URL/-/healthy" "Prometheus"
request "prometheus active targets" "$PROMETHEUS_URL/api/v1/targets?state=active" "ssafy-backend"
request "grafana health" "$GRAFANA_URL/api/health" "database"

echo "[smoke-observability] observability endpoints are reachable."
