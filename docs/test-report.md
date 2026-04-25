# Test Report

## Final Verification Evidence Refresh (2026-04-25)

### Summary
최종 검증 책임자 관점에서 backend/frontend/build/schema/OpenAPI/Docker/runtime smoke를 다시 점검했다. 결론은 **NOT COMPLETE / PARTIAL**이다. Backend/Frontend/정적 OpenAPI는 주요 gate를 통과하지만, live localhost HTTP, Swagger UI `/v3/api-docs`, attachment/support/auth depth, browser E2E가 완료 기준을 만족하지 못한다.

### Commands Run
- `git status --short`, `git log --oneline -5 --decorate` -> PASS, 저장소/최근 커밋 확인.
- `omx team status ssafy-full-clone-omx-continuou` -> NOT PASS, `No team state found`; dead team cleanup 이후 정상 완료 상태 아님.
- `docker compose -f compose.yml --profile app config --services` -> PASS (`mysql`, `rabbitmq`, `redis`, `backend`, `frontend`, `nginx`).
- `docker compose -f compose.yml --profile app config --quiet` -> PASS.
- `docker compose -f compose.yml --profile app ps` -> PARTIAL, 기존 컨테이너들은 healthy로 보이나 current shell curl과 current compose defaults 기준 live PASS는 아님.
- `docker compose -f compose.yml --profile app logs --tail=40 backend nginx` -> PARTIAL, backend startup/MySQL/RabbitMQ/Nginx API 200 로그 확인.
- `curl -fsS --max-time 3 http://127.0.0.1:8080/actuator/health` -> FAIL, `Couldn't connect to server`.
- `curl -fsS --max-time 3 http://127.0.0.1/nginx-health` / `/api/health` -> FAIL, current shell에서 localhost 연결 실패.
- `bash scripts/dev/backend-test.sh --help` -> PASS.
- `bash scripts/dev/backend-test.sh --skip-cache` -> PASS, Dockerized Maven Java 21, `Tests run: 47, Failures: 0, Errors: 0, Skipped: 0`, `BUILD SUCCESS`.
- `docker run --rm -v .../backend:/workspace -w /workspace maven:3.9.9-eclipse-temurin-21 mvn -B test` -> PASS, 동일하게 47 tests PASS.
- `npm --prefix frontend run lint` -> PASS.
- `npm --prefix frontend run build` -> PASS.
- `python3 scripts/dev/smoke-lite.py --with-frontend` -> PASS/PARTIAL, `PASS=15, FAIL=0, SKIP=1`.
- `python3 scripts/dev/smoke-lite.py --http --with-frontend` -> PASS/PARTIAL, `PASS=15, FAIL=0, SKIP=4` (HTTP probe skip).
- `python3 -m json.tool docs/openapi.json` -> PASS, valid JSON.
- `ruby -ryaml -e ... docs/openapi.yaml` -> PASS, OpenAPI `3.0.3`, paths `42`.
- controller-vs-OpenAPI drift Python check -> PASS/PARTIAL, controller ops `50`, OpenAPI ops `51`, missing `[]`, extra excluding actuator `[]`.
- repository-vs-schema Python check -> PASS, repository tables `32`, missing schema tables `[]`.
- `grep -R "springdoc\|openapi\|swagger" backend/...` -> FAIL for runtime Swagger, backend has no Springdoc/Swagger setup.
- `git diff --check` -> PASS.


### Mandatory ai-slop-cleaner Standard Pass
- Scope file: `.omx/ralph/changed-files.txt`.
- Mode: standard.
- Behavior lock: backend Maven tests, frontend lint/build, smoke-lite, OpenAPI drift/schema checks were already available and were rerun after cleanup.
- Cleanup plan: stay inside the changed-files list, remove stale task/team completion wording, do not introduce new dependencies or broad refactors.
- Cleanup performed: updated stale `task 125-130` / historical active-team wording so docs no longer imply a currently active/completed OMX team state.
- Post-cleaner gates: `npm --prefix frontend run lint && npm --prefix frontend run build` PASS; `bash scripts/dev/backend-test.sh` PASS (`47` tests); `python3 scripts/dev/smoke-lite.py --http --with-frontend` PASS/PARTIAL (`PASS=15`, `FAIL=0`, `SKIP=4`); OpenAPI JSON/YAML/controller drift PASS; DB schema check PASS; compose config PASS; `git diff --check` PASS.

### Result
- PASS: backend tests, frontend lint/build, compose config render, schema/table static check, static OpenAPI JSON/YAML validation, controller/OpenAPI drift check.
- PARTIAL: Docker runtime evidence and smoke harness, because current shell cannot reach localhost even though compose state/logs show a running prior stack.
- FAIL: runtime Swagger UI `/v3/api-docs`, common attachment upload/download, support ticket answers/attachments, browser E2E/visual evidence, normal OMX team terminal completion.

### Cause Classification
| Item | Classification | Evidence |
|---|---|---|
| `/v3/api-docs` missing | API 문서 런타임 불일치 | Springdoc/Swagger dependency/config grep result is empty. |
| localhost curl failure | 실행환경/네트워크 검증 실패 | current shell `curl` to backend/nginx ports cannot connect. |
| Attachment gaps | 기능 미구현 | Common upload/download API and domain E2E links are absent. |
| Support answer gaps | 기능 미구현 | Ticket list/create exists, answer/thread/status/attachments missing. |
| E2E missing | 테스트 하네스 부족 | Backend/frontend unit/build pass, but browser automation evidence absent. |
| Team state lost | OMX runtime recovery 필요 | `No team state found` after dead team cleanup; not all-complete terminal. |

### Retest Commands
```bash
bash scripts/dev/backend-test.sh --skip-cache
npm --prefix frontend run lint
npm --prefix frontend run build
python3 scripts/dev/smoke-lite.py --http --with-frontend
python3 -m json.tool docs/openapi.json >/tmp/openapi_json_check
ruby -ryaml -e "doc=YAML.load_file('docs/openapi.yaml'); puts [doc['openapi'], doc['paths'].size].join(' ')"
docker compose -f compose.yml --profile app config --quiet
docker compose -f compose.yml --profile app up -d --build
curl -fsS http://127.0.0.1:18080/actuator/health
curl -fsS http://127.0.0.1:18000/nginx-health
```

## Worker-5 Task Recreation Verification (2026-04-25)

### Summary
`미완성 기능을 task로 생성` 범위를 다시 실행해, 현재 팀 상태에 실제 존재하는 follow-up task(117-124)를 재생성하고 문서 참조를 동기화했다.

### Commands Run
- `omx team api read-task --input '{"team_name":"ssafy-full-clone-omx-continuou","task_id":"117"}' --json` ~ `124` -> PASS (모든 task 존재, owner 할당 확인).
- `rg -n "117-124|Task-backed Continuation Map \\(worker-5, 2026-04-25\\)" docs/progress.md docs/remaining-work.md` -> PASS.
- `git diff --check` -> PASS.

### Result
- PASS: incomplete feature backlog가 실행 가능한 task id 117-124로 정리됨.
- PASS: `docs/progress.md`, `docs/remaining-work.md`가 현재 팀 상태와 일치하도록 갱신됨.

## Compose Duplicate Guard Verification (worker-5, 2026-04-25)

### Summary
`compose*.yml`를 점검해 같은 목적의 compose 파일을 새로 중복 생성하지 않았는지 확인했다. 신규 compose 파일 추가는 없었고, 기존 파일 구조만 유지된다.

### Commands Run
- `ls -1 compose*.yml` -> PASS (`compose.yml`, `compose.mysql.yml`, `compose.observability.yml`).
- service key 추출/중복 집계 스크립트 -> PASS; 중복 서비스명은 `mysql` 1건이며 `compose.yml`(통합 app profile)과 `compose.mysql.yml`(mysql 단독 실행 용도) 사이의 기존 분리 구성임.
- `docker compose -f compose.yml config` -> PASS.

### Result
- PASS: 동일 목적의 신규 compose 파일 중복 생성 없음.
- Note: 기존 `mysql` 서비스명 중복은 의도된 실행 모드 분리로 판단되며, 이번 작업에서 compose 파일 구조 변경은 없다.

## Runtime Failure Fix via Compose Logs/Output (worker-5, 2026-04-25)

### Failure Observed
- `docker compose -f compose.yml --profile app up -d` 실행 중 실패:
  - 1차: 고정 `container_name` 충돌 (`ssafy-clone-*` already in use)
  - 2차: host port 충돌 (`5672`, `6379`, `8080` already allocated)

### Minimal Fix Applied
- `compose.yml`, `compose.mysql.yml`, `compose.observability.yml`에서 `container_name` 제거 (프로젝트별 자동 네이밍 사용).
- `compose.yml` 기본 host port fallback을 충돌-완화 값으로 조정:
  - MySQL `13306`, Redis `16379`, RabbitMQ `25672/25673`, Backend `18080`, Nginx `18000`.
- `.env.example`를 동일 기본값으로 동기화.

### Reverification
- `docker compose -f compose.yml config` -> PASS
- `docker compose -f compose.mysql.yml config` -> PASS
- `docker compose -f compose.observability.yml config` -> PASS
- `docker compose -f compose.yml --profile app up -d` -> PASS
- `curl -fsS http://localhost:18000/nginx-health` -> PASS (`ok`)

### Cause Classification
- `container_name` 충돌: Docker 설정 불일치 (동시 실행 환경에서 이름 강제 고정)
- port bind 충돌: 환경 포트 충돌 (동일 호스트 다중 스택 실행)

## Repository Scope Check (Task 99, worker-5, 2026-04-25)

### Question
저장소가 Docker 설정만으로 구성되어 있는가?

### Verification
- 루트 확인: `ls -1` -> `backend/`, `frontend/`, `docs/`, `scripts/`, `infra/` 존재.
- 소스 확인:
  - `ls backend/src/main/java/com/edussafy/backend` -> `BackendApplication.java`, `board/`, `priority/` 확인.
  - `ls frontend/src` -> `App.tsx`, `api/`, `pages/`, `types.ts` 등 확인.

### Conclusion
- FAIL (for the proposition "Docker 설정만 있다"): 저장소는 Docker 설정 전용이 아니라 backend/frontend 코드, 문서, 스크립트를 함께 포함한다.

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
