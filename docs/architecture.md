# Architecture Summary

## Required Docs Architecture Snapshot (Task 66, 2026-04-24)
- Documentation now treats the repository as a layered full-stack clone scaffold: React/Vite frontend, Spring Boot API backend, MySQL-backed repository layer with demo fallbacks, Docker Compose runtime, and PowerShell smoke/dev harnesses.
- The architecture is intentionally recorded as **partial** rather than final: controller/routes exist for core domains, but production completion depends on auth/session/RBAC enforcement, durable workflow persistence, attachment storage, browser E2E, CI, and rebuilt live smoke evidence.
- Required documentation entry points are `README.md` for operator runbook, `docs/api-summary.md` for endpoint inventory, `docs/test-report.md` for verification evidence, `docs/remaining-work.md` for non-PASS items, and `docs/final-verification.md` for final readiness status.


## Worker-4 Repository Analysis Snapshot (2026-04-24)
- Source layout: backend Java sources are split between `backend/src/main/java/com/edussafy/backend/board/**` for board/category/post APIs and `backend/src/main/java/com/edussafy/backend/priority/**` for auth/profile/dashboard/attendance/learning/quest/survey/community/support APIs.
- Backend tests currently cover board controllers plus priority controller/service paths under `backend/src/test/java/com/edussafy/backend/**`; Maven execution still depends on a host with Maven or Docker access.
- Frontend layout: `frontend/src/App.tsx` owns path dispatch, `frontend/src/api/client.ts` owns fetch/error/fallback policy, `frontend/src/api/app.ts` and `frontend/src/api/boards.ts` own backend DTO normalization, and feature screens live under `frontend/src/pages/**`.
- Dev/runtime layout: `compose.yml` orchestrates the application profile, `infra/nginx/conf.d/default.conf` fronts browser/API traffic, `scripts/mysql/**` seeds/verifies MySQL, and `scripts/dev/**` provides localhost, smoke, compose, Git, Docker, and OpenAPI verification helpers.
- Analysis outcome: the repository is a partial but runnable full-stack clone scaffold; the highest-risk gaps remain production auth/RBAC, attachment storage, material reactions, survey/support depth, browser E2E, and CI automation.


## Worker-4 Audit Documentation Boundary (Task 23, 2026-04-24)
- Task 4/23 changes are documentation-only and do not alter runtime architecture, API behavior, service names, exposed ports, Docker networks, or volumes.
- The audit classifies the current implementation as a partial full-stack clone: Spring controllers and React routes exist for the main SSAFY surfaces, while production-grade durability/authorization depth is intentionally tracked as remaining architecture work.
- Completion-critical gaps are now mirrored in `docs/remaining-work.md`: auth/session/RBAC, durable notifications, material attachments/reactions, quest/survey/support depth, board permissions/edit-delete, browser E2E, CI, live smoke, and final verification.
- The frontend fallback boundary remains architecturally important: local demo fallback is acceptable for unavailable dev backends, but CI/live modes and 401/403 responses must not be masked before final PASS.

## R7.0 Smoke Contract Boundary (2026-04-24)
- The DevOps/QA smoke harness now has explicit JSON contract assertions for auth/profile/board paths. This makes the smoke layer a contract boundary rather than a simple availability check.
- Critical wrappers are enforced in live smoke: auth/current-user use `{ user }`, profile uses `{ profile }`, board detail uses `{ post }`, board create uses `{ item }`, and board list requires `{ items, page }`.
- This is intentionally separate from frontend fallback behavior: R7.0 still must update `frontend/src/api/client.ts` so 401/403 and CI/live failures are not masked by local demo fallbacks.
- `docs/openapi.yaml` is the maintained machine-readable contract bootstrap until generated Spring OpenAPI is introduced; `scripts/dev/verify-openapi.ps1` checks critical wrapper markers against the smoke harness.

## Stack
- Backend: Spring Boot 3.3.5, Java 21, Spring Web/Validation/JDBC/Actuator, MySQL connector, Redis/RabbitMQ dependencies for runtime parity.
- Frontend: React 19, TypeScript, Vite, route dispatch in `frontend/src/App.tsx`, API adapters in `frontend/src/api`.
- Infrastructure: Docker Compose profiles for application services, MySQL seed/verification scripts, Nginx reverse proxy, optional observability stack.

## Runtime Boundaries
- Browser requests go through frontend routes and API adapters.
- API adapters call `/api/**` endpoints and retain mock fallback for unavailable local backend scenarios.
- Spring controllers expose auth/profile/dashboard/attendance/notifications/learning/quest/survey/community/support and board APIs.
- Repository layer reads seeded MySQL data where available; service layer returns safe demo fallback when data access fails.

## Domain Modules
- Auth/Profile: demo login, current user, password check, profile read/update.
- MyCampus: dashboard, attendance records, attendance appeals, notifications.
- Learning: curriculum, lecture replays, materials, resources.
- Quest/Survey: list/detail and submit/respond flows.
- Community/Board: board categories/posts/detail/write/comments/reactions, classmates.
- Help Desk: support ticket list/create and QnA board write flow.

## Current Architectural Gaps
- Authentication/session/RBAC is still demo-level rather than production-grade token/session enforcement.
- File upload/download attachment storage is not yet implemented end-to-end.
- Notification persistence/send pipeline is demo-level and needs durable recipient/read/delete semantics.
- Survey detail currently exposes aggregate question count rather than full question/option DTOs for production forms.
- Support ticket thread/answer/internal memo/status transitions are not complete.
- Browser E2E and CI automation remain minimal.
