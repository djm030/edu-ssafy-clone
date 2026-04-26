#!/usr/bin/env bash
set -euo pipefail

RUN_TESTS="${RUN_TESTS:-true}"
SNIPPETS_DIR="${SNIPPETS_DIR:-backend/target/generated-snippets}"

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$repo_root"

if [[ "$RUN_TESTS" == "true" ]]; then
  docker run --rm \
    -v "$PWD:/workspace" \
    -w /workspace/backend \
    maven:3.9.9-eclipse-temurin-21 \
    mvn -q -Dtest=ApiRestDocsTest,AuthRestDocsTest,SurveyRestDocsTest test
fi

required_snippets=(
  health-check
  readiness-check
  auth-login
  auth-me
  auth-session
  auth-current-role-access
  auth-access-policy
  survey-create
  survey-update
  survey-delete
)

for snippet in "${required_snippets[@]}"; do
  dir="$SNIPPETS_DIR/$snippet"
  if [[ ! -d "$dir" ]]; then
    echo "[restdocs] missing snippet directory: $dir" >&2
    exit 1
  fi
  for file in http-request.adoc http-response.adoc response-body.adoc; do
    if [[ ! -s "$dir/$file" ]]; then
      echo "[restdocs] missing snippet file: $dir/$file" >&2
      exit 1
    fi
  done
  echo "[restdocs] PASS $snippet"
done

for snippet in health-check readiness-check auth-login auth-access-policy survey-create survey-update survey-delete; do
  file="$SNIPPETS_DIR/$snippet/response-fields.adoc"
  if [[ ! -s "$file" ]]; then
    echo "[restdocs] missing response fields: $file" >&2
    exit 1
  fi
done

if [[ ! -s "$SNIPPETS_DIR/auth-login/request-fields.adoc" ]]; then
  echo "[restdocs] missing auth login request fields" >&2
  exit 1
fi

echo "[restdocs] verified ${#required_snippets[@]} Spring REST Docs snippets"
