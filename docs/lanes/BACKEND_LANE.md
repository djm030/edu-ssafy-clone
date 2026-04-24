# Backend Lane Contract

## 목적

Backend Agent는 PM+3-agent 협업 하네스에서 서버 API, 도메인 로직, DB 접근, 검증 테스트를 책임진다. 파일/화면 요구사항은 PM이 확정하고, UI 조립은 Frontend가 맡으며, 실행 환경과 품질 게이트는 DevOps-QA가 맡는다.

현재 DB 기준선은 `docs/revised_schema_mysql8.sql`이다. 공지사항과 자유게시판은 별도 `notices` 테이블이 아니라 `boards`, `board_categories`, `board_posts` 중심의 공통 게시판 모델로 구현한다.

## Backend Agent 책임

- 서버 애플리케이션 구조, 라우팅, 컨트롤러, 서비스, 저장소, DTO, validation, error mapping을 구현한다.
- API 계약을 Frontend가 바로 소비할 수 있는 형태로 문서화하고, 변경 시 PM/Frontend/DevOps-QA에 영향도를 알린다.
- DB 접근은 authoritative schema의 테이블/제약/인덱스를 기준으로 작성한다.
- 인증 사용자 기준 접근 제어, 페이지네이션, 검색, 정렬, 빈 상태, 에러 응답을 서버에서 일관되게 처리한다.
- 테스트 fixture와 seed 데이터가 schema의 FK/code 제약을 만족하는지 검증한다.
- 구현 완료 전 관련 단위/통합/API 테스트와 schema smoke check를 실행한다.

## Owned Paths

프로젝트에 애플리케이션 코드가 추가될 때 Backend Agent가 기본 소유한다.

- `backend/**`
- `server/**`
- `src/main/**`
- `src/test/**`
- `app/**` 중 서버 런타임 코드
- `api/**` 중 backend API 명세/fixture
- `migrations/**`, `db/**`, `seed/**` 중 PM이 승인한 backend-owned DB 변경
- backend 관련 README 또는 실행 스크립트

이 계약 문서 자체의 현재 작성 범위는 `docs/lanes/BACKEND_LANE.md`이다.

## Forbidden Paths

- Frontend UI 구현 파일: `frontend/**`, `web/**`, `client/**`, 화면 컴포넌트/스타일 파일
- DevOps-QA 소유 파일: CI/CD, Docker, 배포, 모니터링, 테스트 오케스트레이션 설정
- PM 소유 기준 문서: `docs/REQUIREMENTS.md`, `docs/FUNCTIONAL_SPEC.md`, `docs/ROLE_MATRIX.md`
- 승인 없는 schema baseline 변경: `docs/revised_schema_mysql8.sql`
- 별도 공지 테이블, 예: `notices`, `notice_posts`, `notice_categories`
- 다른 lane이 편집 중인 파일 또는 명시적으로 배정받지 않은 파일

## Dependencies

### PM

- pilot 범위, 우선순위, 용어, board code, category 명칭을 확정한다.
- notice/free-board list의 필수 컬럼, 정렬 기준, 검색 범위, 권한 정책을 승인한다.
- schema 변경이 필요한 경우 변경 사유와 acceptance criteria를 명시한다.

### Frontend

- API 요청 파라미터, 응답 DTO, 날짜/카운트 표시 포맷, 빈 상태와 에러 상태를 합의한다.
- 페이지네이션 방식(page/size 또는 cursor), category filter, keyword search 사용 방식을 확정한다.
- mock 데이터가 필요하면 Backend가 제공하는 DTO fixture를 사용한다.

### DevOps-QA

- MySQL 8 실행 방식, migration/seed 적용 순서, 테스트 DB 초기화 방식을 제공한다.
- CI에서 실행할 backend test, schema validation, API smoke command를 등록한다.
- 환경 변수, profile, secret placeholder, test report 위치를 관리한다.

## Pilot Feature Scope

pilot은 notice list와 free-board list의 read path를 우선 구현한다.

- Notice list: 공지사항 게시판의 글 목록 조회, category filter, keyword search, pagination.
- Free-board list: 자유게시판 글 목록 조회, category filter, keyword search, pagination.
- 공통 모델: `boards.board_code`로 게시판을 구분하고, `board_categories`로 카테고리를 연결하며, `board_posts`에서 글 목록을 조회한다.
- 공지성 고정 글은 별도 테이블이 아니라 `board_posts.notice_yn`을 사용한다.
- list 범위에서 detail, write, edit, delete, comment, reaction mutation은 제외한다. 단, 목록 표시용 comment/reaction/attachment count가 필요하면 기존 `board_comments`, `board_post_reactions`, `board_post_attachments`를 aggregate로 조회한다.

## Backend Tasks

- board code 기반 게시판 조회 service를 만든다.
- board category 조회와 category filter를 구현한다.
- `board_posts` 목록 조회를 pagination, keyword search, 정렬 기준과 함께 구현한다.
- list DTO에는 최소 `id`, `boardCode`, `category`, `title`, `authorName`, `createdAt`, `viewCount`, `commentCount`, `reactionCount`, `bookmarkCount`, `hasAttachment`, `isPinned`를 포함한다.
- `commentCount`, `reactionCount`, `bookmarkCount`, `hasAttachment`는 목록 표시용 aggregate로 계산한다.
- 존재하지 않는 board/category, 권한 없음, 잘못된 page/size/search 입력을 표준 에러 응답으로 매핑한다.
- N+1 query를 피하고 board/category/author 표시값을 한 번의 명확한 query 또는 bounded query set으로 가져온다.
- 테스트 seed에는 `boards`, `board_categories`, `users`, `board_posts`와 필요한 `codes` FK 값을 포함한다.

## API Contract Responsibilities

Backend Agent는 API 계약 초안을 작성하고, Frontend와 합의 후 구현과 테스트를 맞춘다.

권장 endpoint:

```http
GET /api/boards/{boardCode}/posts?page=1&size=10&categoryId={id}&keyword={keyword}
```

권장 `boardCode`:

- `notice`
- `free`

권장 응답 shape:

```json
{
  "items": [
    {
      "id": 1,
      "boardCode": "notice",
      "category": {
        "id": 10,
        "name": "운영"
      },
      "title": "공지 제목",
      "authorName": "관리자",
      "createdAt": "2026-04-24T09:00:00+09:00",
      "viewCount": 0,
      "commentCount": 0,
      "reactionCount": 0,
      "bookmarkCount": 0,
      "hasAttachment": false,
      "isPinned": true
    }
  ],
  "page": {
    "page": 1,
    "size": 10,
    "totalItems": 1,
    "totalPages": 1
  }
}
```

Contract rules:

- `page`와 `size`의 default/max를 명시한다.
- `keyword`는 title/content 중 PM이 승인한 검색 범위에만 적용한다.
- 정렬은 기본 `notice_yn DESC`, `created_at DESC`, `board_post_id DESC`로 안정화한다.
- 인증 필요 여부와 접근 범위는 `boards.access_scope_code`와 ROLE_MATRIX 기준으로 검증한다.
- API 응답은 DB 컬럼명을 그대로 노출하지 않고 stable DTO 이름을 사용한다.

## DB / Schema Verification Responsibilities

- `docs/revised_schema_mysql8.sql`이 최종 기준이며, 구현 전후 테이블/컬럼명을 대조한다.
- 공지사항은 `boards` row와 `board_posts` row의 조합으로만 표현한다.
- 자유게시판도 같은 `boards`/`board_categories`/`board_posts` 모델을 사용한다.
- `board_posts.board_category_id`를 사용할 때 `(board_category_id, board_id)` FK 일관성을 보장한다.
- `codes` seed에는 `BOARD_GROUP`, `ACCESS_SCOPE`, 필요한 reaction/status code가 포함되어야 한다.
- schema 변경 요청이 생기면 Backend가 단독 변경하지 않고 PM 승인 및 DevOps-QA migration 검증을 거친다.
- 금지 검증: schema 또는 migration에 `CREATE TABLE notices` 계열이 없어야 한다.

## Acceptance Criteria

- notice/free-board list API가 동일한 공통 게시판 service를 사용한다.
- 별도 notices table 없이 `boards`, `board_categories`, `board_posts` 기반으로 동작한다.
- category filter, keyword search, pagination, stable sorting이 테스트로 증명된다.
- 빈 목록은 200과 빈 `items`로 응답한다.
- 잘못된 board code/category/page/size는 표준 error response로 응답한다.
- 인증/권한 정책은 ROLE_MATRIX와 `boards.access_scope_code` 기준을 벗어나지 않는다.
- Frontend가 합의한 DTO만 소비하면 화면 목록을 렌더링할 수 있다.
- 테스트 DB seed가 MySQL 8 FK/check 제약을 통과한다.

## Test Commands / Checks

프로젝트 stack이 확정되면 아래 항목을 CI와 로컬 검증에 맞게 구체화한다.

```bash
# backend unit/integration tests
./gradlew test
# or
./mvnw test
# or
npm test -- --runInBand
```

```bash
# schema smoke check against MySQL 8
mysql --version
mysql < docs/revised_schema_mysql8.sql
```

```bash
# no separate notices table
rg -n "CREATE TABLE .*notices|notice_posts|notice_categories" docs backend server src
```

Required checks:

- board list repository/service tests
- API controller/route tests for notice and free-board
- pagination boundary tests
- category mismatch test
- keyword empty/result/no-result tests
- schema load or migration smoke test on MySQL 8

## Handoff Checklist

- PM-approved board codes and category labels are documented.
- Frontend has endpoint, query params, response DTO, error DTO, and example payloads.
- DevOps-QA has seed/migration order, test command, required env vars, and smoke check command.
- Backend tests pass locally with fresh output.
- No schema drift from `docs/revised_schema_mysql8.sql`.
- No separate notices table or notice-specific persistence model was introduced.
- Known limitations and deferred items are listed in the lane handoff note.
