# edu.ssafy.com Full Clone Master Plan

## 목적
`https://edu.ssafy.com/` 클론을 단건 화면 구현이 아니라 전체 서비스 구축 프로젝트로 관리한다. PM은 캡처와 문서를 근거로 전체 API, 화면, 인프라, QA, 테스트 범위를 확정하고 Backend, Frontend, DevOps-QA에게 반복 라운드로 분배한다.

## 기준 자료
- 화면 근거: `ssafy_pages/**`
- 요구사항: `docs/REQUIREMENTS.md`
- 기능 명세: `docs/FUNCTIONAL_SPEC.md`
- DB 기준: `docs/revised_schema_mysql8.sql`
- 현재 계약: `docs/collaboration/API_SPEC_DRAFT.md`, `docs/collaboration/SCREEN_LIST.md`

live site는 로그인과 세션 상태에 따라 달라질 수 있으므로, 현재 구현 판단은 저장소에 보관된 캡처와 문서를 우선한다.

## 전체 모듈
1. 인증/세션: 로그인, 비밀번호 찾기, 세션 만료, 회원정보 재인증
2. 공통 shell: 글로벌 메뉴, 사용자 요약, 모바일 메뉴, 오류 상태
3. 메인: 개인화 대시보드, 빠른 진입, 요약 위젯
4. 마이캠퍼스: 레벨/경험치/포인트, 출석 현황, 출석 소명, 알림함
5. 강의실: 주차별 커리큘럼, 다시보기, 학습자료 목록/상세/PDF popup
6. Quest/평가: 목록, 상태, 상세, 제출/결과
7. 설문: 목록, 상태, 상세, 응답 제출
8. 커뮤니티: 자유게시판, 우리반 보기, 학생 알림, 게시글 상세
9. HELP DESK: 공지, FAQ, 1:1 문의, 학사규정
10. 회원정보: 비밀번호 재확인, 정보 수정, 연락처/주소/수신동의
11. 운영/관측: Docker Compose, Nginx, MySQL, Redis, RabbitMQ, ELK, smoke/health checks

## 단계
### Phase 0: 기반
- React, Spring Boot, Docker Compose, Nginx, MySQL, Redis, RabbitMQ, ELK scaffold
- PM 계약 문서와 500줄 제한 고정
- 현재 상태: 진행됨, 통합 검증 중

### Phase 1: 우선순위 1 read-only 웹앱
- 로그인, 메인, 출석, 레벨/포인트, 공지 목록, 자유게시판 목록, 학습자료 목록, Quest 목록, 설문 목록
- 목표: 주요 route 직접 진입, API 연결, seed 기반 smoke 가능
- 현재 상태: Backend/Frontend/DevOps-QA 1차 구현 완료, PM 통합 검증 중

### Phase 2: 우선순위 2 운영 화면
- 알림함, 주차별 커리큘럼, 강의 다시보기, FAQ, 1:1 문의 목록, 회원정보 비밀번호 재확인
- 목표: 목록과 상태 중심 기능 안정화

### Phase 3: 상세/작성 흐름
- 게시글 상세, 공지 상세, 학습자료 상세/PDF popup, Quest 상세, 설문 응답, 문의 등록
- 목표: 목록에서 상세/작업 흐름까지 연결

### Phase 4: 수정/제출/관리
- 게시글 작성, 댓글, 추천/찜, 출석 소명 제출, 설문 제출, Quest 제출, 회원정보 수정 저장
- 목표: 쓰기 API와 validation, optimistic/error UI

### Phase 5: 안정화
- E2E smoke, API contract tests, container startup, ELK log flow, failure replay
- 목표: `docker compose --profile app up` 기준으로 서비스 안정 구동

## 역할 운영
PM:
- 전체 범위와 phase를 고정한다.
- 라운드마다 `WORK_TRACKER.md`에 목표, 실패 원인, 다음 조치를 기록한다.
- API/화면 계약 변경을 승인한다.
- 일정량 작업 완료 후 Lore commit 형식으로 커밋/푸시한다.

Backend:
- `backend/**`만 수정한다.
- API catalog를 구현하고 MySQL schema와 seed를 기준으로 동작한다.
- 데이터가 없으면 빈 응답 또는 안전한 fallback을 반환한다.

Frontend:
- `frontend/**`만 수정한다.
- screen catalog route를 구현하고 API/empty/error/loading 상태를 모두 처리한다.
- 모든 화면은 app shell 아래에서 직접 진입 가능해야 한다.

DevOps-QA:
- `compose*.yml`, `infra/**`, `scripts/**`, `.env.example`만 수정한다.
- seed, smoke, compose validation, health check, observability를 유지한다.
- 실패하면 재현 명령, 로그, 원인을 tracker에 보고한다.

## 안정화 루프
1. PM이 라운드 목표와 완료 조건을 정한다.
2. 세 lane이 분리된 write scope에서 구현한다.
3. PM이 lint/build/test/compose/smoke/line-count를 실행한다.
4. 실패하면 `WORK_TRACKER.md`에 실패 원인과 owner를 기록한다.
5. owner lane에 수정 라운드를 배정한다.
6. 모든 gate가 통과하면 커밋/푸시를 시도한다.

## 완료 기준
- 모든 catalog route/API가 최소 read path로 동작한다.
- Docker Compose app profile 설정 검증이 통과한다.
- MySQL seed와 schema 검증이 가능하다.
- Frontend 빌드와 lint가 통과한다.
- Backend compile/test-compile이 통과하고, 테스트 실행 실패는 환경 원인까지 기록된다.
- 수동 또는 자동 smoke 기준으로 주요 route와 API가 안정적으로 응답한다.

## 완료 전 재대조 게이트
작업이 완료된 것처럼 보여도 PM은 반드시 `API_CATALOG.md`와 `SCREEN_CATALOG.md`를 실제 구현과 다시 대조한다.

확인 순서:
1. API catalog의 각 Priority 항목이 controller, service, repository, smoke 중 하나 이상의 증거와 연결되는지 확인한다.
2. Screen catalog의 각 route가 React route, AppShell navigation, page, mock fallback 중 하나 이상의 증거와 연결되는지 확인한다.
3. 누락된 API나 화면이 있으면 해당 round를 complete로 닫지 않고 다시 PM 분해 단계로 돌아간다.
4. 실패한 gate는 `WORK_TRACKER.md`에 원인, owner, 재시도 조건을 적고 같은 실패가 반복되지 않게 다음 lane 입력에 포함한다.
