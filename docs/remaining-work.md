# Remaining Work

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
| Access control | gap | Add role matrix enforcement and frontend unauthorized states. |
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
