# Implementation Plan

## 목표
PM + Backend + Frontend + DevOps-QA 협업 하네스를 실제 구현 전에 고정하고, 첫 파일럿 기능인 공지사항 목록/자유게시판 목록을 작은 단위로 진행한다.

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

### 제외
- 게시글 상세 본문 구현
- 댓글 작성/수정/삭제
- 게시글 작성/수정/삭제
- 첨부파일 다운로드
- 알림/쪽지 발송
- 실제 배포

## 작업 순서
### 0. 계약 고정
담당: PM

작업:
- `DESIGN_NOTES.md` 확인
- `API_SPEC_DRAFT.md` 확인
- `SCREEN_LIST.md` 확인
- `TEST_SPEC.md` 확인
- 레인 문서 3개 확인

완료 조건:
- Backend, Frontend, DevOps-QA가 각자 소유 경로와 금지 경로를 이해한다.
- 파일럿 기능의 API/UI/DB 계약이 한 문서 세트에 연결되어 있다.

### 1. DB 실행 검증
담당: DevOps-QA, Backend 지원

작업:
- MySQL 8 컨테이너 또는 로컬 MySQL 8에서 `docs/revised_schema_mysql8.sql` 실행
- 실패 시 SQL compatibility 이슈를 기록
- 공지/자유게시판 seed data 최소안을 작성

완료 조건:
- schema 실행 성공 또는 실패 원인과 수정 후보가 기록된다.
- 게시판 목록 API 테스트에 필요한 최소 seed shape가 정의된다.

### 2. Backend API 구현
담당: Backend

작업:
- `GET /api/boards/{boardCode}/categories`
- `GET /api/boards/{boardCode}/posts`
- 검색, 카테고리, 페이지네이션 파라미터 처리
- boardCode `notice`, `free` 지원
- 공통 응답 DTO 정의

완료 조건:
- API spec의 success/error shape를 만족한다.
- notice/free가 같은 API 로직을 재사용한다.
- 별도 `notices` 테이블을 만들지 않는다.

### 3. Frontend 화면 구현
담당: Frontend

작업:
- 공지사항 목록 화면
- 자유게시판 목록 화면
- 공통 게시글 목록 컴포넌트 후보
- 로딩/빈 상태/오류 상태
- 카테고리/검색/페이지네이션 UI

완료 조건:
- `SCREEN_LIST.md`의 화면 계약을 만족한다.
- API 응답이 없을 때도 빈 상태가 깨지지 않는다.
- legacy URL이 아니라 앱 내부 route/API 계약을 사용한다.

### 4. 하네스 테스트
담당: DevOps-QA

작업:
- schema 실행 검증
- API smoke test
- UI smoke test
- 실패 증거 수집

완료 조건:
- 테스트 명령과 결과가 재현 가능하다.
- 실패한 항목은 담당 레인으로 되돌릴 수 있다.

### 5. PM 통합 검토
담당: PM

작업:
- 구현이 설계노트/구현계획/API/화면/테스트 문서와 일치하는지 확인
- 남은 리스크 작성
- 사람리뷰 요청 자료 작성

완료 조건:
- 사람 리뷰어가 볼 수 있는 변경 요약과 테스트 증거가 있다.

## 파일 소유권
### PM 전용
- `docs/collaboration/TEAM_HARNESS.md`
- `docs/collaboration/DESIGN_NOTES.md`
- `docs/collaboration/IMPLEMENTATION_PLAN.md`
- `docs/collaboration/API_SPEC_DRAFT.md`
- `docs/collaboration/SCREEN_LIST.md`
- `docs/collaboration/TEST_SPEC.md`

### Backend
- `docs/lanes/BACKEND_LANE.md`
- future backend app paths
- future migration/seed paths, PM 승인 필요

### Frontend
- `docs/lanes/FRONTEND_LANE.md`
- future frontend app paths

### DevOps-QA
- `docs/lanes/DEVOPS_QA_LANE.md`
- future docker/test/script paths

## 병렬화 기준
다음 조건이 모두 맞으면 3개 레인을 병렬 실행한다.

- API 응답 shape가 확정되어 있다.
- DB seed 최소안이 있다.
- Frontend가 mock response로 먼저 작업 가능하다.
- DevOps-QA가 DB/API/UI 검증을 독립 실행할 수 있다.

## 중단 기준
- SQL이 MySQL 8에서 실행되지 않고 원인이 schema 구조 수정이 필요한 경우
- API 응답 shape가 Frontend 요구와 충돌하는 경우
- 앱 프레임워크 선택이 필요해 구현 경로가 결정되지 않는 경우

## 다음 실행 명령 후보
```bash
omx team 3:executor "execute the board-list pilot from docs/collaboration/IMPLEMENTATION_PLAN.md"
```

## Decision Log
- 2026-04-24: 첫 구현 단위는 게시판 목록 API/UI/harness로 제한했다.
