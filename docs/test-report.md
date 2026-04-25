# Test Report

## Role Access Matrix Verification (2026-04-26 KST)
- Added focused interceptor tests for public health, missing-session JSON errors, authenticated learner reads, and protected mutation role gates for survey creation, attendance resolve, classmate notifications, support answers, and admin campus structure.
- `docker run --rm -v "$PWD:/workspace" -w /workspace/backend maven:3.9.9-eclipse-temurin-21 mvn -B -Dtest=RoleAccessInterceptorTest test` -> PASS, Tests run: 17, Failures: 0, Errors: 0, Skipped: 0.


## Survey Creation REST Docs Verification (2026-04-26 KST)
- Added Spring REST Docs coverage for `POST /api/surveys` request/response fields and generated the `survey-create` snippet from a focused MVC test.
- `docker run --rm -v "$PWD:/workspace" -w /workspace/backend maven:3.9.9-eclipse-temurin-21 mvn -B -Dtest=SurveyRestDocsTest test` -> PASS, Tests run: 1, Failures: 0, Errors: 0, Skipped: 0.
- `docker compose config` -> PASS.


## Survey Creation Feature Verification (2026-04-26 KST)
- Implemented `POST /api/surveys` for coach/admin survey creation with persisted survey, first question, and choice options.
- Added backend controller/service tests for staff creation and learner forbidden behavior.
- Added frontend survey create form connected to the real API for users with `survey:manage`.
- `docker run --rm -v "$PWD:/workspace" -w /workspace/backend maven:3.9.9-eclipse-temurin-21 mvn -B test` -> PASS, Tests run: 115, Failures: 0, Errors: 0, Skipped: 0.
- `cd frontend && npm run build` -> PASS.
- `cd frontend && npm run lint` -> PASS.
- `docker compose config` -> PASS.


## Final Verification Direct Recheck (2026-04-26 KST)
- `docker run --rm -v "$PWD:/workspace" -w /workspace/backend maven:3.9.9-eclipse-temurin-21 mvn -B test` -> PASS, Tests run: 111, Failures: 0, Errors: 0, Skipped: 0.
- `cd frontend && npm run build` -> PASS, TypeScript build and Vite production build completed; 69 modules transformed.
- `cd frontend && npm run lint` -> PASS.
- `docker compose config --services` -> PASS for mysql/rabbitmq/redis.
- `docker compose --profile app config --services` -> PASS for mysql/rabbitmq/redis/backend/frontend/nginx.
- `docker compose --profile app ps` -> PASS, running backend/frontend/mysql/nginx/rabbitmq/redis are healthy.
- Backend/Nginx smoke -> PASS: `localhost:18080/actuator/health`, `localhost:18000/`, and `localhost:18000/api/health` returned HTTP 200.
- Session/domain smoke -> PASS: seeded login through Nginx created a session and `/api/me`, attendance, board, survey, quest, support list endpoints returned HTTP 200.
- `docker compose --profile app up -d --build` -> BLOCKED/CANCELLED: Docker Hub base-image metadata loading stalled; existing app profile stayed healthy.
- Browser E2E/visual -> UNKNOWN: no browser E2E harness was available in this verification pass.


## Final Verification Recheck (2026-04-24)
- `docker run --rm -v .../backend:/workspace -w /workspace maven:3.9.9-eclipse-temurin-21 mvn -B test` -> PASS, Tests run: 34, Failures: 0, Errors: 0, Skipped: 0.
- `npm --prefix frontend run lint` -> PASS after App shell prop/state fix.
- `npm --prefix frontend run build` -> PASS, Vite transformed 67 modules.
- `docker compose -f compose.yml --profile app build backend frontend` -> PASS.
- `docker compose -f compose.yml --profile app up -d` -> PASS; after correcting nginx healthcheck to `127.0.0.1`, mysql/redis/rabbitmq/backend/frontend/nginx are healthy.
- Local HTTP smoke -> PASS for `/nginx-health`, `/actuator/health`, `/api/me`, `/api/auth/login`, `/api/auth/roles/current`, `/api/attendance/records`, `/api/learning/materials`, `/api/boards/free/posts`.
- `pwsh`/`powershell` smoke remains UNKNOWN on this macOS host because PowerShell is not installed.


## Team Recovery State (worker-5, 2026-04-24)

### Summary
The OMX team pipeline is still active in `team-exec`, but worker-5 has no remaining claimable original assignments. Pending work is owned by non-reporting/dead workers, and direct claim attempts on another worker's assigned task return `claim_conflict`.

### Current Team State
- Team: `ssafy-full-clone-omx-continuou`
- Task totals from `omx team api get-summary`: total=132, completed=105, pending=22, in_progress=4, failed=1.
- Dead/non-alive workers reported by summary: worker-1, worker-2, worker-3, worker-4, worker-5.
- Failed task requiring continuation: task 50 (`인증/인가가 필요한 기능은 권한 검사가 있다.`) failed truthfully because backend authorization enforcement is incomplete.
- Stale/in-progress continuation risk: task 118 remains in progress under worker-3 while worker-3 is non-alive.

### Recovery Recommendation
Do not declare terminal success yet. Reassign or clean up stale ownership, then continue tasks 117-128 and 131 until `docs/final-verification.md` can move every core row to PASS. Suggested commands for the leader/host lane:

```bash
omx team status ssafy-full-clone-omx-continuou
omx team api cleanup --input '{"team_name":"ssafy-full-clone-omx-continuou","force":true,"confirm_issues":true}' --json
git worktree prune
find . -name ".DS_Store" -delete
git status --short
```

### Worker-5 Boundary
Worker-5 documented the recovery state and reported upward instead of force-cleaning the team runtime, matching the worker dead/zombie prevention rule.

## R10 CI Smoke Workflow (worker-5, 2026-04-24)

### Summary
Added `.github/workflows/ci.yml` as a repository-level CI smoke gate. It validates Docker Compose rendering, checks maintained OpenAPI/smoke marker coverage, runs backend Maven tests in a Java 21 environment, and runs frontend `npm ci`, lint, and build in Node 22. This provides CI coverage without adding new project dependencies.

### Local Verification
- `git diff --check` -> PASS.
- `docker compose -f compose.yml config` -> PASS.
- `docker compose -f compose.yml --profile app config` -> PASS.
- `docker compose -f compose.observability.yml config` -> PASS.
- `grep` for CI workflow commands and OpenAPI/smoke markers -> PASS.
- `npm --prefix frontend run lint` -> PASS.
- `npm --prefix frontend run build` -> PASS.

### Not Verified Locally
- GitHub Actions execution itself was not run from this worker.
- Backend Maven tests still cannot run locally because `backend/mvnw` is absent and local `mvn` is unavailable; CI uses hosted Maven/Java instead.

## Worker-5 Continuation Verification (2026-04-24)

### Summary
Verified that the documented API/screen coverage is represented in the frontend route table and API adapters, then refreshed frontend dependency setup so lint/build are not left in a failed state. Backend Maven remains blocked in this worker because no wrapper is present and `mvn` is unavailable.

### Commands Run
- `omx team api list-tasks --input '{"team_name":"ssafy-full-clone-omx-continuou"}' --json` -> PASS, 130 total tasks after task ids 117-130 were created.
- `grep`/`sed` inspection of `frontend/src/App.tsx`, `frontend/src/api/app.ts`, `frontend/src/api/boards.ts`, and `frontend/src/api/client.ts` -> PASS, screens/call points and error handling are present for the core documented domains.
- `npm --prefix frontend ci` -> PASS, dependencies installed with 0 vulnerabilities; Node engine warning only.
- `git diff --check` -> PASS.
- `npm --prefix frontend run lint` -> PASS.
- `npm --prefix frontend run build` -> PASS, Vite built 65 modules.
- Backend tests -> BLOCKED, `backend/mvnw` is absent and local `mvn` is unavailable.

### Reverification Result
Frontend lint/build are passing after dependency installation. No Docker execution failure logs were present because no compose services are running in this worker. The project is still not complete while `docs/remaining-work.md` contains partial/gap rows and follow-up tasks 117-130 remain pending.

### Failure/Blocker Cause Classification
| Item | Classification | Evidence |
|---|---|---|
| Initial frontend lint/build failure before `npm ci` | 의존성 문제 | `eslint` and `tsc` binaries were missing because `frontend/node_modules` had not been installed in this worker. |
| Backend Maven tests not executed | 테스트 하네스 부족 / 의존성 문제 | `backend/mvnw` is absent and local `mvn` is unavailable. |
| Live Nginx reverse-proxy smoke not executed | worker/runtime 문제 | Compose services are not running in this worker; host/CI compose startup is required. |
| Live ELK ingestion/log confirmation not executed | worker/runtime 문제 | Observability compose config renders, but live containers/log ingestion require host/CI compose startup. |
| Remaining full-clone partial/gap rows | 코드 오류 아님 / 문서 불일치 방지 대상 | Missing product depth is tracked in `docs/remaining-work.md` and task ids 117-130 instead of being declared complete. |

### Report Criteria Coverage
- 실행한 검증 명령: recorded above under Commands Run.
- 성공/실패 결과: PASS/BLOCKED status is listed per command.
- 실패 로그 요약: frontend tool-missing state was resolved by `npm ci`; backend Maven is blocked because no wrapper/local Maven exists.
- Docker compose 검증 결과: `docker compose -f compose.yml config` and `docker compose -f compose.yml --profile app config` pass; compose logs had no failure output because services are not running in this worker.
- Nginx 검증 결과: Nginx configuration remains part of the existing `compose.yml --profile app config` rendering and `infra/nginx/conf.d/default.conf`; live reverse-proxy smoke still requires host/CI compose startup.
- ELK 로그 확인 결과: `compose.observability.yml` service configuration is present for Elasticsearch/Logstash/Filebeat/Kibana; live ELK log ingestion still requires host/CI compose startup.
- 수정 내용: task-backed docs were updated in `docs/progress.md`, `docs/remaining-work.md`, and this report.
- 재검증 결과: frontend lint/build and compose config pass after docs updates.
- 검증하지 못한 항목과 이유: backend Maven, live Nginx reverse proxy, and live ELK ingestion are blocked in this worker by missing Maven wrapper/local Maven and absence of running Docker services.

## R7.0 DevOps/QA Smoke Shape Guardrail (worker-5, 2026-04-24)

### Summary
Added live JSON shape assertions to `scripts/dev/smoke.ps1` for critical auth/profile/board contracts. This closes part of the R7.0 smoke requirement: the harness now fails when required wrappers/fields are absent, even if the endpoint returns HTTP 200.

### Commands Run
- `docker compose -f compose.yml config` -> PASS.
- `python3` static marker check for `scripts/dev/smoke.ps1`, `docs/openapi.yaml`, and `scripts/dev/verify-openapi.ps1` -> PASS.
- `wc -l scripts/dev/smoke.ps1` -> PASS, 373 lines and below the script's 500-line guardrail.
- `command -v pwsh || command -v powershell` -> BLOCKED, no PowerShell runtime installed in this worker environment.
- `ls frontend/node_modules` -> BLOCKED, dependencies absent; frontend lint/build were not runnable without installing packages.

### Coverage Added
- Login/current-user smoke asserts required `user` fields.
- Profile smoke asserts required `profile` fields.
- Board smoke asserts list pagination, detail `{ post }` wrapper, and create `{ item }` wrapper.

### Retest Commands For Host/CI
1. Install/enable PowerShell (`pwsh`) for script execution.
2. `pwsh -NoProfile -ExecutionPolicy Bypass -File scripts/dev/smoke.ps1 -SkipHttp`
3. `docker compose -f compose.yml --profile app up -d --build`
4. `pwsh -NoProfile -ExecutionPolicy Bypass -File scripts/dev/smoke.ps1`
5. `cd frontend && npm ci && npm run lint && npm run build`

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
