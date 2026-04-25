1) **이번 TEAM 실행 목표**

- 이번 라운드 목표는 **“FAIL 3개(첨부파일/문의 답변/문의 첨부)를 PARTIAL 이상으로 끌어올리고, 핵심 PARTIAL(인증·RBAC/설문·퀘스트/알림/E2E)을 닫을 수 있는 실행 단위로 분해해 4인 병렬 개발이 가능하게 만드는 것”**입니다.
- 기준 근거: `docs/final-verification.md`(2026-04-25), `docs/remaining-work.md`(2026-04-25), 현재 controller/service/test 구조.

---

2) **4인 역할별 작업 배정**

- **PM**
  - P-01 (API 계약 고정 + 범위 잠금 + DoD/검증 명령 고정)
- **Backend**
  - B-01 (공통 첨부파일 시스템)
  - B-02 (1:1 문의 답변/스레드/상태/첨부)
  - B-03 (인증·인가·RBAC/401·403 서버 강제)
  - B-04 (설문/퀘스트 제출 영속화·검증)
  - B-05 (알림 read/delete/lifecycle)
- **Frontend**
  - F-01 (첨부 업·다운로드 UI + 도메인 연결)
  - F-02 (문의 상세/답변/상태 전이 UI)
  - F-03 (인증 상태/권한 UX + 설문/퀘스트/알림 액션 UX)
- **DevOps/QA**
  - Q-01 (브라우저 E2E smoke + CI + REST Docs 노출 검증 + 최소 문서 동기화)

---

3) **우선순위 task 목록**

Task ID: P-01  
Title: Attachment/Support/Auth API 계약 잠금 및 라운드 DoD 고정  
Priority: P0  
Owner: PM  
Feature Area: Cross-cutting scope control  
Why this matters: 구현 착수 전 API/권한/완료조건을 고정해야 병렬 충돌을 줄임  
Dependencies: 없음  
Expected files: `docs/remaining-work.md`, `docs/progress.md`(최소 갱신), `docs/collaboration/*`  
Backend scope: endpoint/role matrix/에러코드(401/403/404/409) 합의  
Frontend scope: 요청/응답 shape, optimistic/fallback 금지 구간 고정  
Spring REST Docs scope: 이번 라운드 신규 snippet 목록 확정(아래 4번)  
Verification commands: `git diff -- docs/remaining-work.md docs/progress.md`; 합의 endpoint 체크리스트  
Completion condition: Backend/Frontend/QA가 같은 계약으로 구현 가능한 상태  
Risks: 계약 누락 시 병렬 구현 재작업  
Suggested commit message: Lock round contract to prevent false completion and parallel drift

---

Task ID: B-01  
Title: 공통 첨부파일 업로드/다운로드/권한검사 백엔드 구현  
Priority: P1  
Owner: Backend  
Feature Area: Attachment system  
Why this matters: 현재 FAIL(첨부파일, 문의 첨부)의 공통 원인  
Dependencies: P-01  
Expected files: `backend/src/main/java/**/attachment/*`, `backend/src/main/java/**/api/*`, `backend/src/main/java/**/repository/*`, `docs/revised_schema_mysql8.sql`  
Backend scope: multipart(or presigned 정책) 업로드, 다운로드, 도메인 연결(material/board/support/quest), 권한검사  
Frontend scope: 없음(별도 F-01)  
Spring REST Docs scope: `attachments-upload`, `attachments-download`, `attachments-link-domain` snippet  
Verification commands:  
- `bash scripts/dev/backend-test.sh --skip-cache`  
- `docker run --rm -v "$PWD/backend:/workspace" -w /workspace maven:3.9.9-eclipse-temurin-21 mvn -B test prepare-package`  
Completion condition: 파일 바이너리 경로 + 메타 연결 + 권한 실패(403)까지 테스트 통과  
Risks: 저장소/스토리지 정책 미확정 시 재작업  
Suggested commit message: Enable shared attachment flow to unblock multi-domain feature closure

---

Task ID: B-02  
Title: 1:1 문의 스레드/답변/상태전이/첨부 API 구현  
Priority: P2  
Owner: Backend  
Feature Area: Support ticket lifecycle  
Why this matters: 문의 답변/첨부 FAIL 직접 해소  
Dependencies: P-01, B-01  
Expected files: `backend/src/main/java/**/priority/api/SupportController.java`, `.../service/PriorityApiService.java`, `.../repository/PriorityP2Repository.java`, 관련 DTO/test  
Backend scope: ticket detail, message thread, answer(create/update), status transition(open/in_progress/closed), attachment link  
Frontend scope: 없음(별도 F-02)  
Spring REST Docs scope: `support-ticket-detail`, `support-ticket-message-create`, `support-ticket-answer`, `support-ticket-status-update`  
Verification commands: `bash scripts/dev/backend-test.sh --skip-cache`; Support controller/service 테스트  
Completion condition: 문의 생성→대화→답변→상태전이→첨부까지 API 시나리오 재현 가능  
Risks: 상태머신 규칙 미정의 시 프론트/백 mismatch  
Suggested commit message: Close support workflow gaps with thread, answer, status, and attachment APIs

---

Task ID: B-03  
Title: 인증/세션/RBAC 서버 강제 및 401/403 명확화  
Priority: P3  
Owner: Backend  
Feature Area: Authz/Authn  
Why this matters: “동작처럼 보이는 데모”를 실동작으로 전환하는 핵심  
Dependencies: P-01  
Expected files: `backend/src/main/java/**/priority/security/RoleAccessInterceptor.java`, 관련 auth service/controller/test  
Backend scope: 경로별 role matrix 확장, unauthorized/forbidden 일관 응답, logout/session 정책 강화  
Frontend scope: 없음(별도 F-03)  
Spring REST Docs scope: `auth-roles-current`, `auth-logout`, `error-401`, `error-403`  
Verification commands: `bash scripts/dev/backend-test.sh --skip-cache`; RBAC/401/403 MockMvc 테스트  
Completion condition: 보호 endpoint에서 role별 허용/차단이 테스트로 증명됨  
Risks: 기존 fallback UX와 충돌 가능  
Suggested commit message: Enforce server-side RBAC to remove demo-only authorization behavior

---

Task ID: B-04  
Title: 설문/퀘스트 제출 영속화 및 중복/정합성 검증  
Priority: P4  
Owner: Backend  
Feature Area: Survey & Quest submission  
Why this matters: 제출 API가 현재 성공 응답 중심이라 완결성이 낮음  
Dependencies: P-01, B-01(퀘스트 첨부 연계 시)  
Expected files: `PriorityApiService/PriorityP3Repository/QuestSurveyController` 및 테스트  
Backend scope: submit 시 DB 반영, 중복 제출 정책, answer validation, submission status/result 연결  
Frontend scope: 없음(별도 F-03)  
Spring REST Docs scope: `survey-response-submit`, `quest-submission-create`  
Verification commands: `bash scripts/dev/backend-test.sh --skip-cache`; submission 시나리오 테스트  
Completion condition: list/detail/submit 결과가 DB 상태와 일치  
Risks: 기존 mock 데이터와 충돌  
Suggested commit message: Persist quest and survey submissions for real end-to-end workflow closure

---

Task ID: B-05  
Title: 알림 read/delete/send lifecycle API 완성  
Priority: P5  
Owner: Backend  
Feature Area: Notifications  
Why this matters: 현재 목록 조회 중심이라 사용자 lifecycle 미완결  
Dependencies: P-01  
Expected files: Notification controller/service/repository/test  
Backend scope: mark-read, bulk-read, delete(soft/hard 정책), send 검증  
Frontend scope: 없음(별도 F-03)  
Spring REST Docs scope: `notifications-list`, `notifications-mark-read`, `notifications-delete`  
Verification commands: `bash scripts/dev/backend-test.sh --skip-cache`  
Completion condition: 읽음/삭제 후 목록/카운트 정합성 유지  
Risks: unread count 동기화 이슈  
Suggested commit message: Complete notification lifecycle to move beyond list-only implementation

---

Task ID: F-01  
Title: 첨부파일 UI/클라이언트 연결(material/board/support/quest)  
Priority: P6  
Owner: Frontend  
Feature Area: Attachment UX  
Why this matters: 백엔드 첨부 API가 있어도 화면에서 닫히지 않으면 미완료  
Dependencies: B-01  
Expected files: `frontend/src/api/app.ts`, `frontend/src/pages/*Detail*`, `*Write*`, `*Submit*`, `types.ts`  
Backend scope: 없음  
Frontend scope: 업로드/다운로드 버튼, 진행/실패 표시, 권한/확장자/용량 오류 처리  
Spring REST Docs scope: N/A(소비자), 단 endpoint명과 request shape 동기화 체크  
Verification commands: `npm --prefix frontend run lint && npm --prefix frontend run build`  
Completion condition: 4개 도메인 화면에서 첨부 업/다운로드 동작 및 오류 UX 확인  
Risks: API shape 변경 시 build green인데 런타임 실패 가능  
Suggested commit message: Wire attachment UX across domains to realize backend file lifecycle

---

Task ID: F-02  
Title: 문의 상세/대화/답변/상태 전이 화면 구현  
Priority: P7  
Owner: Frontend  
Feature Area: Support UI  
Why this matters: 문의 기능을 “등록만 가능” 상태에서 실제 운영 흐름으로 전환  
Dependencies: B-02  
Expected files: `frontend/src/pages/Qna*.tsx`(신규 detail/list 확장), `frontend/src/api/app.ts`, `types.ts`  
Backend scope: 없음  
Frontend scope: ticket detail, thread UI, answer 작성(권한별), status 변경  
Spring REST Docs scope: N/A(소비자), docs snippet 기반 요청/응답 필드 매핑 체크  
Verification commands: `npm --prefix frontend run lint && npm --prefix frontend run build`  
Completion condition: 문의 등록→조회→대화→답변→상태변경 UI 루프 완성  
Risks: role별 UI 분기 누락  
Suggested commit message: Close support ticket UX loop with threaded conversation and status transitions

---

Task ID: F-03  
Title: 권한/에러 UX + 설문/퀘스트/알림 액션 화면 마감  
Priority: P8  
Owner: Frontend  
Feature Area: Auth UX + Submission UX + Notification UX  
Why this matters: 401/403 처리와 핵심 제출/읽음 액션이 사용자 흐름 완결에 직접 영향  
Dependencies: B-03, B-04, B-05  
Expected files: `frontend/src/api/client.ts`, `frontend/src/api/app.ts`, `NotificationsPage.tsx`, `SurveyRespondPage.tsx`, `QuestSubmitPage.tsx`, `App.tsx`  
Backend scope: 없음  
Frontend scope: fallback 금지 구간 고정, forbidden/auth-required 이벤트 처리, 알림 read/delete 액션  
Spring REST Docs scope: N/A(소비자), auth/error/submit/lifecycle endpoint 매핑 검증  
Verification commands: `npm --prefix frontend run lint && npm --prefix frontend run build`  
Completion condition: 401/403/submit/read-delete 주요 상호작용이 화면에서 검증 가능  
Risks: fallback 잔존 시 완료 착각  
Suggested commit message: Harden auth/error UX and complete user actions for survey quest and notifications

---

Task ID: Q-01  
Title: 브라우저 E2E smoke + CI 게이트 + REST Docs 노출 검증  
Priority: P9  
Owner: DevOps/QA  
Feature Area: Delivery verification  
Why this matters: 최종 단계의 “실행 증거”를 만들고 완료 착각 방지  
Dependencies: B-01~B-05, F-01~F-03  
Expected files: `.github/workflows/ci.yml`, `scripts/dev/*`, (신규) E2E 스크립트, `docs/test-report.md` 최소 갱신  
Backend scope: compose 기동 후 docs/API health 확인  
Frontend scope: 핵심 플로우 smoke 시나리오(로그인/권한거부/첨부/문의/설문or퀘스트/알림)  
Spring REST Docs scope: `mvn prepare-package` 산출물이 `.../static/docs/api/index.html`로 생성되고 `/docs/api/index.html`로 노출되는지 검증  
Verification commands:  
- `docker compose -f compose.yml --profile app up -d --build`  
- `python3 scripts/dev/smoke-lite.py --with-frontend --http`  
- `curl -fsS http://localhost:8080/docs/api/index.html | head`  
- `curl -fsS http://localhost/docs/api/index.html | head`  
- (추가) `npm --prefix frontend run test:e2e`  
Completion condition: CI에서 build+smoke+E2E+REST Docs 접근 증거 확보  
Risks: 로컬/CI 환경 차이, Docker 접근권한  
Suggested commit message: Add browser smoke and docs-serving checks to make completion evidence executable

---

4) **Spring REST Docs 반영 대상**

- 테스트 파일 중심: `backend/src/test/java/com/edussafy/backend/docs/ApiRestDocsTest.java`
- 문서 인덱스: `backend/src/docs/asciidoc/index.adoc`
- 생성 산출물: `backend/target/classes/static/docs/api/index.html`
- 제공 경로:
  - Backend: `http://localhost:8080/docs/api/index.html`
  - Nginx: `http://localhost/docs/api/index.html`

**이번 라운드 신규 snippet 목표**
- Attachment: `attachments-upload`, `attachments-download`, `attachments-link-domain`
- Support: `support-ticket-detail`, `support-ticket-message-create`, `support-ticket-answer`, `support-ticket-status-update`
- Auth/RBAC: `auth-roles-current`, `auth-logout`, `error-401`, `error-403`
- Survey/Quest: `survey-response-submit`, `quest-submission-create`
- Notification: `notifications-list`, `notifications-mark-read`, `notifications-delete`

---

5) **실행 전 주의할 blocker**

- `docs/final-verification.md` 기준으로 현재 상태는 **NOT COMPLETE / PARTIAL**이며 FAIL 항목이 남아 있음.
- `docs/remaining-work.md`에 핵심 blocker(첨부, 문의 depth, auth/RBAC, survey/notification, E2E)가 명시되어 있어 **문서만 업데이트하고 완료 선언 금지**.
- 현재 저장소 상태에서 `omx team` 과거 런타임은 `No team state found` 이력 → **새 backlog를 현재 task ID로 재생성**하고 시작 필요.
- Git 작업 전 노이즈 파일(`.codex/`, `docs/omx-plan-result.md`) 포함 여부를 팀에서 먼저 정리.

---

6) **TEAM 단계에서 실행할 첫 번째 task**

- **첫 시작 task: `P-01` (PM 소유)**
  - 이유: attachment/support/auth API 계약과 REST Docs snippet 목록을 먼저 고정해야 Backend/Frontend/QA 3개 lane이 충돌 없이 병렬로 진행 가능.
  - 완료 즉시 Backend는 `B-01`로 착수하고, Frontend는 API shape 준비 브랜치(F-01 선작업), DevOps/QA는 Q-01 테스트 골격 생성으로 동시 시작.


