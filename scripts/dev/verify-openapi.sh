#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$repo_root"

SPEC_PATH="${SPEC_PATH:-docs/openapi.json}"
SMOKE_PATH="${SMOKE_PATH:-scripts/dev/smoke.ps1}"
CATALOG_PATH="${CATALOG_PATH:-backend/src/test/resources/api-docs-endpoints.tsv}"

require_file() {
  local path="$1"
  if [[ ! -s "$path" ]]; then
    echo "[openapi] required file is missing or empty: $path" >&2
    exit 1
  fi
}

require_contains() {
  local path="$1"
  local needle="$2"
  if ! grep -Fq "$needle" "$path"; then
    echo "[openapi] expected marker is missing in ${path}: ${needle}" >&2
    exit 1
  fi
}

require_file "$SPEC_PATH"
require_file "$SMOKE_PATH"
require_file "$CATALOG_PATH"

required_paths=(
  '"/api/auth/login"'
  '"/api/me"'
  '"/api/profile"'
  '"/api/dashboard/summary"'
  '"/api/attendance/check"'
  '"/api/elearning/in-progress"'
  '"/api/me/bookmarks"'
  '"/api/documents/requests"'
  '"/api/pledges"'
  '"/api/ebooks"'
  '"/api/boards/{boardCode}/posts"'
  '"/api/boards/{boardCode}/posts/{postId}"'
  '"/api/surveys/{id}/responses"'
  '"/api/support/tickets/{ticketId}/answers"'
  '"/api/quests/{id}/submissions"'
  '"/api/external-services"'
  '"/api/external-services/{code}/access-log"'
  '"BoardPostDetailResponse"'
  '"BoardPostCreateResponse"'
)

for marker in "${required_paths[@]}"; do
  require_contains "$SPEC_PATH" "$marker"
done

required_smoke_markers=(
  'Test-AuthJsonShape'
  'Test-ProfileJsonShape'
  'Test-BoardJsonShape'
  'post.engagement.commentCount'
  'item.createdAt'
)

for marker in "${required_smoke_markers[@]}"; do
  require_contains "$SMOKE_PATH" "$marker"
done

missing=0
while IFS=$'\t' read -r domain method path handler auth; do
  [[ -z "${domain:-}" || "${domain:0:1}" == "#" ]] && continue
  [[ "${path:-}" == /api/* ]] || continue
  [[ "$path" == /api/docs* ]] && continue
  if ! grep -Fq "\"$path\"" "$SPEC_PATH"; then
    echo "[openapi] cataloged endpoint missing from ${SPEC_PATH}: ${method:-?} ${path} (${handler:-unknown})" >&2
    missing=1
  fi
done < "$CATALOG_PATH"

if [[ "$missing" -ne 0 ]]; then
  exit 1
fi

if grep -Fq 'docs/openapi.yaml' .github/workflows/ci.yml 2>/dev/null; then
  echo "[openapi] CI must verify generated docs/openapi.json, not stale docs/openapi.yaml" >&2
  exit 1
fi

echo "[openapi] verified generated Swagger/OpenAPI snapshot markers and endpoint catalog."
