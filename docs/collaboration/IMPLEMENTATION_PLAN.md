# Implementation Plan

## 목표
PM + Backend + Frontend + DevOps-QA 작업 하네스를 실제 구현 전에 고정하고, 첫 파일럿 기능을 공지사항 목록과 자유게시판 목록의 작은 단위로 진행한다.

## 범위
### 포함
- 공지사항 목록
- 자유게시판 목록
- 게시글 메타데이터 표시
- 게시판 카테고리 필터
- 검색어 필터
- 페이지네이션
- 빈 상태
- SQL 실행 검증 계획
- API/UI smoke 검증 계획
- Docker Compose 기반 실행 환경

### 제외
- 게시글 상세 본문
- 댓글 작성, 수정, 삭제
- 게시글 작성, 수정, 삭제
- 첨부파일 다운로드
- 알림과 쪽지 발송
- 실제 외부 배포
- 운영급 secret 관리

## 작업 순서
### 0. 계약 고정
담당: PM

작업:
- `DESIGN_NOTES.md` 확인
- `API_SPEC_DRAFT.md` 확인
- `SCREEN_LIST.md` 확인
- `TEST_SPEC.md` 확인
- lane별 소유 경로 확인

완료 조건:
- Backend, Frontend, DevOps-QA가 각자 소유 경로와 금지 경로를 이해한다.
- 파일럿 기능의 API/UI/DB 계약이 문서 링크로 연결되어 있다.
- 직접 작성 코드 파일 500줄 제한이 모든 lane에 공유되어 있다.

### 1. DB 실행 검증
담당: DevOps-QA, Backend 지원

작업:
- MySQL 8 컨테이너에서 `docs/revised_schema_mysql8.sql` 실행
- 실패 시 SQL compatibility 이슈 기록
- 공지와 자유게시판 seed data 최소안을 작성

완료 조건:
- schema 실행 성공 또는 실패 원인과 수정 후보가 기록된다.
- 게시판 목록 API 테스트에 필요한 최소 seed shape가 정의된다.

### 2. Backend API 구현
담당: Backend

작업:
- `GET /api/boards/{boardCode}/categories`
- `GET /api/boards/{boardCode}/posts`
- 검색, 카테고리, 페이지네이션, 정렬 파라미터 처리
- boardCode `notice`, `free` 지원
- 공통 응답 DTO 정의

완료 조건:
- API spec의 success/error shape를 만족한다.
- notice/free가 같은 API 로직을 재사용한다.
- 별도 `notices` 테이블을 만들지 않는다.
- Java 소스 파일은 500줄 이하로 유지한다.

### 3. Frontend 화면 구현
담당: Frontend

작업:
- 공지사항 목록 화면
- 자유게시판 목록 화면
- 공통 게시글 목록 컴포넌트
- 로딩, 빈 상태, 오류 상태
- 카테고리, 검색어, 페이지네이션 UI

완료 조건:
- `SCREEN_LIST.md`의 화면 계약을 만족한다.
- API 응답이 없어도 빈 상태가 깨지지 않는다.
- legacy URL이 아니라 내부 route/API 계약을 사용한다.
- React 컴포넌트와 TS 파일은 500줄 이하로 유지한다.

### 4. 하네스와 테스트
담당: DevOps-QA

작업:
- Docker Compose runtime 검증
- MySQL schema 검증 스크립트
- API smoke test
- UI smoke test
- 실패 증거 수집

완료 조건:
- 테스트 명령과 결과가 재현 가능하다.
- 실패한 항목은 담당 lane으로 되돌릴 수 있다.
- Compose 설정 파일과 스크립트는 500줄 이하로 유지한다.

### 5. PM 통합 검토
담당: PM

작업:
- 구현이 설계노트, 구현계획, API, 화면, 테스트 문서와 일치하는지 확인
- 남은 리스크 작성
- 사람리뷰 요청 자료 작성

완료 조건:
- 리뷰 가능한 변경 요약과 테스트 증거가 있다.

## 파일 소유권
### PM
- `docs/collaboration/TEAM_HARNESS.md`
- `docs/collaboration/DESIGN_NOTES.md`
- `docs/collaboration/IMPLEMENTATION_PLAN.md`
- `docs/collaboration/API_SPEC_DRAFT.md`
- `docs/collaboration/SCREEN_LIST.md`
- `docs/collaboration/TEST_SPEC.md`
- `docs/collaboration/STACK_DECISION.md`

### Backend
- `backend/**`
- Backend 관련 seed/migration 후보는 PM 승인 필요

### Frontend
- `frontend/**`

### DevOps-QA
- `compose*.yml`
- `infra/**`
- `scripts/**`
- `.env.example`

## 병렬화 기준
다음 조건을 모두 만족하면 3개 lane이 병렬 실행한다.

- API 응답 shape가 확정되어 있다.
- DB seed 최소안이 있다.
- Frontend가 mock response로 먼저 작업 가능하다.
- DevOps-QA가 DB/API/UI 검증을 독립 실행할 수 있다.

## 우선순위 1 확장 실행
이번 구현은 게시판 목록 파일럿을 전체 우선순위 1 웹앱으로 확장한다.

Backend:
- 데모 로그인, 현재 사용자, 대시보드 요약 API를 구현한다.
- 출석, 알림, 학습자료, Quest, 설문 목록 API를 구현한다.
- 공지와 자유게시판은 기존 board API를 확장해 계속 재사용한다.

Frontend:
- `/login`, `/`, `/mycampus/attendance`, `/mycampus/level`을 구현한다.
- `/help/notice`, `/community/free`, `/learning/materials`, `/quest`, `/survey`를 구현한다.
- 공통 app shell, navigation, loading/empty/error 상태를 공유한다.

DevOps-QA:
- MySQL seed가 우선순위 1 화면을 검증할 수 있게 보강한다.
- Compose config validation과 smoke script를 유지한다.
- Docker 권한이 막히는 환경에서는 권한 오류를 증거로 기록한다.

PM:
- `API_SPEC_DRAFT.md`와 `SCREEN_LIST.md`를 계약 기준으로 유지한다.
- 500줄 초과 파일이 생기면 해당 lane에 분리 요청한다.
- 일정량 작업 완료 후 Lore commit 형식으로 커밋/푸시를 시도한다.

## 중단 기준
- SQL이 MySQL 8에서 실행되지 않고 schema 구조 수정이 필요한 경우
- API 응답 shape가 Frontend 요구와 충돌하는 경우
- 선택된 프레임워크나 런타임 경로가 아직 결정되지 않은 경우
- 특정 파일이 500줄을 넘기기 시작해 분리가 필요한 경우

## 다음 실행 명령 후보
```bash
omx team 3:executor "execute the board-list pilot from docs/collaboration/IMPLEMENTATION_PLAN.md"
```

## Decision Log
- 2026-04-24: 첫 구현 단위를 게시판 목록 API/UI/harness로 제한했다.
- 2026-04-24: 고정 스택을 React, Spring Boot, Nginx, MySQL, Redis, RabbitMQ, ELK, Docker Compose로 확정했다.
- 2026-04-24: 직접 작성 코드 파일 500줄 제한을 작업 완료 조건에 추가했다.
- 2026-04-24: 구현 범위를 우선순위 1 전체 웹앱으로 확장했다.
