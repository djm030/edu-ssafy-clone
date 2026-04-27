# Architecture Summary

## Current Architecture Snapshot (2026-04-27)
- The repository is a layered full-stack SSAFY clone: React/Vite frontend, Spring Boot API backend, MySQL-backed repository layer, Docker Compose runtime, Nginx reverse proxy, and smoke/dev verification scripts.
- The implemented local full-stack clone surface is verified as PASS in `docs/final-verification.md`.
- Remaining work is production hardening and UI/UX parity: browser visual baseline automation, repeated deployment evidence, and the active backlog in `docs/next_plan.md`.
- Required documentation entry points are `README.md` for operator runbook, `docs/api-summary.md` for endpoint inventory, `docs/test-report.md` for verification evidence, `docs/remaining-work.md` for follow-ups, `docs/next_plan.md` for active parity backlog, and `docs/final-verification.md` for readiness status.


## Repository Layout
- Source layout: backend Java sources are split between `backend/src/main/java/com/edussafy/backend/board/**` for board/category/post APIs and `backend/src/main/java/com/edussafy/backend/priority/**` for auth/profile/dashboard/attendance/learning/quest/survey/community/support APIs.
- Backend tests currently cover board controllers plus priority controller/service paths under `backend/src/test/java/com/edussafy/backend/**`; Maven execution still depends on a host with Maven or Docker access.
- Frontend layout: `frontend/src/App.tsx` owns path dispatch, `frontend/src/api/client.ts` owns fetch/error/fallback policy, `frontend/src/api/app.ts` and `frontend/src/api/boards.ts` own backend DTO normalization, and feature screens live under `frontend/src/pages/**`.
- Dev/runtime layout: `compose.yml` orchestrates the application profile, `infra/nginx/conf.d/default.conf` fronts browser/API traffic, `scripts/mysql/**` seeds/verifies MySQL, and `scripts/dev/**` provides localhost, smoke, compose, Git, Docker, and OpenAPI verification helpers.
- Analysis outcome: the repository is a runnable full-stack clone surface with verified local Docker Compose execution. The highest-risk remaining items are browser visual baseline automation, repeated deployment evidence, and UI/UX parity work tracked in `docs/next_plan.md`.

## Smoke Contract Boundary
- The smoke harness has explicit JSON contract assertions for auth/profile/board paths. This makes the smoke layer a contract boundary rather than a simple availability check.
- Critical wrappers are enforced in live smoke: auth/current-user use `{ user }`, profile uses `{ profile }`, board detail uses `{ post }`, board create uses `{ item }`, and board list requires `{ items, page }`.
- This is intentionally separate from frontend fallback behavior: CI/live failures and 401/403 responses must not be masked by local demo fallbacks.
- Runtime OpenAPI is generated at `/v3/api-docs`; `docs/openapi.json` is the committed snapshot used for offline review.

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
- Auth/Profile: session login, current user, password check/change, profile read/update.
- MyCampus: dashboard, attendance records, attendance appeals, notifications.
- Learning: curriculum, lecture replays, materials, resources.
- Quest/Survey: list/detail and submit/respond flows.
- Community/Board: board categories/posts/detail/write/comments/reactions, classmates.
- Help Desk: support ticket list/create and QnA board write flow.

## Current Architectural Gaps
- Browser visual baseline automation is still a production follow-up.
- Repeated deployment evidence is still environment-specific and must be collected per target environment.
- `docs/next_plan.md` tracks current UI/UX parity gaps against the EduSSAFY service shape.
