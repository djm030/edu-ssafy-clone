# Test Report

## R7.0 Contract/Fallback Guardrail Verification (2026-04-24)

### Changed Verification Surface
- Added backend controller assertions for board detail `{ post }` fields and board create `{ item }` fields.
- Added live smoke JSON shape assertions for auth login, `/api/me`, profile, board detail, and board create responses.
- Added maintained OpenAPI marker verification through `scripts/dev/verify-openapi.ps1`.

### Results From This Worker Pass
- `npm ci` -> PASS; dependency install completed with a non-fatal local Node v23.6.0 engine warning for `eslint-visitor-keys`.
- `cd frontend && npm run lint` -> PASS.
- `cd frontend && npm run build` -> PASS; Vite built 65 modules.
- `git diff --check` -> PASS.
- Python marker check equivalent for `docs/openapi.yaml` -> PASS.

### Blocked Commands
- `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/dev/smoke.ps1 -SkipHttp` -> BLOCKED: `powershell`/`pwsh` is not installed in this execution environment.
- `cd backend && mvn -B test` -> BLOCKED: `mvn` is not installed and this repo has no Maven wrapper.

### Host/CI Retest
1. `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/dev/smoke.ps1 -SkipHttp`
2. `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/dev/smoke.ps1` after app profile is running
3. `cd backend && mvn -B test`
4. `cd frontend && npm run lint && npm run build`

## R6 Round 1 QA Harness Update (2026-04-24)

### Summary
Coverage: R5 baseline smoke -> R6 expanded smoke for health/login/profile/attendance/appeal/material detail/quest/survey/board/comment/support/classmate/notification flows.  
Test Health: partially verified. Frontend/static/live baseline smoke passed; backend Maven and backend rebuild are blocked by local tool/Docker ACL limits in this sandbox.

### Harness Changes
- `scripts/dev/README.md`
  - Recorded the previously successful R5 Git commit/push path using `git -c safe.directory=... add/commit/push`.
  - Recorded the previously successful Docker runtime path: `docker compose -f compose.yml --profile app up -d --build`, live smoke, Dockerized Maven, frontend lint/build.
  - Added Git/Docker ACL recovery commands for `CodexSandboxOffline` / `CodexSandboxUsers` and the R6 retest sequence.
- `scripts/dev/diagnose-git.ps1`
  - Added a non-destructive Git harness check for safe-directory status, `.git` metadata writability, recent commits, and optional remote reachability.
- `scripts/dev/smoke.ps1`
  - Added required HTTP smoke coverage for backend health, login, profile read/update/password-check, dashboard, attendance, notifications, classmates, material detail/resources, quest/survey detail and submit, board detail/write/comment/reaction, QNA write, support ticket list/create.
  - Added source-level assertion for `CommunityController` classmate notification route.
  - Added dynamic seeded board post ID discovery so detail/comment/reaction checks do not assume a fixed `free` board post id.
  - Added optional diagnostics for:
    - `POST /api/community/classmates/{userId}/notifications` when the live backend image has not been rebuilt from R6 source yet.
    - `POST /api/learning/materials/{id}/reactions`, which remains future material-reaction work.

### PM Verification Results
- `git diff --check` -> PASS (line-ending warnings only).
- `npm run lint` from `frontend/` -> PASS.
- `npm run build` from `frontend/` -> PASS (`tsc -b && vite build`; 65 modules transformed).
- `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/dev/smoke.ps1 -SkipHttp` -> PASS.
- `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/dev/smoke.ps1` -> PASS for required live endpoints.
  - Optional warning: R6 classmate notification endpoint returned 404 from the currently running backend, which indicates the live container is still the pre-R6 image.
  - Optional warning: material reaction endpoint returned 404 because material reactions are not yet implemented.
- `docker compose -f compose.yml --profile app up -d --build` -> BLOCKED: Docker client cannot access the Docker engine pipe from this sandbox user.
- Backend `mvn test` -> BLOCKED: local Maven is unavailable on PATH.
- Dockerized Maven fallback -> BLOCKED: Docker engine pipe access is denied from this sandbox user.

### Coverage Gaps / Blockers
- Backend unit/integration tests are present for the new classmate notification contract but could not be executed in this sandbox.
- Live HTTP verification for `POST /api/community/classmates/{userId}/notifications` requires rebuilding/restarting the backend container from the R6 source, which is blocked by Docker ACL in this session.
- Material reaction API is deliberately optional in smoke until an implementation round adds it.

### Retest Commands For Host/CI
1. `docker compose -f compose.yml --profile app up -d --build`
2. `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/dev/smoke.ps1`
3. `cd backend; mvn -B test`
4. `cd frontend; npm run lint; npm run build`

### Remaining Risks
- Full backend test status is unknown until Maven or Dockerized Maven can run in a CI/host environment with Docker pipe access.
- Smoke write endpoints create demo records/comments/reactions against the running seeded environment; keep using disposable/local compose data for repeated runs.
