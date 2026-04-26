#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost}"
BACKEND_URL="${BACKEND_URL:-http://localhost:8080}"
SKIP_HTTP="${SKIP_HTTP:-false}"

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$repo_root"

cookie_file="$(mktemp)"
trap 'rm -f "$cookie_file"' EXIT

require_file_contains() {
  local path="$1"
  local needle="$2"
  grep -Fq "$needle" "$path" || {
    echo "Expected static path is missing in ${path}: ${needle}" >&2
    exit 1
  }
}

require_any_file_contains() {
  local needle="$1"
  shift
  for path in "$@"; do
    if grep -Fq "$needle" "$path"; then
      return 0
    fi
  done
  echo "Expected static API path is missing: ${needle}" >&2
  exit 1
}

request() {
  local method="$1"
  local url="$2"
  local body="${3:-}"
  if [[ -n "$body" ]]; then
    curl -fsS -X "$method" -H 'Content-Type: application/json' -b "$cookie_file" -c "$cookie_file" -d "$body" "$url" >/dev/null
  else
    curl -fsS -X "$method" -b "$cookie_file" -c "$cookie_file" "$url" >/dev/null
  fi
  echo "$method $url -> OK"
}

for route in \
  /mycampus/attendance \
  /mycampus/elearning \
  /mycampus/bookmarks \
  /mycampus/documents \
  /mycampus/pledges \
  /mycampus/education-status \
  /mycampus/ebooks \
  /learning/curriculum \
  /learning/required-studies \
  /learning/live \
  /learning/replays/my \
  /learning/replays/all \
  /profile/check \
  /community/free \
  /community/anonymous \
  /community/classmates \
  /help/rules \
  /mentoring/stories \
  /mentoring/questions \
  /help/qna \
  /quest \
  /survey \
  /ops/readiness; do
  require_file_contains frontend/src/App.tsx "$route"
done

for api_path in \
  /api/auth/login \
  /api/readiness \
  /api/attendance/records \
  /api/notifications \
  /api/learning/materials \
  /api/elearning/in-progress \
  /api/me/bookmarks \
  /api/documents/requests \
  /api/pledges \
  /api/mycampus/education-status \
  /api/ebooks \
  /api/required-studies \
  /api/live-sessions/today \
  /api/curriculum/weeks \
  /api/replays/my \
  /api/quests/ \
  /api/surveys/ \
  /api/support/tickets \
  /api/help/academic-rules \
  /api/mentoring/stories \
  /api/mentoring/questions; do
  require_any_file_contains "$api_path" frontend/src/api/app.ts frontend/src/api/readiness.ts
done

if [[ "$SKIP_HTTP" == "true" ]]; then
  echo "HTTP smoke checks skipped."
  exit 0
fi

request GET "$BASE_URL/nginx-health"
request GET "$BASE_URL/api/readiness"
request GET "$BACKEND_URL/actuator/health"
request GET "$BACKEND_URL/api/health"
request GET "$BACKEND_URL/api/readiness"
request POST "$BACKEND_URL/api/auth/login" '{"email":"student@ssafy.com","password":"password"}'
request GET "$BACKEND_URL/api/me"
request GET "$BACKEND_URL/api/attendance/records"
request GET "$BACKEND_URL/api/notifications?page=1&size=5"
request GET "$BACKEND_URL/api/learning/materials?page=1&size=5"
request GET "$BACKEND_URL/api/elearning/in-progress?page=1&size=5"
request GET "$BACKEND_URL/api/me/bookmarks?page=1&size=5"
request GET "$BACKEND_URL/api/documents/requests?page=1&size=5"
request GET "$BACKEND_URL/api/pledges?page=1&size=5"
request GET "$BACKEND_URL/api/mycampus/education-status"
request GET "$BACKEND_URL/api/ebooks?page=1&size=5"
request GET "$BACKEND_URL/api/required-studies?page=1&size=5"
request GET "$BACKEND_URL/api/live-sessions/today"
request GET "$BACKEND_URL/api/curriculum/weeks"
request GET "$BACKEND_URL/api/replays/my"
request GET "$BACKEND_URL/api/replays/all"
request GET "$BACKEND_URL/api/quests?page=1&size=5"
request GET "$BACKEND_URL/api/surveys?page=1&size=5"
request GET "$BACKEND_URL/api/support/tickets?page=1&size=5"
