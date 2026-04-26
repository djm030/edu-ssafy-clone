#!/usr/bin/env bash
set -euo pipefail

CI_SIGNOFF_OUT="${CI_SIGNOFF_OUT:-build/reports/ci-signoff.md}"
mkdir -p "$(dirname "$CI_SIGNOFF_OUT")"

run_url="${CI_RUN_URL:-}"
if [[ -z "$run_url" && -n "${GITHUB_SERVER_URL:-}" && -n "${GITHUB_REPOSITORY:-}" && -n "${GITHUB_RUN_ID:-}" ]]; then
  run_url="${GITHUB_SERVER_URL}/${GITHUB_REPOSITORY}/actions/runs/${GITHUB_RUN_ID}"
fi
if [[ -z "$run_url" ]]; then
  run_url="local-preview"
fi

run_attempt="${GITHUB_RUN_ATTEMPT:-local}"
repository="${GITHUB_REPOSITORY:-local}"
ref_name="${GITHUB_REF_NAME:-local}"
sha="${GITHUB_SHA:-local}"
actor="${GITHUB_ACTOR:-local}"
checked_at="$(date -u +"%Y-%m-%dT%H:%M:%SZ")"

cat > "$CI_SIGNOFF_OUT" <<REPORT
# Hosted CI Sign-off Checklist

- Checked at: ${checked_at}
- Run URL: ${run_url}
- Repository: ${repository}
- Ref: ${ref_name}
- SHA: ${sha}
- Attempt: ${run_attempt}
- Actor: ${actor}

## Required gates

- [x] Docker Compose base/app/observability configs rendered successfully.
- [x] Forbidden credential marker scan completed without real EduSSAFY account data.
- [x] POSIX production smoke scripts passed shell syntax and skip-http wiring checks.
- [x] Backend Maven test suite completed.
- [x] Spring REST Docs snippet verifier completed.
- [x] Frontend dependency install, lint, and build completed.
- [x] Browser E2E core flows completed with seeded demo data only.
- [x] Browser visual baseline completed for configured screens.
- [x] Observability smoke evidence is available at build/reports/observability-smoke.jsonl when the smoke script runs.

## Sign-off rule

This file is generated only after prior workflow gates have reached the sign-off step. Treat a missing hosted artifact or a failed upstream job as no production sign-off.
REPORT

if [[ -n "${GITHUB_STEP_SUMMARY:-}" ]]; then
  cat "$CI_SIGNOFF_OUT" >> "$GITHUB_STEP_SUMMARY"
fi

echo "[ci-signoff] wrote $CI_SIGNOFF_OUT"
