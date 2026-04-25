# Worker 1 Scope/API/Owner Lock

Date: 2026-04-25
Team: `ssafy-full-clone-team-4-worker`
Owner: `worker-1`
Task: 1 - 기능 범위/완료조건/API 계약 확정, owner 배정

## 1. Round Goal

이번 TEAM 라운드는 `docs/final-verification.md`의 FAIL/PARTIAL blocker 중 코드로 닫을 수 있는 핵심 기능을 우선 폐쇄한다. 완료 선언이 아니라, 기능별 PASS 전환에 필요한 API/DB/Frontend/QA 증거를 남기는 것이 목표다.

## 2. Locked Feature Scope

| Area | Current blocker | Completion condition | Primary owner | Supporting owner |
|---|---|---|---|---|
| Common attachments | upload/storage/download 미완료 | 공통 업로드/다운로드 API, storage metadata, 도메인 link API, backend test/API docs 증거 | worker-1 | worker-2 |
| Support ticket depth | 답변/thread/status/첨부 workflow 미완료 | ticket detail/messages/answer/status endpoints, frontend workflow, attachment 연결 | worker-3 | worker-1 |
| Survey lifecycle | 문항/선택지/응답 검증 depth 부족 | survey detail questions/options, duplicate/required validation, response persistence test | worker-1 | worker-2 |
| Notification lifecycle | read/delete/send persistence 부족 | list/read/read-all/delete/send state transition API와 UI/smoke 증거 | worker-1 | worker-3 |
| RBAC breadth | domain-wide 401/403 부족 | role matrix, server guard tests, frontend unauthorized UX | worker-4 | worker-3 |
| Runtime/E2E | localhost 접근 편차 및 browser evidence 부족 | compose/nginx/backend health, browser smoke/E2E, CI evidence | worker-2 | worker-4 |
| Final docs | PASS/PARTIAL/FAIL 최신성 | final/remaining docs는 실제 command evidence로만 갱신 | worker-2 | worker-1 |

## 3. API Contract Lock

### 3.1 Common Attachment

Owner: `worker-1` for first implementation slice.

Required endpoints:

| Method | Path | Purpose |
|---|---|---|
| `POST` | `/api/attachments` | multipart upload and metadata creation |
| `GET` | `/api/attachments/{attachmentId}` | metadata lookup |
| `GET` | `/api/attachments/{attachmentId}/download` | binary download or deterministic local-file response |
| `POST` | `/api/learning/materials/{id}/attachments` | attach uploaded file to material |
| `POST` | `/api/boards/{boardCode}/posts/{postId}/attachments` | attach uploaded file to board post; existing metadata-only route must remain compatible |
| `POST` | `/api/support/tickets/{ticketId}/attachments` | attach uploaded file to support ticket |

Acceptance:
- Upload accepts a real multipart file and stores metadata in schema-backed tables or a documented local storage fallback.
- Download validates ownership/visibility where role guards exist; unsupported/unknown file returns the standard error envelope.
- Existing board attachment metadata clients do not break.
- REST Docs or generated static OpenAPI/docs are refreshed before moving on from an API-changing task.

### 3.2 Support Ticket Workflow

Owner: `worker-3`; `worker-1` only supplies attachment contract and PM guardrails.

Required endpoints:

| Method | Path | Purpose |
|---|---|---|
| `GET` | `/api/support/tickets/{ticketId}` | ticket detail with thread, status, attachments |
| `POST` | `/api/support/tickets/{ticketId}/messages` | learner/coach/admin thread message |
| `POST` | `/api/support/tickets/{ticketId}/answers` | official answer shortcut; may be represented as a message with answer role |
| `PATCH` | `/api/support/tickets/{ticketId}/status` | status transition such as open/in_progress/resolved/closed |

Acceptance:
- Status transitions persist and are reflected in list/detail DTOs.
- Message order and author role are deterministic in tests.
- Attachment link fields reuse the common attachment DTO.

### 3.3 Survey Lifecycle

Owner: `worker-1`.

Required contract:
- `GET /api/surveys/{id}` returns survey metadata plus ordered questions and options.
- `POST /api/surveys/{id}/responses` validates required questions, unknown question/option ids, and duplicate submit policy.
- Response payload remains frontend-compatible: answers are an array of question id plus selected option id or text value.

Acceptance:
- Backend tests cover valid submit, missing required answer, unknown option, and duplicate submit behavior.
- Frontend state shows submitted/closed/error states without local-only success.

### 3.4 Notification Lifecycle

Owner: `worker-1` for backend lifecycle, `worker-3` for frontend state.

Required endpoints:

| Method | Path | Purpose |
|---|---|---|
| `GET` | `/api/notifications` | existing list, includes read state |
| `PATCH` | `/api/notifications/{id}/read` | mark one notification read |
| `PATCH` | `/api/notifications/read-all` | mark all current-user notifications read |
| `DELETE` | `/api/notifications/{id}` | delete/dismiss one notification |
| `POST` | `/api/community/classmates/{userId}/notifications` | existing send flow, must persist into recipient list |

Acceptance:
- Read/delete/send transitions are persisted and covered by backend tests.
- Frontend does not fake unread count after API failure.

## 4. Verification Gates

For any API-changing task:
1. Backend focused tests pass under Java 21 Docker Maven path or equivalent.
2. REST Docs snippets/generated docs or static OpenAPI/API summary are refreshed and drift-checked.
3. Frontend typecheck/build runs when DTO/API client contracts changed.
4. `git diff --check` passes.
5. Final task result includes exact command evidence and remaining gaps.

## 5. Stop Conditions

Do not mark final clone complete while any of these remain true:
- `docs/final-verification.md` has FAIL/PARTIAL rows for attachments, support ticket workflow, survey/notification depth, RBAC, runtime, or E2E.
- API contract changed without docs/OpenAPI/REST Docs evidence.
- Runtime proof depends only on stale containers or historical logs.
- Browser/E2E evidence is still absent for the changed user workflow.
