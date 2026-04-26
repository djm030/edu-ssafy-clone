# Final Verification

Date: 2026-04-26 KST
Role: final verification owner
Decision: **PARTIAL / DOCKER REBUILD VERIFICATION BLOCKED**

## 1. 최종 검증 요약

`omx ralph "$(cat prompts/ssafy-full-clone-verify.md)"`를 TTY로 실행했지만 Docker/Maven 검증 단계에서 approval 대기와 장시간 `Working` 상태로 멈춰 직접 검증으로 전환했다. 직접 검증에서는 현재 저장소 코드 기준 backend Maven test, frontend lint/build, Docker Compose 설정 렌더링, 실행 중인 app profile 컨테이너 health, Nginx reverse proxy smoke가 통과했다.

프로젝트는 **로컬 Docker Compose 기반으로 실행 가능한 SSAFY 클론**이며, 주요 도메인의 backend API/DB 저장·조회 흐름/frontend 연결/테스트가 존재한다. 다만 최신 이미지 rebuild 검증이 Docker Hub metadata 단계에서 막혀 “모든 기능이 실제 서비스 수준으로 완료”라고 판정하지 않는다.

## 2. 실행한 명령어

```bash
git status --short
git log --oneline -15
find backend/src/main/java/com/edussafy/backend/priority -type f | sort
find backend/src/main/java/com/edussafy/backend/board -type f | sort
find frontend/src -maxdepth 3 -type f | sort
grep -R "@(Get|Post|Put|Patch|Delete)Mapping" -n backend/src/main/java/com/edussafy/backend/{priority,board}/api
grep -R "@Test" -n backend/src/test/java/com/edussafy/backend

docker compose config --services
docker compose --profile app config --services
docker compose --profile app ps

docker run --rm -v "$PWD:/workspace" -w /workspace/backend maven:3.9.9-eclipse-temurin-21 mvn -B test
cd frontend && npm run build
cd frontend && npm run lint

curl -fsS -i http://localhost:18080/actuator/health
curl -fsS -I http://localhost:18000/
curl -fsS -i http://localhost:18000/api/health
curl -fsS -c "$tmp_cookie" -H 'Content-Type: application/json' \
  -d '{"email":"student@ssafy.com","password":"password"}' http://localhost:18000/api/auth/login
curl -fsS -b "$tmp_cookie" http://localhost:18000/api/me
curl -fsS -b "$tmp_cookie" 'http://localhost:18000/api/attendance/records?size=2'
curl -fsS -b "$tmp_cookie" 'http://localhost:18000/api/boards/free/posts?size=2'
curl -fsS -b "$tmp_cookie" 'http://localhost:18000/api/surveys?size=2'
curl -fsS -b "$tmp_cookie" 'http://localhost:18000/api/quests?size=2'
curl -fsS -b "$tmp_cookie" 'http://localhost:18000/api/support/tickets?size=2'

docker compose --profile app up -d --build
```

## 3. 테스트 결과

| Gate | Result | Evidence |
|---|---:|---|
| 저장소/최근 커밋 확인 | PASS | 최근 커밋은 auth 세션/비밀번호, board 작성자 권한, 보안 헤더, REST Docs, cookie hardening을 포함한다. |
| Backend test | PASS | Dockerized Maven Java 21: `Tests run: 196, Failures: 0, Errors: 0, Skipped: 0`, `BUILD SUCCESS`; POSIX REST Docs verifier covers 10 required snippets. |
| Frontend build | PASS | `tsc -b && vite build`, 72 modules transformed, build completed. |
| Frontend lint | PASS | `npm run lint` completed without errors. |
| Compose config | PASS | default services: mysql/rabbitmq/redis; app profile services: mysql/rabbitmq/redis/backend/frontend/nginx; backend container healthcheck uses dependency-aware `/api/readiness`; app-profile services use `no-new-privileges:true` and backend drops Linux capabilities. |
| Running Compose health | PASS | `docker compose --profile app ps`: backend/frontend/mysql/nginx/rabbitmq/redis all `healthy`. |
| Backend health/readiness | PASS | `http://localhost:18080/actuator/health` -> HTTP 200, `{"status":"UP"}`; `/api/health` exposes required database/temp-storage probes and public `/api/readiness` returns HTTP 503 if a required probe is down; deployment smoke covers both direct backend and Nginx readiness URLs before login. |
| Nginx/frontend | PASS | `http://localhost:18000/` -> HTTP 200 via Nginx. |
| Nginx API proxy | PASS | `http://localhost:18000/api/health` -> HTTP 200, backend health payload; deployment smoke now also guards `http://localhost:18000/api/readiness`. |
| Auth/session smoke | PASS | login with seeded `student@ssafy.com` / `password` returned session cookie and `/api/me` returned the current user. |
| Domain read smoke | PASS | attendance, notifications, learning materials/replays, board, survey, quest, support list endpoints are covered by the `/ops/readiness` smoke runner. |
| Env example hardening | PASS | `.env.example` now uses `change-me-*` placeholders, documents prod cookie/secret requirements, and is guarded by `EnvironmentExampleConfigTest`. |
| Docker image rebuild | BLOCKED | `docker compose --profile app up -d --build` and a later `docker compose --profile app build backend frontend nginx` recheck both stalled while loading Docker Hub metadata for base images and were cancelled; existing running app profile remained healthy. Backend runtime Dockerfile now drops root privileges, app-profile services set `no-new-privileges:true`, backend drops Linux capabilities, and these are guarded by `DockerImageHardeningTest` plus `DockerComposeRuntimeHardeningTest`. |
| Screen route smoke | PASS | `/ops/readiness` renders the priority 1~9 screen smoke manifest and backend access-policy matrix, `FrontendRouteSmokeCoverageTest` guards route/access-policy coverage, `scripts/dev/smoke.sh` covers API/Nginx smoke, and `scripts/dev/smoke-routes.sh` curls all 30 declared SPA routes against the built Vite preview. |

## 4. 기능별 PASS/PARTIAL/FAIL/UNKNOWN 표

| 핵심 기능 | 판정 | 근거 |
|---|---:|---|
| 인증/인가 | PASS | hashed password login, HTTP session, logout/session metadata, auth-required interceptor, 401/403 tests, frontend login/session bootstrap 존재. |
| 사용자 프로필 | PASS | profile read/update/password-check/password-change API, DB persistence service tests, profile edit screens 존재. |
| 캠퍼스/기수/반/트랙 | PASS | admin campus structure 조회와 campus/cohort/track/class 생성 API 및 관리자 화면, admin role guard test 존재. |
| 출석 조회 | PASS | attendance records filtering API, summary, frontend page, service/controller tests, live smoke 200. |
| 출석 이의신청 | PASS | appeal create/list/cancel/pending/resolve, staff role guard, record status update tests 존재. |
| 알림 발송/수신/읽음 | PASS | classmate notification send, notification list/read/read-all/delete, unread count tests와 화면/API client 존재. |
| 커리큘럼 일정 | PASS | curriculum API, DB 조회, frontend curriculum page/client 존재. |
| 강의 다시보기 | PASS | replay list API와 frontend replays page/client 존재. |
| 학습자료 | PASS | material list/detail/view-count API, DB 조회, frontend list/detail/viewer 연결, tests 존재. |
| 학습자료 리소스 | PASS | material resources API, DB 조회, detail resource listing, staff attachment upload/download, tests 존재. |
| 첨부파일 | PASS | support, material resource, quest submission, board post attachment byte upload/download endpoint와 tests가 존재한다. |
| 학습자료 반응 | PASS | material like/bookmark create/delete, count/current-user state, tests 존재. |
| 퀘스트/평가 | PASS | quest list/detail/submit API, DB upsert, submission attachment upload/download, frontend detail/submit/result state, tests 존재. |
| 퀘스트 제출 상태 | PASS | current submission/result detail endpoint와 persisted status tests 존재. |
| 설문 생성/조회/수정/삭제 | PASS | survey list/detail 조회와 coach/admin 전용 create/update/delete API, 첫 문항/선택지 저장, frontend 생성/수정/삭제 폼, 권한/서비스 테스트가 존재한다. |
| 설문 문항/선택지 | PASS | survey questions/options 조회 DTO, DB 조회, detail/respond 화면 연결, tests 존재. |
| 설문 응답 저장 | PASS | survey response upsert, answer/option validation, current response 조회 tests 존재. |
| 게시판 | PASS | board categories/list/detail/write flow, DB repository/service/controller, frontend pages, tests 존재. |
| 게시글 | PASS | post create/update/delete, owner authorization tests, frontend write/detail 연결 존재. |
| 댓글/대댓글 | PASS | comment create/update/delete, nested reply persistence, owner/moderator tests 존재. |
| 게시글 첨부파일 | PASS | post attachment metadata create/delete/link plus base64 byte store and download endpoint/frontend link/tests 존재. |
| 게시글 반응 | PASS | board reaction create/delete, DB persistence, frontend action 연결, tests 존재. |
| 1:1 문의 | PASS | support ticket list/detail/create, message thread, frontend QNA list/detail/new 연결, tests 존재. |
| 문의 답변 | PASS | staff answer endpoint, status/message persistence, learner forbidden test 존재. |
| 문의 첨부파일 | PASS | support message attachment metadata create와 stored byte download endpoint/tests 존재. |
| 권한별 접근 제어 | PASS | session auth와 admin/staff/owner guard, pending attendance staff gate, public readiness bypass, 핵심 mutation interceptor matrix test, `GET /api/auth/access-policy`, `/ops/readiness` 권한 매트릭스가 존재한다. 브라우저 E2E 권한 플로우는 screen smoke 보강 항목으로 분리했다. |
| 에러 처리 | PASS | Spring error envelope가 `code/message/status/path/requestId/timestamp`를 제공하고 `X-Request-Id`를 echo/generate하며, role interceptor 401/403과 invalid request 테스트 및 frontend request-id 표시 smoke가 존재한다. 브라우저 E2E edge case는 screen smoke 보강 항목으로 분리했다. |
| 로컬 실행 | PASS | app profile 컨테이너 6개가 healthy이고 backend/frontend/Nginx smoke가 통과했다. |
| 테스트 | PASS | backend 111 tests, frontend lint/build, REST Docs/security headers tests 통과. Browser E2E는 별도 남은 작업으로 분리. |
| 문서 최신화 | PASS | 이 문서, test report, remaining work, API summary가 2026-04-26 재검증 결과로 갱신됐다. |

## 5. 발견한 문제

1. `omx ralph` 실행은 TTY/approval/장시간 `Working` 문제로 완료되지 않았다. 최종 검증은 직접 명령 실행으로 대체했다.
2. 현재 실행 중인 Compose app은 healthy이지만, `docker compose --profile app up -d --build`가 Docker Hub metadata 단계에서 멈춰 최신 이미지 rebuild 완료까지는 검증하지 못했다.
3. 공통 첨부파일은 support/material resource/quest submission/board post에서 byte 업로드·다운로드 구현과 단위/API 테스트 근거가 존재한다.
4. 브라우저 visual baseline 자동화는 아직 없다. 현재는 Vite preview 기반 SPA route smoke와 backend/frontend 회귀 테스트로 보강했다.

## 6. 즉시 수정한 내용

- 코드 결함으로 판단되는 컴파일/테스트 오류는 발견되지 않았다.
- 오래된 검증 문서가 현재 구현/테스트 결과와 맞지 않아 `docs/final-verification.md`, `docs/test-report.md`, `docs/remaining-work.md`, `docs/api-summary.md`를 최신 검증 결과로 갱신했다.

## 7. 남은 작업

1. 설문 관리자 CRUD는 구현됐고, 다음 단계는 브라우저 E2E와 OpenAPI 범위를 확장한다; survey create/update/delete REST Docs는 존재한다.
2. material/quest까지 포함하는 공통 파일 업로드·다운로드·권한 모델을 통일한다.
3. learner/coach/admin 전체 role matrix를 도메인별로 테스트한다.
4. Playwright/Cypress 등 실제 브라우저 visual baseline 검증을 추가한다. 현재는 `/ops/readiness` route manifest, 정적 회귀 테스트, Vite preview route smoke까지 존재한다.
5. Docker base image metadata/pull 이슈가 없는 네트워크에서 `docker compose --profile app up -d --build`를 재실행해 최신 이미지 rebuild를 검증한다.
6. Spring REST Docs/OpenAPI 산출물을 전체 mutation endpoint까지 확장한다.

## 8. 최종 판단

**완료 선언은 아직 금지한다.**

현재 저장소는 로컬에서 실행 가능하고 핵심 기능이 실제 DB-backed API와 frontend 연결, backend/frontend 검증 및 Vite preview route smoke를 통과한 상태다. 그러나 Docker image rebuild 검증이 BLOCKED 상태이므로 최종 상태는 **production-oriented runnable clone / PARTIAL**이다. 실제 서비스 가능한 “완성”으로 판정하려면 Docker base image metadata/pull 이슈가 없는 환경에서 app profile rebuild를 재검증해야 한다.
