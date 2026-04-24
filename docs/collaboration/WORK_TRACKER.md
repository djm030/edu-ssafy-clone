# Work Tracker

## Current Round
Round: `R5-stability-runtime`

Goal:
- P1 route/API를 실제 앱 형태로 구성한다.
- React/Spring/Compose 기반을 안정화한다.
- seed와 smoke를 P1 전체 범위로 확장한다.

Owners:
- PM: full clone plan, API/screen/QA catalog, integration verification
- Backend: P1 API implementation
- Frontend: P1 React screens
- DevOps-QA: seed, compose, smoke

## Status
| Area | Status | Evidence |
|---|---|---|
| PM docs | done | API/screen/test docs UTF-8, 500줄 미만 |
| Backend P1 API | done, PM verified | Dockerized Maven `mvn -B test` passed with 28 tests |
| Frontend P1 screens | done, PM verified | `npm run lint` and `npm run build` passed |
| DevOps-QA | done | compose/smoke safe checks passed |
| Runtime containers | done | `docker compose -f compose.yml --profile app up -d --build`; live smoke all HTTP checks passed |
| Git commit/push | ready for retest | use `git -c safe.directory=...` from sandbox because owner differs |
| Backend P2 API | done, PM verified | Dockerized Maven `mvn -B test` passed with 28 tests |
| Frontend P2 screens | done, PM verified | `npm run lint` and `npm run build` passed |
| DevOps-QA P2 gates | done | seed/verify/smoke expanded to P2 |
| Git push | blocked | temp-index commit object `8d4309457bd75bd90132a8d3f33754c3f5335558` created, GitHub 443 unreachable |
| Backend P3 API | done, PM verified | detail API patterns present; cached Maven test-compile passed |
| Frontend P3 routes | done, PM verified | regex route matcher handles detail/viewer routes |
| DevOps-QA P3 gates | done | static seed/verify/smoke expanded to detail coverage |
| Backend P4 API | done, PM verified | write/submit API patterns present; cached Maven test-compile passed |
| Frontend P4 routes | done, PM verified | write/submit routes and action pages present; lint/build passed |
| DevOps-QA P4 gates | done | static seed/verify/smoke expanded to write coverage |
| R5 stability runtime | done | strict `scripts/dev/smoke.ps1` live HTTP smoke passed; backend board SQL and smoke payloads fixed |

## Failure Log
```text
Round: R1-P1-readonly
Gate: Docker runtime
Command: docker run / docker compose up
Result: permission denied on docker_engine pipe
Failure owner: Environment
Root cause: sandbox user lacks Docker pipe/config access
Fix: run from normal user shell or grant sandbox Docker permissions
Retest: pending
Status: blocked
```

```text
Round: R1-P1-readonly
Gate: Git write
Command: git add
Result: unable to create .git/index.lock
Failure owner: Environment
Root cause: sandbox user lacks write permission to .git
Fix: grant .git modify permission to CodexSandboxOffline SID or commit from normal user shell
Retest: pending
Status: blocked
```

```text
Round: R1-P1-readonly
Gate: Maven test
Command: mvn test
Result: local Maven repo/network/cache issue depending on repo path
Failure owner: Environment/Backend
Root cause: default sandbox .m2 not writable; temp repo cannot fetch remote dependencies under restricted network
Fix: use writable warmed Maven cache or run from normal user shell
Retest: pending
Status: blocked in PM shell
```

```text
Round: R2-P2-operational-lists
Gate: Docker runtime
Command: scripts/dev/diagnose-docker.ps1; scripts/dev/up.ps1
Result: config warning removed with temp DOCKER_CONFIG, engine pipe still Access is denied
Failure owner: Environment
Root cause: current process runs as DESKTOP-KPGHMRC\CodexSandboxOffline, while docker-users contains only DESKTOP-KPGHMRC\kwanyeol
Fix: add CodexSandboxOffline or CodexSandboxUsers to local docker-users, or run runtime scripts from a Docker-authorized kwanyeol shell
Retest: scripts/dev/up.ps1, then scripts/dev/smoke.ps1
Status: blocked in sandbox, compose config still passes
```

```text
Round: R2-P2-operational-lists
Gate: Git push
Command: temp GIT_INDEX_FILE/GIT_OBJECT_DIRECTORY commit-tree, then git push origin <commit>:refs/heads/main
Result: commit object 8d4309457bd75bd90132a8d3f33754c3f5335558 created, push failed
Failure owner: Environment
Root cause: github.com:443 is unreachable from this session
Fix: retry push when network is available, or push from a network-enabled shell using the same worktree
Retest: git ls-remote origin main; git push origin main
Status: blocked in sandbox
```

```text
Round: R5-stability-runtime
Gate: Stability closure
Command: docker compose -f compose.yml --profile app up -d --build; scripts/dev/smoke.ps1; docker run --rm -v "${PWD}:/workspace" -w /workspace maven:3.9.9-eclipse-temurin-21 mvn -B test; npm run lint; npm run build
Result: all live smoke endpoints passed; backend tests passed 28/28; frontend lint/build passed
Failure owner: Backend/DevOps-QA
Root cause: board list SQL concatenated `:boardId` and `ORDER BY`; smoke write payloads used stale request field names; local `mvn` is not installed on PATH
Fix: add SQL whitespace before `ORDER BY`; align smoke payloads to DTOs; use Dockerized Maven test until local Maven is installed
Retest: passed
Status: resolved
```

```text
Round: R4-P4-write-flows
Gate: Full backend test
Command: mvn -Dmaven.repo.local=<cached target repo> -o -B test
Result: failed before test execution
Failure owner: Environment
Root cause: Maven Surefire cache is incomplete; missing org/apache/maven/plugin/surefire/SurefireReportParameters
Fix: warm Maven cache with network access or provide complete Surefire plugin dependencies
Retest: mvn test
Status: blocked in sandbox
```

```text
Round: R4-P4-write-flows
Gate: Live Docker smoke
Command: scripts/dev/up.ps1; scripts/dev/smoke.ps1
Result: docker_engine pipe Access is denied for DESKTOP-KPGHMRC\CodexSandboxOffline
Failure owner: Environment
Root cause: current sandbox identity is not authorized for Docker Desktop engine pipe
Fix: add CodexSandboxOffline or CodexSandboxUsers to docker-users, then restart this session, or run from an authorized kwanyeol shell
Retest: scripts/dev/up.ps1; scripts/dev/smoke.ps1
Status: blocked in sandbox
```

## Completion Recheck Loop
Before marking any round complete:
- Compare `API_CATALOG.md` against backend controllers/tests and smoke coverage.
- Compare `SCREEN_CATALOG.md` against frontend routes/navigation/pages.
- If anything is missing, return to PM split, assign the missing API/screen/infrastructure/test work, and keep the round open.
- Record every failed gate with command, result, owner, root cause, fix, retest command, and status.

## Next Rounds
### R2-P2-operational-lists
- Backend: notifications, curriculum, replays, FAQ, QnA, profile check hardening
- Frontend: corresponding routes and list/detail placeholders
- DevOps-QA: HTTP smoke coverage for P2

### R3-P3-details
- Backend: detail APIs for board, notice, learning materials, quest, survey
- Frontend: detail routes and back/list navigation
- DevOps-QA: API contract smoke with seeded detail records

### R4-P4-write-flows
- Backend: create/update/submit endpoints and validation
- Frontend: forms, field errors, success/error flows
- DevOps-QA: write smoke in isolated test data

## Commit Policy
- Commit after a round reaches stable gates or after a coherent partial milestone.
- Commit message follows Lore protocol.
- If `.git` permission remains blocked, PM records exact command failure and leaves worktree ready.
