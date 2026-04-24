# Remaining Work

## Task 95 Required Work Check (worker-4, 2026-04-24)

Question: does `docs/remaining-work.md` have no required work left? **No.** Required work remains and must stay tracked.

Required non-PASS work still present:
- Production auth/session/RBAC: real credential verification, token/session persistence, expiry, role enforcement, 401/403 UI.
- Durable workflows: attendance appeal status/history, notifications send/read/delete, support ticket threads/answers/status, survey question/options and response persistence.
- Attachments/reactions: materials, boards, tickets, submissions, material like/bookmark/favorite.
- Permissions and management: board edit/delete/owner/moderator behavior, admin campus/cohort/class/track flows as needed.
- QA/release: backend Maven or Dockerized Maven in host/CI, rebuilt live smoke, browser E2E, CI workflow, final all-PASS verification.

Decision: keep this file non-empty and continue implementation tasks. Do not delete remaining work or declare final completion until these items are implemented and verified.

## Task 66 Required Documentation Recheck (2026-04-24)
- Required documents now exist, including newly created `docs/final-verification.md`.
- Documentation itself is no longer the blocker for Task 66.
- Product completion remains blocked by non-documentation gaps: production auth/RBAC, durable notification/support/survey/material workflows, attachments, permissions/edit-delete depth, browser E2E, CI, live rebuilt smoke, and backend Maven/CI verification.


## R7.0 Status Update (2026-04-24)
- Done in DevOps/QA slice: smoke JSON shape assertions for auth/profile/board critical paths, plus maintained `docs/openapi.yaml` bootstrap and `scripts/dev/verify-openapi.ps1` drift-marker check.
- Still needed before leaving R7.0: frontend `fetchJson` fallback policy must rethrow 401/403 and disable fallback in CI/live mode; frontend board adapters must normalize the backend `{ post }` / `{ item }` wrappers; live smoke and verify-openapi must be rerun in a host/CI environment with PowerShell and rebuilt services.


## R7.1 Worker-1 Status Update (2026-04-24)
- Done in frontend Auth/RBAC slice: App bootstrap now calls `GET /api/auth/roles/current`, displays current role/permission count in the shell, routes denied paths (for example `/admin`) to an explicit unauthorized state, and calls `POST /api/auth/logout` before returning to login.
- Verification: `npm run lint` and `npm run build` pass in `frontend/`. Backend Maven tests remain blocked in this host because only Java 25 is installed and the current Mockito/Byte Buddy stack reports Java 25 class instrumentation incompatibility; rerun under Java 21 or upgrade test tooling.

## Full Clone Completion Checklist
| Area | Status | Remaining Work |
|---|---|---|
| Login/session | partial | Demo login works; add real credential verification, sessions/tokens, expiry, password recovery. |
| Profile | partial | Read/update exists and frontend payload is aligned; add authorization checks and persistence depth. |
| Campus/cohort/class/track | partial | Admin campus/cohort/track/class demo management API and screen exist; add persisted CRUD, edit/delete, and server RBAC enforcement. |
| Attendance | partial | Records and appeal submit exist; add durable appeal workflow/status/history. |
| Notifications | partial | List exists and R6 source adds classmate send API; add durable send/read/delete persistence and live rebuild verification. |
| Curriculum/replays | partial | Lists exist and frontend adapters now map backend DTOs; add richer filters, replay authorization and progress state. |
| Materials/resources | partial | List/detail/resources exist and frontend adapters map backend DTOs; add attachments, viewer fidelity, like/bookmark/favorite. |
| Quest/evaluation | partial | List/detail/submit exists; add result detail, file attachments, grading status. |
| Survey | partial | List/detail/respond exists with DTO-aligned frontend payload; add full questions/options DTOs and persisted responses. |
| Board/community | partial | List/detail/write/comment/reaction exists; add attachments, edit/delete, permissions. |
| 1:1 inquiry | partial | Ticket list/create exists and QNA new page uses support tickets; add thread messages, answers, status transitions, attachments. |
| Access control | partial | Frontend role bootstrap and unauthorized route state exist; add server-side enforcement coverage and admin/operator role matrices. |
| Error/loading/empty states | partial | Present in many pages; verify all mutation flows and permission errors. |
| Local one-command run | partial | Compose profile works in prior live verification; current sandbox cannot rebuild due Docker ACL. |
| Tests/smoke | partial | Backend/frontend/smoke exist; add browser E2E and CI. |
| README/docs | partial | Add top-level runbook/API/progress/test docs and keep tracker updated. |


## Task-backed Continuation Map (worker-5, 2026-04-24)
The partial/gap checklist above is now represented by concrete OMX follow-up tasks so the team can continue without treating the project as complete:

| Task IDs | Area | Purpose |
|---|---|---|
| 117-119 | Auth/RBAC/Profile | Real credential/session behavior, role guards, unauthorized UI, profile authorization/persistence. |
| 120-123 | Admin/Attendance/Notifications/Curriculum | Admin management, attendance appeal workflow/history, persisted notification lifecycle, replay access/progress. |
| 124-128 | Attachments/Domain Depth | Material reactions/attachments, quest result/grading, survey questions/options/responses, board permissions/attachments, support ticket threads/answers/status. |
| 129-130 | QA/Docs | Browser E2E or CI smoke coverage and final verification/remaining-work synchronization. |

Completion remains blocked until these task-backed areas move from partial/gap to PASS with fresh verification evidence.

## Next Rounds
1. R7-auth-rbac: real session/token model, role guards, unauthorized UI states.
2. R8-attachments-reactions: file upload/download, board/material/ticket attachments, material reactions.
3. R9-survey-ticket-depth: full survey questions/options, support ticket thread/answers/status.
4. R10-e2e-ci-docs: browser E2E smoke, CI workflow, README/runbook finalization.

## PM Rule
At the end of every round, re-check this file against `docs/collaboration/API_CATALOG.md`, `docs/collaboration/SCREEN_CATALOG.md`, current controllers, frontend routes, and smoke coverage. If any completion criterion is still partial/gap, plan another round.

## Required Remaining-Work Classification

### 아직 PASS가 아닌 항목
- 모든 `partial` 항목: Login/session, Profile, Campus/cohort/class/track, Attendance, Notifications, Curriculum/replays, Materials/resources, Quest/evaluation, Survey, Board/community, 1:1 inquiry, Access control, Error/loading/empty states, Local one-command run, Tests/smoke, README/docs.
- 현재 `gap` 항목은 R7.1 기준 0개로 정리했지만, 서버 측 RBAC/권한 테스트는 아직 PASS가 아니다.

### PARTIAL 항목
- 기능 UI와 demo/API contract는 대부분 존재하지만, 실서비스 수준의 인증/세션, 권한 enforcement, 첨부파일, 설문/문의 depth, E2E/CI 검증이 남아 있다.

### FAIL 항목
- 현재 문서 기준 명시적인 기능 FAIL 항목은 없다.
- 단, 이 호스트의 backend Maven test 실행은 Java 25와 Mockito/Byte Buddy 호환성 문제로 FAIL한다. Java 21 환경 또는 테스트 의존성 업그레이드가 필요하다.

### UNKNOWN 항목
- Docker Compose live smoke 결과: 현재 워커 호스트에서 재검증하지 못했다.
- PowerShell 기반 `scripts/dev/verify-openapi.ps1` 결과: 현재 워커 호스트에서 재검증하지 못했다.
- 실제 SSAFY 원본 대비 pixel/interaction fidelity: browser E2E 및 visual capture 기준이 아직 부족하다.

### 다음에 생성해야 할 task
1. Java 21 CI/runtime 고정 또는 Mockito/Byte Buddy 테스트 도구 업그레이드로 backend test unblock.
2. Server-side RBAC guard를 controller/service 레벨에 적용하고 learner/operator/admin 권한 test 추가.
3. Admin campus/cohort/track/class demo flow를 persisted CRUD/edit/delete로 확장.
3. 첨부파일 업로드/다운로드 API와 board/material/ticket/submission 연결.
4. 설문 질문/선택지/응답 저장 depth 구현.
5. 지원 티켓 thread/answer/status transition 구현.
6. Browser E2E smoke와 GitHub Actions CI 추가.

### 위험 요소 / known issue
- Frontend fallback은 demo UX를 보장하지만 CI/live mode에서는 반드시 비활성 상태로 검증해야 한다.
- Backend test는 Java 25 환경에서 Mockito inline mock이 class instrumentation을 실패시키므로, Java 21 검증이 완료될 때까지 최종 PASS로 볼 수 없다.
- `docs/final-verification.md`가 아직 없으므로 최종 완료 선언 금지.

### 완료 판단
- 이 문서에 남은 partial/gap/unknown 항목이 존재하므로 SSAFY 풀 클론은 아직 완료가 아니다.
- 다음 실행은 위 task backlog를 이어서 처리해야 한다.
