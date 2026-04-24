# Architecture Summary

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
