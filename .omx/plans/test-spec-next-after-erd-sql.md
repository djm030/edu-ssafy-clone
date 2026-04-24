# Test Spec: ERD/SQL 이후 MVP 계약 문서화

## Verification Goal

다음 실행 단계가 구현자가 사용할 수 있는 문서 계약으로 닫혔는지 검증한다. 검증 대상은 문서 존재 여부, Priority 1 범위 누락 여부, 최신 DB 기준 사용 여부, API/DB/화면 추적성이다.

## Required Files

- `docs/SCREEN_LIST.md`
- `docs/MVP_TRACEABILITY.md`
- `docs/API_SPEC_DRAFT.md`
- `docs/DB_TABLE_DETAILS.md`

## Required Coverage

Priority 1 항목 9개가 모두 포함되어야 한다.

- 로그인
- 메인 대시보드
- 출석 현황
- 레벨/경험치/장학 포인트
- 공지사항 목록
- 자유게시판 목록
- 학습자료 목록
- Quest/평가 목록
- 설문 목록

## Structural Checks

1. 문서 존재 확인

```powershell
Test-Path docs\SCREEN_LIST.md
Test-Path docs\MVP_TRACEABILITY.md
Test-Path docs\API_SPEC_DRAFT.md
Test-Path docs\DB_TABLE_DETAILS.md
```

2. 구 DB 기준선 참조 차단

```powershell
rg "schema.sql|ERD.sql|schema.dbml" docs\API_SPEC_DRAFT.md docs\DB_TABLE_DETAILS.md
```

결과가 있다면 최신 기준으로 쓰였는지 확인한다. 최신 기준선처럼 쓰였으면 실패다.

3. 레거시 URL을 canonical API로 사용하지 않는지 확인

```powershell
rg "POST /login|GET /attendance|SecurityLoginForm\.do|attendanceClassList\.do" docs\API_SPEC_DRAFT.md
```

근거 섹션에서만 쓰였으면 허용한다. canonical endpoint로 쓰였으면 실패다.

4. Priority 1 키워드 누락 확인

```powershell
rg "로그인|메인|출석|레벨|장학|공지|자유게시판|학습자료|Quest|평가|설문" docs\SCREEN_LIST.md docs\MVP_TRACEABILITY.md
```

5. 테이블명 대조

`docs/API_SPEC_DRAFT.md`, `docs/DB_TABLE_DETAILS.md`, `docs/MVP_TRACEABILITY.md`에 등장하는 테이블명이 `docs/revised_schema_mysql8.sql`의 `CREATE TABLE` 목록에 존재해야 한다.

## Manual Review Criteria

- `SCREEN_LIST.md`의 각 row가 `Screen ID`, `FR ID`, `FS ID`, Domain, API candidate, Table(s), Evidence/Inference를 가진다.
- `MVP_TRACEABILITY.md`에서 Priority 1 요구사항이 화면, API, DB, 테스트 기준까지 이어진다.
- `API_SPEC_DRAFT.md`는 도메인 경계 기준으로 묶인다.
- `DB_TABLE_DETAILS.md`는 `docs/revised_schema_mysql8.sql`을 유일한 최신 DB 기준으로 사용한다.
- Quest 상세, Survey 상세/응답, eBook/PDF 뷰어는 deferred로 표시되고 MVP 완료 조건에 섞이지 않는다.

## Pass Condition

모든 required file이 존재하고, Priority 1 항목 9개가 모두 추적되며, 최신 DB 기준선이 `docs/revised_schema_mysql8.sql`로 유지되고, canonical API가 domain-based로 정리되어 있으면 통과한다.

## Failure Handling

- Priority 1 누락: `SCREEN_LIST.md`와 `MVP_TRACEABILITY.md`를 먼저 보정한다.
- API가 레거시 URL 모양으로 굳은 경우: domain-based canonical path로 바꾸고 레거시 URL은 evidence로만 남긴다.
- 존재하지 않는 테이블 참조: `docs/revised_schema_mysql8.sql` 기준으로 실제 테이블명으로 수정한다.
- deferred 항목이 MVP에 섞인 경우: 별도 보류 섹션으로 이동한다.
