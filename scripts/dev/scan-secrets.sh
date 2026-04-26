#!/usr/bin/env bash
set -euo pipefail

# Lightweight repository guard for credentials that must never be committed.
# It intentionally scans tracked source/config files and avoids broad words such as
# "password" because placeholder names and tests need to describe secret handling.
patterns=(
  'djm030'
  'djm062954'
  'JSESSIONID='
  'SESSION='
  'Cookie:.*(JSESSIONID|SESSION)'
  'AWS_SECRET_ACCESS_KEY='
  'GOOGLE_CLIENT_SECRET='
  'KAKAO_CLIENT_SECRET='
  'NAVER_CLIENT_SECRET='
  '-----BEGIN (RSA |OPENSSH |EC |DSA )?PRIVATE KEY-----'
)

regex="$(IFS='|'; echo "${patterns[*]}")"

if git grep -n -I -E "$regex" -- \
  ':!frontend/node_modules' \
  ':!backend/target' \
  ':!frontend/dist' \
  ':!frontend/playwright-report' \
  ':!frontend/test-results' \
  ':!scripts/dev/scan-secrets.sh' \
  ':!backend/src/test/java/com/edussafy/backend/docs/SecretScanScriptTest.java'
then
  echo "[scan-secrets] potential committed credential detected" >&2
  exit 1
fi

echo "[scan-secrets] no forbidden credential markers found."
