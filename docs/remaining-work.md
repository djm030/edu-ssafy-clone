# Remaining Work

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
| Campus/cohort/class/track | partial | Seeded/read through user/classmate views; add management/admin flows if required. |
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

## Next Rounds
1. R7-auth-rbac: real session/token model, role guards, unauthorized UI states.
2. R8-attachments-reactions: file upload/download, board/material/ticket attachments, material reactions.
3. R9-survey-ticket-depth: full survey questions/options, support ticket thread/answers/status.
4. R10-e2e-ci-docs: browser E2E smoke, CI workflow, README/runbook finalization.

## PM Rule
At the end of every round, re-check this file against `docs/collaboration/API_CATALOG.md`, `docs/collaboration/SCREEN_CATALOG.md`, current controllers, frontend routes, and smoke coverage. If any completion criterion is still partial/gap, plan another round.
