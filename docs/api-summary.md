# API Summary

## R7.0 Contract Notes
- Board detail contract is canonical as `GET /api/boards/{boardCode}/posts/{postId}` -> `{ post: { id, boardCode, title, content, engagement, ... } }`.
- Board create contract is canonical as `POST /api/boards/{boardCode}/posts` -> HTTP 201 with `{ item: { id, boardCode, title, content, demo, ... } }`.
- Frontend free/QNA creation adapters unwrap `{ item }`; board detail adapter unwraps `{ post }`.
- Error/fallback policy: `401 Unauthorized` and `403 Forbidden` must propagate to UI state and must not be hidden by mock fallback. Production/live, CI, and `VITE_DISABLE_API_FALLBACK=true` disable all frontend fallback.
- API contract artifact: `docs/openapi.yaml`; drift marker command: `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/dev/verify-openapi.ps1`.

## Implemented API Surface
- `POST /api/auth/login`, `GET /api/me`
- `POST /api/profile/password-check`, `GET /api/profile`, `PUT /api/profile`
- `GET /api/dashboard/summary`
- `GET /api/attendance/records`, `POST /api/attendance/appeals`
- `GET /api/notifications`
- `GET /api/learning/curriculum`, `GET /api/learning/replays`
- `GET /api/learning/materials`, `GET /api/learning/materials/{id}`, `GET /api/learning/materials/{id}/resources`
- `GET /api/quests`, `GET /api/quests/{id}`, `POST /api/quests/{id}/submissions`
- `GET /api/surveys`, `GET /api/surveys/{id}`, `POST /api/surveys/{id}/responses`
- `GET /api/boards/{boardCode}/categories`, `GET /api/boards/{boardCode}/posts`, `GET /api/boards/{boardCode}/posts/{postId}`
- `POST /api/boards/{boardCode}/posts`, `POST /api/boards/{boardCode}/posts/{postId}/comments`, `POST /api/boards/{boardCode}/posts/{postId}/reactions`
- `GET /api/support/tickets`, `POST /api/support/tickets`
- `GET /api/community/classmates`
- `POST /api/community/classmates/{userId}/notifications` (R6 source implemented; live smoke requires rebuilt backend image)
- `GET /api/health`, actuator health endpoints

## R6 Contract Closure Completed
- Added backend classmate notification DTO/controller/service demo response.
- Normalized frontend submit/update adapters around backend response wrappers:
  - `{ item }` for create/submit responses.
  - `{ profile }` for profile responses.
  - `{ items, page }` for paginated lists.
- Aligned frontend request payloads to backend DTOs:
  - Attendance appeal: `{ type, reason, requestedStatus }`.
  - Quest submission: `{ content, attachmentUrl? }`.
  - Survey response: `{ answers: [{ questionId, answerText, optionIds }] }`.
- Added adapter mapping for backend read DTOs that differ from UI models:
  - notifications: `body` -> `message`, category defaulting.
  - attendance: `checkInAt/checkOutAt` -> `checkIn/checkOut`.
  - curriculum: `weekNo/classDate/startTime/endTime` -> week/period/lessons/status.
  - replays: `publishedAt/versionNo` -> date/category.
  - materials: `summary/detailUrl/resources` -> description/fileName and UI material type.

## Still Needed For Full Clone
- Password recovery/session expiry endpoints.
- Durable notification mark-read/delete/send persistence and recipient targeting.
- Material like/bookmark/favorite reactions.
- Attachment upload/download APIs for materials, boards, tickets, and submissions.
- Full survey question/option detail and persisted responses.
- Support ticket thread messages, answers, status transitions, internal memo/admin response.
- RBAC-protected endpoints for learner/operator/admin roles.
- OpenAPI/Swagger generation or maintained machine-readable API spec.
