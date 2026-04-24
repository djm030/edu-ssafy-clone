# Test Report

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
