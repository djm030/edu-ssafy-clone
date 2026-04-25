#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
BACKEND_DIR="${ROOT_DIR}/backend"

print_usage() {
  cat <<'USAGE'
Usage:
  backend-test.sh [--skip-cache] [--it | --integration]

Commands:
  - default: run backend unit tests with Dockerized Maven (Java 21)
  - --skip-cache: skip Maven local repo bind mount (cleaner in CI)
  - --it / --integration: also run verify-openapi marker check when PowerShell is available
USAGE
}

USE_LOCAL_CACHE=0
RUN_INTEGRATION=0

while [[ $# -gt 0 ]]; do
  case "$1" in
    --skip-cache)
      USE_LOCAL_CACHE=1
      shift
      ;;
    --it|--integration)
      RUN_INTEGRATION=1
      shift
      ;;
    -h|--help)
      print_usage
      exit 0
      ;;
    *)
      echo "Unknown argument: $1" >&2
      print_usage
      exit 1
      ;;
  esac
done

if ! command -v docker >/dev/null 2>&1; then
  echo "ERROR: docker is required for the standard backend verification path." >&2
  echo "Install Docker Desktop or run backend tests on a CI runner with docker support." >&2
  exit 1
fi

DOCKER_ARGS=()
if [[ "$USE_LOCAL_CACHE" -eq 0 ]]; then
  DOCKER_ARGS+=( -v "${HOME}/.m2/repository:/root/.m2/repository" )
fi

echo "[backend-test] Running Java 21 Maven tests in containerized mode: maven:3.9.9-eclipse-temurin-21"
if [[ "${#DOCKER_ARGS[@]}" -gt 0 ]]; then
  docker run --rm "${DOCKER_ARGS[@]}" \
    -v "${BACKEND_DIR}:/workspace" \
    -w /workspace \
    maven:3.9.9-eclipse-temurin-21 mvn -B test
else
  docker run --rm \
    -v "${BACKEND_DIR}:/workspace" \
    -w /workspace \
    maven:3.9.9-eclipse-temurin-21 mvn -B test
fi

if [[ "$RUN_INTEGRATION" -eq 1 ]]; then
  if [[ -f "${ROOT_DIR}/scripts/dev/verify-openapi.ps1" ]]; then
    if command -v pwsh >/dev/null 2>&1 || command -v powershell >/dev/null 2>&1; then
      echo "[backend-test] Running verify-openapi.ps1"
      if command -v pwsh >/dev/null 2>&1; then
        pwsh -NoProfile -ExecutionPolicy Bypass -File "${ROOT_DIR}/scripts/dev/verify-openapi.ps1"
      else
        powershell -NoProfile -ExecutionPolicy Bypass -File "${ROOT_DIR}/scripts/dev/verify-openapi.ps1"
      fi
    else
      echo "[backend-test] Skip verify-openapi.ps1 (PowerShell unavailable)."
    fi
  fi
fi
