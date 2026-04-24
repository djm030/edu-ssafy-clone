# PRD: ERD/SQL 이후 MVP 계약 문서화

## Decision

ERD와 SQL 초안 이후 바로 애플리케이션 구현으로 진입하지 않는다. 먼저 `docs/revised_schema_mysql8.sql`을 최신 DB 기준으로 고정하고, Priority 1 범위를 화면, API, DB, 검증 기준까지 연결하는 문서 계약을 만든다.

## Problem

현재 프로젝트는 코드베이스보다 캡처 자료와 문서 산출물이 중심이다. ERD와 SQL은 작성되어 있지만, 구현자가 바로 사용할 화면 ID, API 계약, DB 테이블 사용처, 테스트 기준이 아직 하나의 흐름으로 묶여 있지 않다. 이 상태에서 구현을 시작하면 화면 중심 구현, DB 중심 구현, API 중심 구현이 서로 다르게 해석될 수 있다.

## Source Baseline

- 최신 DB 기준: `docs/revised_schema_mysql8.sql`
- 최신 DBML 기준: `docs/revised_schema.dbml`
- 기능 상세 기준: `docs/FUNCTIONAL_SPEC.md`
- 요구사항 범위 기준: `docs/REQUIREMENTS.md`
- 도메인 경계 기준: `docs/DOMAIN_SPLIT.md`
- 역할/권한 기준: `docs/ROLE_MATRIX.md`

이전 산출물인 `docs/schema.sql`, `docs/ERD.sql`, `docs/schema.dbml`은 참고 자료로만 사용한다.

## MVP Scope

Priority 1 범위를 첫 MVP 계약 대상으로 한다.

- 로그인
- 메인 대시보드
- 출석 현황
- 레벨/경험치/장학 포인트
- 공지사항 목록
- 자유게시판 목록
- 학습자료 목록
- Quest/평가 목록
- 설문 목록

## Deferred Scope

아래 항목은 MVP 목록 계약에는 연결하되, 상세 구현 범위에서는 보류한다.

- Quest 상세 및 제출 흐름
- Survey 상세, 문항, 응답 제출 흐름
- 학습자료 eBook/PDF 내부 뷰어 렌더링
- 게시글 상세의 댓글/첨부 전체 동작
- 회원정보 수정 세부 필드 검증

## Required Deliverables

1. `docs/SCREEN_LIST.md`
   - Priority 1 화면 목록을 고정한다.
   - 각 row는 `Screen ID`, 화면명, Priority, `FR ID`, `FS ID`, Domain, API candidate, Table(s), MVP 포함 여부, Evidence/Inference, Deferred note를 포함한다.

2. `docs/MVP_TRACEABILITY.md`
   - Priority 1 요구사항을 화면, API, DB 테이블, 테스트 기준까지 한 줄로 추적한다.
   - `FR-*`와 `FS-*`를 모두 유지한다.

3. `docs/API_SPEC_DRAFT.md`
   - API를 레거시 JSP URL 모양이 아니라 도메인 기준 canonical API로 작성한다.
   - 각 API는 화면 ID, `FR/FS ID`, request, response, auth requirement, empty/error state, 주요 DB 테이블을 포함한다.

4. `docs/DB_TABLE_DETAILS.md`
   - `docs/revised_schema_mysql8.sql` 기준으로 Priority 1에서 쓰는 테이블과 컬럼을 설명한다.
   - PK/FK, 코드 그룹, 주요 인덱스, 사용 화면/API를 연결한다.

## Design Principles

- 화면 우선으로 범위를 고정하되, 각 화면 row에 API와 DB 후보를 같이 둔다.
- API는 도메인 경계 기준으로 작성하고 레거시 화면 URL은 근거로만 둔다.
- 공지사항은 별도 `notices` 테이블을 만들지 않고 `boards`, `board_categories`, `board_posts` 계열로 연결한다.
- 추론 항목은 반드시 Evidence/Inference로 구분한다.
- 보류 항목은 MVP에 포함된 것처럼 쓰지 않는다.

## Acceptance Criteria

- Priority 1 항목 9개가 모두 `docs/SCREEN_LIST.md`와 `docs/MVP_TRACEABILITY.md`에 존재한다.
- 모든 Priority 1 row에 `FR ID`와 `FS ID`가 모두 연결된다.
- 모든 화면 row에 최소 1개의 API candidate와 1개 이상의 관련 테이블이 연결된다.
- `docs/API_SPEC_DRAFT.md`의 canonical API는 domain-based path만 사용한다.
- `docs/API_SPEC_DRAFT.md`와 `docs/DB_TABLE_DETAILS.md`가 `schema.sql`, `ERD.sql`, `schema.dbml`을 최신 기준으로 참조하지 않는다.
- 문서에 등장하는 모든 테이블명이 `docs/revised_schema_mysql8.sql`의 `CREATE TABLE`에 실제로 존재한다.

## Execution Recommendation

현재 단계는 `solo`가 가장 적합하다. 문서 4개를 한 흐름으로 작성해야 하므로 병렬 team은 아직 이르다. 문서 작성 후 검증 루프까지 자동으로 닫고 싶다면 다음 단계에서 `ralph`를 사용한다. 실제 앱 구현 단계에서 프론트엔드, API, DB seed, 테스트가 나뉘면 그때 `team`을 사용한다.
