#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${FRONTEND_BASE_URL:-http://127.0.0.1:4173}"
SKIP_HTTP="${SKIP_HTTP:-false}"

routes=(
  "/"
  "/login"
  "/profile/check"
  "/profile/edit"
  "/mycampus/attendance"
  "/mycampus/attendance/appeals/new"
  "/mycampus/elearning"
  "/mycampus/elearning/1"
  "/mycampus/bookmarks"
  "/mycampus/documents"
  "/mycampus/pledges"
  "/mycampus/pledges/1"
  "/mycampus/education-status"
  "/mycampus/ebooks"
  "/mycampus/ebooks/1"
  "/mycampus/level"
  "/community/free"
  "/community/free/1"
  "/community/free/write"
  "/community/anonymous"
  "/community/anonymous/1"
  "/community/anonymous/write"
  "/community/classmates"
  "/survey"
  "/survey/1"
  "/survey/1/respond"
  "/help/qna"
  "/help/qna/new"
  "/help/qna/tickets/1"
  "/help/notice"
  "/help/faq"
  "/help/rules"
  "/mentoring/stories"
  "/mentoring/stories/1"
  "/mentoring/questions"
  "/mentoring/questions/new"
  "/mentoring/questions/1"
  "/mentoring/notices"
  "/mentoring/notices/1"
  "/mentoring/meetings"
  "/mentoring/meetings/1"
  "/mentoring/meetings/my-applications"
  "/mentoring/meeting-results"
  "/mentoring/meeting-results/993"
  "/mentoring/meeting-reviews"
  "/mentoring/meeting-reviews/write"
  "/mentoring/meeting-reviews/1301"
  "/mycampus/notifications"
  "/learning/live"
  "/learning/curriculum"
  "/learning/materials"
  "/learning/materials/1"
  "/learning/materials/1/viewer"
  "/learning/required-studies"
  "/learning/required-studies/1"
  "/learning/replays"
  "/learning/replays/my"
  "/learning/replays/all"
  "/quest"
  "/quest/1"
  "/quest/1/submit"
  "/ops/readiness"
  "/admin/campus"
)

if [[ "$SKIP_HTTP" == "true" ]]; then
  printf '[screen-smoke] manifest contains %s routes\n' "${#routes[@]}"
  printf '%s\n' "${routes[@]}"
  exit 0
fi

for route in "${routes[@]}"; do
  url="${BASE_URL%/}${route}"
  body_file="$(mktemp)"
  status="$(curl -fsS -o "$body_file" -w '%{http_code}' "$url")"
  if [[ "$status" != "200" ]]; then
    echo "[screen-smoke] FAIL $route returned HTTP $status" >&2
    cat "$body_file" >&2 || true
    rm -f "$body_file"
    exit 1
  fi
  if ! grep -q '<div id="root"></div>' "$body_file"; then
    echo "[screen-smoke] FAIL $route did not return the SPA shell" >&2
    rm -f "$body_file"
    exit 1
  fi
  rm -f "$body_file"
  echo "[screen-smoke] PASS $route"
done
