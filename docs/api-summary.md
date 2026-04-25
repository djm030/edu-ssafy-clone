# API Summary

## Final Verification API Sync (2026-04-25)
- Controller 기준으로 다시 대조한 결과, Spring MVC controller surface는 **50 operations**이며 `docs/openapi.yaml` / `docs/openapi.json`에 모두 반영되어 있다.
- `docs/openapi.yaml`은 `Auth/Profile/Campus/Cohort/Class/Track/Attendance/Attendance Appeal/Notification/Curriculum/Lecture Replay/Learning Material/Quest/Survey/Board/Comment/Support Ticket/Attachment` 그룹을 포함한다.
- Spring Boot 프로젝트에는 아직 `springdoc-openapi` 자동 생성 의존성이 없으므로 Swagger UI와 `/v3/api-docs`는 실행 가능한 endpoint가 아니다. 현재 OpenAPI는 정적 controller-derived contract이며, 최종 PASS 전에는 자동 생성 또는 CI drift check가 필요하다.
- Material reaction은 더 이상 "미구현"이 아니다: `/api/learning/materials/{id}/reactions`가 controller/service/repository/frontend detail page에 연결되어 반응 상태와 카운트를 반환한다.

## Task 69 Recheck Snapshot (worker-4, 2026-04-25)
- API 표는 endpoint/method/request/response/auth/frontend-screen 컬럼을 유지하고 있다.
- 본 문서는 현재 구현된 컨트롤러 surface와 frontend 연결 화면 기준으로 유지되며, 미구현 depth는 `docs/remaining-work.md`와 최종 검증 문서에서 추적한다. 현재 OMX team state는 cleanup 이후 존재하지 않으므로 task id만으로 완료를 판정하지 않는다.


## Task 69 Endpoint Matrix (worker-4, 2026-04-24)

> Auth note: current implementation uses demo/current-user semantics. `POST /api/auth/login` is public; most other learner-facing APIs should be treated as auth-required for product completion, but production RBAC/session enforcement is still tracked as remaining work.

| API endpoint | HTTP method | Request parameters/body | Response summary | Auth needed | Frontend connected screen |
|---|---|---|---|---|---|
| `/api/auth/login` | POST | body `{ email, password }` | `{ user }` demo login profile | No | `/login` |
| `/api/me` | GET | none | `{ user }` current learner profile | Yes (demo) | app shell/session bootstrap, `/` |
| `/api/auth/roles/current` | GET | none | current role and permission summary | Yes (demo) | role/authorization future UI |
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
| `/api/learning/materials/{id}/reactions` | POST | path `id`, body `{ type }` (`like`/`bookmark`/`favorite`) | `{ item }` active state, per-type counts, current-user flags | Yes (demo) | `/learning/materials/:id` |
| `/api/quests` | GET | `page`, `size` optional | `{ items, page }` quest list | Yes (demo) | `/quest` |
| `/api/quests/{id}` | GET | path `id` | `{ item/quest }` quest detail | Yes (demo) | `/quest/:id`, `/quest/:id/submit` |
| `/api/quests/{id}/submissions` | POST | path `id`, body `{ content, attachmentUrl? }` | created submission `{ item }` | Yes (demo) | `/quest/:id/submit` |
| `/api/surveys` | GET | `page`, `size` optional | `{ items, page }` survey list | Yes (demo) | `/survey` |
| `/api/surveys/{id}` | GET | path `id` | `{ item/survey }` survey detail | Yes (demo) | `/survey/:id`, `/survey/:id/respond` |
| `/api/surveys/{id}/responses` | POST | path `id`, body `{ answers: [{ questionId, answerText, optionIds }] }` | created survey response `{ item }` | Yes (demo) | `/survey/:id/respond` |
| `/api/boards/{boardCode}/categories` | GET | path `boardCode` | `{ items }` board categories | Yes (demo) | `/community/free`, `/help/notice`, `/help/faq`, `/help/qna` |
| `/api/boards/{boardCode}/posts` | GET | path `boardCode`; `categoryId`, `keyword`, `page`, `size`, `sort` optional | `{ items, page }` board posts | Yes (demo) | board list screens |
| `/api/boards/{boardCode}/posts/{postId}` | GET | path `boardCode`, `postId` | `{ post }` detail wrapper | Yes (demo) | board detail screens |
| `/api/boards/{boardCode}/posts` | POST | path `boardCode`, body post draft | created `{ item }` | Yes (demo) | `/community/free/write`, `/help/qna/new` |
| `/api/boards/{boardCode}/posts/{postId}` | PUT | path `boardCode`, `postId`, header `X-User-Role: admin`, body post draft | updated `{ item }` | Admin (demo) | board detail/admin action |
| `/api/boards/{boardCode}/posts/{postId}` | DELETE | path `boardCode`, `postId`, header `X-User-Role: admin` | `204 No Content` | Admin (demo) | board detail/admin action |
| `/api/boards/{boardCode}/posts/{postId}/comments` | POST | path `boardCode`, `postId`, body `{ content }` | created comment `{ item }` | Yes (demo) | board detail screens |
| `/api/boards/{boardCode}/posts/{postId}/reactions` | POST | path `boardCode`, `postId`, body reaction type | created reaction `{ item }` | Yes (demo) | board detail screens |
| `/api/boards/{boardCode}/posts/{postId}/attachments` | POST | path `boardCode`, `postId`, header `X-User-Role: admin`, body attachment metadata | created attachment metadata `{ item }` | Admin (demo) | board detail/admin action |
| `/api/support/tickets` | GET | `page`, `size`, status filters optional | `{ items, page }` support tickets | Yes (demo) | `/help/qna` |
| `/api/support/tickets` | POST | body `{ title, content, category }` | created support ticket `{ item }` | Yes (demo) | `/help/qna/new` |
| `/api/admin/campus-structure` | GET | header `X-User-Role: admin` | campus/cohort/track/class lists | Admin (demo) | `/admin/campus` |
| `/api/admin/campus-structure/campuses` | POST | header `X-User-Role: admin`, body `{ name }` | created campus `{ item }` | Admin (demo) | `/admin/campus` |
| `/api/admin/campus-structure/campuses/{campusId}` | PUT/DELETE | header `X-User-Role: admin`, path `campusId` | updated `{ item }` or `204` | Admin (demo) | `/admin/campus` |
| `/api/admin/campus-structure/cohorts` | POST | header `X-User-Role: admin`, body `{ name, year }` | created cohort `{ item }` | Admin (demo) | `/admin/campus` |
| `/api/admin/campus-structure/cohorts/{cohortId}` | PUT/DELETE | header `X-User-Role: admin`, path `cohortId` | updated `{ item }` or `204` | Admin (demo) | `/admin/campus` |
| `/api/admin/campus-structure/tracks` | POST | header `X-User-Role: admin`, body `{ name, description? }` | created track `{ item }` | Admin (demo) | `/admin/campus` |
| `/api/admin/campus-structure/tracks/{trackId}` | PUT/DELETE | header `X-User-Role: admin`, path `trackId` | updated `{ item }` or `204` | Admin (demo) | `/admin/campus` |
| `/api/admin/campus-structure/classes` | POST | header `X-User-Role: admin`, body `{ campusId, cohortId, trackId, name, classroom?, capacity? }` | created class `{ item }` | Admin (demo) | `/admin/campus` |
| `/api/admin/campus-structure/classes/{classId}` | PUT/DELETE | header `X-User-Role: admin`, path `classId` | updated `{ item }` or `204` | Admin (demo) | `/admin/campus` |
| `/api/health` | GET | none | `{ status: "UP" }` | No | smoke/ops only |
| `/actuator/health` | GET | none | Spring actuator health | No | smoke/ops only |

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
- `POST /api/auth/login`, `GET /api/me`, `GET /api/auth/roles/current`, `POST /api/auth/logout`
- `POST /api/profile/password-check`, `GET /api/profile`, `PUT /api/profile`
- `GET /api/dashboard/summary`
- `GET /api/attendance/records`, `POST /api/attendance/appeals`
- `GET /api/notifications`
- `GET /api/learning/curriculum`, `GET /api/learning/replays`
- `GET /api/learning/materials`, `GET /api/learning/materials/{id}`, `GET /api/learning/materials/{id}/resources`, `POST /api/learning/materials/{id}/reactions`
- `GET /api/quests`, `GET /api/quests/{id}`, `POST /api/quests/{id}/submissions`
- `GET /api/surveys`, `GET /api/surveys/{id}`, `POST /api/surveys/{id}/responses`
- `GET /api/boards/{boardCode}/categories`, `GET /api/boards/{boardCode}/posts`, `GET /api/boards/{boardCode}/posts/{postId}`
- `POST /api/boards/{boardCode}/posts`, `PUT /api/boards/{boardCode}/posts/{postId}`, `DELETE /api/boards/{boardCode}/posts/{postId}`
- `POST /api/boards/{boardCode}/posts/{postId}/comments`, `POST /api/boards/{boardCode}/posts/{postId}/reactions`, `POST /api/boards/{boardCode}/posts/{postId}/attachments`
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

- `GET /api/admin/campus-structure`, `POST /api/admin/campus-structure/campuses|cohorts|tracks|classes`, `PUT /api/admin/campus-structure/campuses|cohorts|tracks|classes/{id}`, `DELETE /api/admin/campus-structure/campuses|cohorts|tracks|classes/{id}` (admin-only persisted campus/cohort/track/class CRUD flow)

## Still Needed For Full Clone
- Password recovery/session expiry endpoints.
- Durable notification mark-read/delete/send persistence and recipient targeting.
- Attachment upload/download APIs for materials, boards, tickets, and submissions.
- Full survey question/option detail and persisted responses.
- Support ticket thread messages, answers, status transitions, internal memo/admin response.
- RBAC-protected endpoints for learner/operator/admin roles.
- Executable Springdoc/Swagger UI and `/v3/api-docs`; static `docs/openapi.yaml`/`docs/openapi.json` is maintained, but runtime generation is not configured.
