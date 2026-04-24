# Remaining Work

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


## Worker-4 Incomplete Feature Audit (2026-04-24)

Repository evidence checked for Task 4: backend controllers under `backend/src/main/java/com/edussafy/backend/**`, frontend routes/pages under `frontend/src/**`, `docs/api-summary.md`, and current smoke/test notes. The app has a real Spring Boot backend and React frontend, but the clone is still **partial**, not full PASS.

| Feature area | Current evidence | Completion status | Follow-up needed |
|---|---|---|---|
| Authentication/session/RBAC | `AuthController` exposes login/me/current-role/logout/password-check demo endpoints. | PARTIAL | Add real credential/session or token persistence, expiry, role enforcement, and frontend unauthorized/error states. |
| Profile | `ProfileController` has read/update and frontend edit flow. | PARTIAL | Persist deeper profile fields, add authorization checks, and verify live update round-trip. |
| Attendance | Records and appeal submission endpoints/pages exist. | PARTIAL | Add durable appeal status/history workflow and reviewer/admin path. |
| Notifications/classmates | Notifications list plus classmate notification POST route exist. | PARTIAL | Add durable read/delete/send persistence and rebuild/live smoke verification for the new classmate route. |
| Learning materials/replays | Curriculum, replay, materials list/detail/resources routes exist. | PARTIAL | Add attachment download/viewer fidelity, material like/bookmark/favorite/reaction endpoints, authorization, and progress state. |
| Quest/survey | Quest list/detail/submit and survey list/detail/respond endpoints exist. | PARTIAL | Add grading/result details, file attachments, full survey question/option DTOs, and persisted responses. |
| Board/community | Board list/detail/write/comment/reaction endpoints exist. | PARTIAL | Add edit/delete, attachments, permission checks, and owner/moderator behavior. |
| Support/QNA | Ticket list/create and QNA page exist. | PARTIAL | Add ticket detail thread messages, answers, status transitions, attachments, and staff/admin response flows. |
| Tests/CI/E2E | Unit/controller tests, frontend lint/build, and smoke harness exist. | PARTIAL | Add browser E2E, CI workflow, live smoke in rebuilt services, and final verification doc. |

Task impact: no area above can be marked full-clone PASS yet; existing follow-up tasks 83-116 already cover final pass checks, verification, docs, and additional task creation.

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
