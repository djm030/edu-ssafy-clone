# Architecture Summary

## Worker-2 Required Architecture Coverage (2026-04-24)

### 전체 구조
- Browser → Nginx reverse proxy → React frontend / Spring Boot backend → MySQL, Redis, RabbitMQ 흐름을 기준으로 한다.
- `compose.yml`의 `app` profile은 `mysql`, `redis`, `rabbitmq`, `backend`, `frontend`, `nginx`를 하나의 로컬 실행 경계로 묶는다.
- `compose.observability.yml`은 애플리케이션 로그 관찰용 ELK(Filebeat, Logstash, Elasticsearch, Kibana)를 별도 compose 파일로 제공한다.

### Docker Compose 서비스 구조
- `mysql`: MySQL 8.0, `docs/revised_schema_mysql8.sql` 및 `scripts/mysql/20-board-list-seed.sql`을 초기화 SQL로 마운트한다.
- `redis`: Redis 7 Alpine, AOF를 켜고 `ssafy_redis_data` 볼륨을 사용한다. 현재 코드는 의존성/환경 연결은 되어 있으나 기능별 캐시 또는 세션 저장 사용은 아직 명시 구현되지 않았다.
- `rabbitmq`: RabbitMQ 3.13 management, AMQP `5672`와 관리 UI `15672`를 노출한다. 현재 코드는 의존성/환경 연결은 되어 있으나 알림 발행/소비 파이프라인은 demo 수준이다.
- `backend`: `backend/Dockerfile`로 빌드되며 `SPRING_DATASOURCE_URL`, `SPRING_DATA_REDIS_*`, `SPRING_RABBITMQ_*`로 compose 서비스명에 연결된다.
- `frontend`: `frontend/Dockerfile`로 Vite 빌드 산출물을 Nginx 정적 서버에 올린다. 컨테이너 내부 `/api/`는 502를 반환하고, 실제 API 프록시는 상위 `nginx` 서비스가 담당한다.
- `nginx`: `infra/nginx/conf.d/default.conf`를 마운트해 `/api/**`, `/actuator/**`를 backend로, 그 외 경로를 frontend로 프록시한다.

### Backend Spring Boot 구조
- 진입점은 `backend/src/main/java/com/edussafy/backend/BackendApplication.java`이다.
- `backend/src/main/java/com/edussafy/backend/board/**`는 게시판/카테고리/게시글 API, DTO, repository, service, 예외 처리를 담당한다.
- `backend/src/main/java/com/edussafy/backend/priority/**`는 auth/profile/dashboard/attendance/notification/learning/quest/survey/community/support API와 DTO/service/repository를 담당한다.
- `backend/src/main/resources/application.yml`은 MySQL/Redis/RabbitMQ host를 compose 서비스명 기본값(`mysql`, `redis`, `rabbitmq`)으로 둔다.

### Frontend React 구조
- `frontend/src/App.tsx`가 경로 분기, 로그인 상태, 401/403 전역 이벤트 처리, forbidden/login 이동을 소유한다.
- `frontend/src/api/client.ts`는 `fetchJson` 공통 클라이언트, API error, mock fallback 정책, auth/forbidden 이벤트 dispatch를 담당한다.
- `frontend/src/api/app.ts`와 `frontend/src/api/boards.ts`는 backend DTO를 화면 DTO로 정규화한다.
- `frontend/src/pages/**`는 로그인, 대시보드, 출석, 학습자료, 퀘스트, 설문, 게시판, 문의, 알림, 프로필 화면을 나눈다.
- 스타일은 `frontend/src/styles.css`, `p2.css`, `p3.css`, `p4.css`, `responsive.css`에 있으며 추가 CSS framework는 도입하지 않는다.

### DB/API 구조
- DB schema 기준 파일은 `docs/revised_schema_mysql8.sql`이며 MySQL compose 초기화에 직접 연결된다.
- Board API는 `/api/boards/**` 경로에서 board/category/post list/detail/write 계층을 제공하고 `BoardRepository`가 JDBC로 MySQL을 조회한다.
- Priority API는 `/api/auth/**`, `/api/profile/**`, `/api/dashboard/**`, `/api/attendance/**`, `/api/notifications/**`, `/api/learning/**`, `/api/quests/**`, `/api/surveys/**`, `/api/community/**`, `/api/support/**` 계열을 제공한다.
- Frontend API adapter는 같은 `/api/**` 경로를 호출하므로 browser 기준 URL은 Nginx reverse proxy 아래에서 동일하게 유지된다.

### Redis/RabbitMQ 사용 지점
- Infra 연결 지점: `compose.yml` backend 환경변수와 `application.yml`에 Redis/RabbitMQ host/port가 정의되어 있고 backend Maven dependency도 포함된다.
- 현재 애플리케이션 코드에서 Redis를 사용하는 도메인 캐시/세션 로직은 아직 확인되지 않았다. 따라서 Redis는 실행환경 parity와 향후 session/cache 지점으로 문서화한다.
- 현재 애플리케이션 코드에서 RabbitMQ producer/consumer는 아직 확인되지 않았다. 알림 도메인은 `/api/notifications/**` 동기 API와 demo fallback 중심이며 RabbitMQ 비동기화는 남은 작업이다.

### Nginx reverse proxy 흐름
- 외부 HTTP 진입점은 `nginx` 서비스의 `${HTTP_PORT:-80}`이다.
- `/api/**`와 `/actuator/**`는 `upstream ssafy_backend`(`backend:8080`)로 전달된다.
- `/` 및 SPA route는 `upstream ssafy_frontend`(`frontend:80`)로 전달된다.
- `client_max_body_size 20m`이 설정되어 있어 향후 첨부 업로드 프록시 용량 상한은 20MB 기준이다.

### ELK 로그 흐름
- `compose.observability.yml`은 `filebeat → logstash → elasticsearch → kibana` 흐름을 제공한다.
- `infra/filebeat/filebeat.yml`은 Docker container JSON log(`/var/lib/docker/containers/*/*.log`)를 읽고 docker metadata와 `project.name=ssafy-clone` 필드를 추가해 Logstash `5044`로 보낸다.
- `infra/logstash/pipeline/logstash.conf`는 beats/tcp input을 받아 container name을 `service_name`으로 추가하고 `ssafy-clone-%{+YYYY.MM.dd}` index로 Elasticsearch에 저장한다.
- Kibana는 `5601`, Elasticsearch는 `9200`으로 노출된다.

### 파일 업로드 구조
- 현재 repository에는 게시글/학습자료 mock 및 DB seed에서 `hasAttachment`, `fileName`, resource URL 등 첨부 메타데이터가 존재한다.
- Nginx에는 업로드 가능성을 고려한 `client_max_body_size 20m`가 설정되어 있다.
- Multipart upload controller, object/local storage 저장, 다운로드 API, attachment metadata write path는 아직 end-to-end로 구현되지 않아 남은 구조 gap이다.

### 인증/인가 구조
- Backend `AuthController`는 demo login/current roles/logout/password-check 흐름을 제공한다.
- `PriorityApiService`는 demo current user와 권한 목록을 반환하고, repository 조회 실패 시 safe fallback을 사용한다.
- Frontend `LoginPage`, `App.tsx`, `api/client.ts`는 로그인 상태와 401/403 이벤트 기반 login/forbidden 이동을 처리한다.
- JWT/session persistence, Spring Security filter chain, API별 RBAC enforcement는 아직 production 수준으로 구현되지 않아 주요 gap으로 남아 있다.

### 주요 데이터 흐름
- 로그인: `LoginPage` → `login()` → `POST /api/auth/login` → `AuthController` → `PriorityApiService` → `PriorityApiRepository.findUserByEmail()` 또는 demo fallback → `{ user }` 응답 → React user state 갱신.
- 게시판 목록/상세: React board pages → `frontend/src/api/boards.ts` → `/api/boards/**` → `BoardController` → `BoardService` → `BoardRepository` → MySQL schema/seed → `{ items, page }` 또는 `{ post }` wrapper.
- 학습/출석/설문/문의/알림: React pages → `frontend/src/api/app.ts` → priority controllers → `PriorityApiService`/repositories → MySQL 조회 또는 safe demo fallback.
- 권한 오류: backend/API client에서 401/403 발생 → `fetchJson`이 `edussafy:auth-required` 또는 `edussafy:forbidden` 이벤트 dispatch → `App.tsx`가 `/login` 또는 `/forbidden`으로 이동.
- 로그 관찰: Docker container stdout/stderr → Filebeat docker input → Logstash pipeline → Elasticsearch daily index → Kibana 탐색.

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
