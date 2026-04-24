# Worker-2 Role Responsibility Check

Date: 2026-04-24
Team: ssafy-full-clone-omx-continuou
Worker: worker-2
Task: 13 - 팀 구성

## Team Composition Contract

This team run assumes the following lanes:

| Lane | Count | Responsibility |
| --- | ---: | --- |
| product-manager | 1 | PM, scope control, remaining-work judgement, and preventing false completion. |
| architect | 1 | Structural design, API/UI contracts, and Docker/application wiring coordination. |
| executor | 2 | Implementation split across backend-centered and frontend-centered work. |
| test-engineer | 1 | Docker Compose, Nginx, ELK, smoke tests, and documentation verification. |

## Worker-2 Lane

Worker-2 is operating as executor-2 and should keep work frontend-centered unless the leader explicitly widens scope.

| Responsibility | Worker-2 interpretation |
| --- | --- |
| React/Tailwind scaffold or cleanup | Verify and maintain `frontend/` React/Vite/npm setup without introducing another CSS framework. |
| Routing/layout | Own frontend route dispatch, application shell integration, and recovery screens. |
| Login/profile/attendance/materials/survey/board/QnA/notifications screens | Implement or repair screen-level behavior and connect screens to existing API client functions. |
| API client | Keep browser calls aligned to backend/Nginx contracts and preserve auth/forbidden event handling. |
| Loading/empty/error/unauthorized states | Ensure user-visible state handling exists instead of silent fallbacks or blank screens. |

## Boundary Notes

- Product-manager final PASS/PARTIAL/FAIL decisions remain outside worker-2 ownership.
- Architect-owned cross-service contract changes should be escalated before broad edits.
- Backend implementation belongs to executor-1 unless the leader explicitly assigns a backend slice to worker-2.
- DevOps/QA ownership remains with test-engineer; worker-2 may still run bounded verification for its own changes.

## Current Evidence

- Task 2 refreshed the repository status check and confirmed frontend lint/build plus Docker Compose config render successfully.
- Task 7 implemented an explicit frontend not-found route so unknown paths no longer silently render the dashboard.
- Verification for current worker-2 changes: `npm --prefix frontend run lint`, `npm --prefix frontend run build`, and `git diff --check` passed.

## Guardrail

Worker-2 must continue taking assigned frontend-centered tasks and should not declare full-clone completion while any PARTIAL, FAIL, UNKNOWN, failed test, or unverified live path remains.
