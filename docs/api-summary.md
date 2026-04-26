# API Summary

## Final Verification API Delta (2026-04-26 KST)
- Auth/session endpoints are verified as session-backed with hashed password login: `POST /api/auth/login`, `GET /api/me`, `GET /api/auth/session`, `POST /api/auth/logout`, plus `GET /api/auth/access-policy` for the staff/admin API policy matrix.
- Attendance appeals now include list, pending staff list, create, cancel, and resolve endpoints.
- Notifications include list, read one, read all, delete, plus classmate notification send.
- Learning materials include list/detail/resources/view-count, staff resource attachment upload/download, and like/bookmark reaction create/delete.
- Quests include list/detail/current submission/submission upsert plus owner-scoped submission attachment upload/download.
- Surveys include list/detail/create/current response/response submit; edit/delete admin CRUD remains future work.
- Board endpoints include post create/update/delete, attachment metadata create/delete, comment create/update/delete, and reaction create/delete.
- Support endpoints include ticket list/detail/create, user message create, staff answer create, support attachment metadata create, and attachment byte download.
- Error responses keep `{ error: { code, message } }` compatibility and now add `status`, `path`, `requestId`, and `timestamp`; `X-Request-Id` is echoed/generated for support correlation.
- Remaining API gaps before final service readiness: mutation E2E coverage, browser-level role-flow checks, and broader Spring REST Docs/OpenAPI coverage beyond the documented auth/health/survey endpoints.


## Task 69 Endpoint Matrix (worker-4, 2026-04-24)

> Auth note: current implementation uses demo/current-user semantics. `POST /api/auth/login` is public; most other learner-facing APIs should be treated as auth-required for product completion, but production RBAC/session enforcement is still tracked as remaining work.

| API endpoint | HTTP method | Request parameters/body | Response summary | Auth needed | Frontend connected screen |
|---|---|---|---|---|---|
| `/api/auth/login` | POST | body `{ email, password }` | `{ user }` demo login profile | No | `/login` |
| `/api/me` | GET | none | `{ user }` current learner profile | Yes (demo) | app shell/session bootstrap, `/` |
| `/api/auth/roles/current` | GET | none | current role and permission summary | Yes (demo) | app shell authorization state |
| `/api/auth/access-policy` | GET | none | protected API policy matrix `{ items[] }` with method/path/allowed roles | Yes (demo) | `/ops/readiness` access policy matrix |
| `/api/auth/logout` | POST | none | `{ success, message }` | Yes (demo) | app shell/logout action |
| `/api/profile/password-check` | POST | body `{ password }` | `{ valid }` | Yes (demo) | `/profile/check` |
| `/api/profile` | GET | none | `{ profile }` | Yes (demo) | `/profile/edit` |
| `/api/profile` | PUT | body profile draft fields | `{ profile }` updated profile wrapper | Yes (demo) | `/profile/edit` |
| `/api/dashboard/summary` | GET | none | dashboard summary/cards for learner | Yes (demo) | `/` |
| `/api/attendance/records` | GET | optional page/filter params in adapter | `{ summary, items }` attendance records | Yes (demo) | `/mycampus/attendance` |
| `/api/attendance/appeals` | POST | body `{ type, reason, requestedStatus }` | created appeal `{ item/status }` | Yes (demo) | `/mycampus/attendance/appeals/new` |
| `/api/notifications` | GET | `page`, `size` optional | `{ items, page }` notifications | Yes (demo) | `/mycampus/notifications` |
| `/api/community/classmates` | GET | none | `{ items }` classmates | Yes (demo) | `/community/classmates` |
| `/api/community/classmates/{userId}/notifications` | POST | path `userId`, body `{ message }` | created classmate notification `{ item }` | Yes (demo) | `/community/classmates` |
| `/api/learning/curriculum` | GET | none | `{ items }` curriculum weeks | Yes (demo) | `/learning/curriculum` |
| `/api/learning/replays` | GET | `keyword` optional | `{ items }` replay lectures | Yes (demo) | `/learning/replays` |
| `/api/learning/materials` | GET | `keyword`, `type`, `page`, `size` optional | `{ items, page }` materials | Yes (demo) | `/learning/materials` |
| `/api/learning/materials/{id}` | GET | path `id` | `{ item/material }` material detail | Yes (demo) | `/learning/materials/:id` |
| `/api/learning/materials/{id}/resources` | GET | path `id` | `{ items }` material resources | Yes (demo) | `/learning/materials/:id`, `/learning/materials/:id/viewer` |
| `/api/learning/materials/{id}/resources/{resourceId}/attachments` | POST | staff path ids, body `{ filename, mimeType?, contentBase64 }` | created attachment `{ item, resource }` | Coach/Admin | frontend API client |
| `/api/learning/materials/{id}/resources/{resourceId}/attachments/{attachmentId}` | GET | path ids | attachment bytes with content disposition | Yes (demo) | frontend API client/download URL helper |
| `/api/quests` | GET | `page`, `size` optional | `{ items, page }` quest list | Yes (demo) | `/quest` |
| `/api/quests/{id}` | GET | path `id` | `{ item/quest }` quest detail | Yes (demo) | `/quest/:id`, `/quest/:id/submit` |
| `/api/quests/{id}/submissions` | POST | path `id`, body `{ content, attachmentUrl? }` | created submission `{ item }` | Yes (demo) | `/quest/:id/submit` |
| `/api/quests/{id}/submissions/{submissionId}/attachments` | POST | owner path ids, body `{ filename, mimeType?, contentBase64 }` | created attachment `{ item, submission }` | Yes (demo) | `/quest/:id/submit` |
| `/api/quests/{id}/submissions/{submissionId}/attachments/{attachmentId}` | GET | owner path ids | attachment bytes with content disposition | Yes (demo) | frontend API helper |
| `/api/surveys` | GET | `page`, `size` optional | `{ items, page }` survey list | Yes (demo) | `/survey` |
| `/api/surveys` | POST | body `{ title, category, required, status, startAt, endAt, questions[] }` | created survey `{ item }` | Coach/Admin | `/survey` manager form |
| `/api/surveys/{id}` | GET | path `id` | `{ item/survey }` survey detail | Yes (demo) | `/survey/:id`, `/survey/:id/respond` |
| `/api/surveys/{id}` | PUT | staff path `id`, body survey metadata/questions | updated survey `{ item }`; replaces questions and resets stale responses | Coach/Admin | `/survey` manager edit action |
| `/api/surveys/{id}` | DELETE | staff path `id` | deleted marker `{ item }` | Coach/Admin | `/survey` manager delete action |
| `/api/surveys/{id}/responses` | POST | path `id`, body `{ answers: [{ questionId, answerText, optionIds }] }` | created survey response `{ item }` | Yes (demo) | `/survey/:id/respond` |
| `/api/boards/{boardCode}/categories` | GET | path `boardCode` | `{ items }` board categories | Yes (demo) | `/community/free`, `/help/notice`, `/help/faq`, `/help/qna` |
| `/api/boards/{boardCode}/posts` | GET | path `boardCode`; `categoryId`, `keyword`, `page`, `size`, `sort` optional | `{ items, page }` board posts | Yes (demo) | board list screens |
| `/api/boards/{boardCode}/posts/{postId}` | GET | path `boardCode`, `postId` | `{ post }` detail wrapper | Yes (demo) | board detail screens |
| `/api/boards/{boardCode}/posts` | POST | path `boardCode`, body post draft | created `{ item }` | Yes (demo) | `/community/free/write`, `/help/qna/new` |
| `/api/boards/{boardCode}/posts/{postId}/comments` | POST | path `boardCode`, `postId`, body `{ content }` | created comment `{ item }` | Yes (demo) | board detail screens |
| `/api/boards/{boardCode}/posts/{postId}/attachments` | POST | path `boardCode`, `postId`, body filename/metadata plus optional `contentBase64` | created attachment `{ item }` and stores bytes when content is supplied | Yes (demo metadata fallback) | board detail attachment manager |
| `/api/boards/{boardCode}/posts/{postId}/attachments/{attachmentId}` | GET | path `boardCode`, `postId`, `attachmentId` | attachment bytes with `Content-Disposition` | No | board detail download link |
| `/api/boards/{boardCode}/posts/{postId}/reactions` | POST | path `boardCode`, `postId`, body reaction type | created reaction `{ item }` | Yes (demo) | board detail screens |
| `/api/support/tickets` | GET | `page`, `size`, status filters optional | `{ items, page }` support tickets | Yes (demo) | `/help/qna` |
| `/api/support/tickets` | POST | body `{ title, content, category }` | created support ticket `{ item }` | Yes (demo) | `/help/qna/new` |
| `/api/health` | GET | none | `{ status, checkedAt, service, profile, checks[] }` including DB/temp-storage readiness | No | smoke/ops only |
| `/api/readiness` | GET | none | same readiness payload as `/api/health`; HTTP 503 when required checks are down | No | `/ops/readiness` |
| `/actuator/health` | GET | none | Spring actuator health | No | smoke/ops only |

## Spring REST Docs Coverage (2026-04-26 KST)
- `health-check`: documents `GET /api/health` overall status plus database and temporary storage readiness probes for production smoke checks.
- `readiness-check`: documents `GET /api/readiness` with the same probe payload and HTTP 503 semantics for failed required checks.
- `auth-access-policy`: documents `GET /api/auth/access-policy` staff/admin API policy matrix fields consumed by `/ops/readiness`.
- `survey-create`: documents `POST /api/surveys` request fields for survey metadata, first question, and choice options plus the persisted `{ item }` response shape.
- `survey-update`: documents `PUT /api/surveys/{id}` replacement semantics, including response reset when questions are replaced.
- `survey-delete`: documents `DELETE /api/surveys/{id}` deleted marker response.

## Task 66 API Documentation Status (2026-04-24)
- Endpoint inventory is current for the implemented Spring controller surface: auth/profile/dashboard/attendance/notifications/learning/quest/survey/board/support/community/health.
- Most learner-facing endpoints currently use demo-session semantics; final auth requirement is **not complete** until RBAC/session/token enforcement is implemented and documented per endpoint.
- Frontend connection screens are mapped in `frontend/src/App.tsx` and page components; high-level coverage is: dashboard `/`, attendance `/mycampus/attendance`, learning `/learning/**`, quest `/quest/**`, survey `/survey/**`, board/community `/community/**`, help/QNA `/help/**`, and profile `/profile/**`.
- Keep `docs/openapi.yaml` and `scripts/dev/verify-openapi.ps1` aligned with this summary whenever request/response wrappers change.


## R7.0 Contract Guardrails Added (2026-04-24)
- Smoke now treats the following wrappers as required contract shapes:
  - `POST /api/auth/login` -> `{ user: { id, email, name, role } }`.
  - `GET /api/me` -> `{ user: { id, email, name, campusName, cohortName, trackName } }`.
  - `GET /api/profile` -> `{ profile: { id, email, name, campusName, cohortName, trackName } }`.
  - `GET /api/boards/{boardCode}/posts` -> `{ items, page: { page, size, totalItems, totalPages } }`.
  - `GET /api/boards/{boardCode}/posts/{postId}` -> `{ post: { id, boardCode, title, content, engagement } }`.
  - `POST /api/boards/{boardCode}/posts` -> `{ item: { id, boardCode, title, content, authorName, createdAt } }`.
- These assertions are an R7.0 prerequisite for Auth/RBAC work because later auth failures must not be hidden by weak smoke checks or frontend fallback behavior.
- Added maintained contract bootstrap `docs/openapi.yaml` and drift-marker command `scripts/dev/verify-openapi.ps1`; keep both updated as endpoints/wrappers change.

## Implemented API Surface
- `POST /api/auth/login`, `GET /api/me`, `GET /api/auth/roles/current`, `GET /api/auth/access-policy`, `POST /api/auth/logout`
- `POST /api/profile/password-check`, `GET /api/profile`, `PUT /api/profile`
- `GET /api/dashboard/summary`
- `GET /api/attendance/records`, `POST /api/attendance/appeals`
- `GET /api/notifications`
- `GET /api/learning/curriculum`, `GET /api/learning/replays`
- `GET /api/learning/materials`, `GET /api/learning/materials/{id}`, `GET /api/learning/materials/{id}/resources`
- `POST /api/learning/materials/{id}/resources/{resourceId}/attachments`, `GET /api/learning/materials/{id}/resources/{resourceId}/attachments/{attachmentId}`
- `GET /api/quests`, `GET /api/quests/{id}`, `POST /api/quests/{id}/submissions`
- `POST /api/quests/{id}/submissions/{submissionId}/attachments`, `GET /api/quests/{id}/submissions/{submissionId}/attachments/{attachmentId}`
- `GET /api/surveys`, `POST /api/surveys`, `GET/PUT/DELETE /api/surveys/{id}`, `POST /api/surveys/{id}/responses`
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

- `GET /api/admin/campus-structure`, `POST /api/admin/campus-structure/classes` (demo admin campus/cohort/track/class management flow)

## Still Needed For Full Clone
- Password recovery/session expiry endpoints.
- Durable notification mark-read/delete/send persistence and recipient targeting.
- Material like/bookmark/favorite reactions.
- Attachment byte upload/download APIs now exist for board post attachments, material resource attachments, support ticket message attachments, and quest submission attachments; remaining gaps are broader E2E/role-matrix documentation and production image rebuild verification.
- Full survey question/option detail and persisted responses.
- Support ticket thread messages, answers, status transitions, internal memo/admin response.
- RBAC-protected endpoints for learner/operator/admin roles.
- OpenAPI/Swagger generation or maintained machine-readable API spec.
