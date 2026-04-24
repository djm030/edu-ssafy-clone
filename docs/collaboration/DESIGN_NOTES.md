# Design Notes

## 기준 자료
- 요구사항: `docs/REQUIREMENTS.md`
- 기능 명세: `docs/FUNCTIONAL_SPEC.md`
- ERD 설명: `docs/ERD.md`
- 최신 SQL 기준: `docs/revised_schema_mysql8.sql`
- 화면 캡처 목록: `ssafy_pages/capture-manifest.json`
- 추가 상세 캡처: `ssafy_pages/targeted-capture-summary.json`

## 현재 상태
- ERD와 MySQL 8 SQL 초안은 작성되어 있다.
- 문서 인코딩은 UTF-8로 확인됐다.
- SQL은 아직 실제 MySQL 8에서 실행 검증되지 않았다.
- 앱 프레임워크와 런타임 구조는 아직 확정되지 않았다.

## 파일럿 도메인
게시판 목록 공통 모델을 첫 파일럿으로 사용한다.

대상 기능:
- `FR-NOTI-001` 공지사항 목록 조회
- `FS-NOTI-001` 공지사항 목록 조회
- `FR-COMM-001` 열린 게시판 목록 조회
- `FS-COMM-001` 자유게시판 목록 조회
- `FR-COMM-002` 게시글 메타데이터 표시
- `FS-COMM-002` 게시글 메타정보 표시

## 데이터 모델 결정
공지사항과 자유게시판은 별도 테이블로 분리하지 않는다.

사용 테이블:
- `boards`
- `board_categories`
- `board_posts`
- `board_post_attachments`
- `board_comments`
- `board_post_reactions`
- `users`
- `attachments`

공지사항은 `boards.board_group_code = 'notice'` 또는 `boards.board_code = 'notice'` 형태의 코드/보드 구분으로 표현한다.

자유게시판은 `boards.board_group_code = 'community'` 또는 `boards.board_code = 'free'` 형태의 코드/보드 구분으로 표현한다.

정확한 code value는 seed 설계 단계에서 `codes`와 함께 확정한다.

## API 설계 방향
legacy URL 예시는 근거로만 사용한다.

근거 URL:
- `/edu/board/notice/list.do`
- `/edu/board/free/list.do`
- `/edu/board/notice/detail.do`
- `/edu/board/free/detail.do`

구현 API는 도메인 기준으로 둔다.

```text
GET /api/boards/{boardCode}/posts
GET /api/boards/{boardCode}/categories
```

초기 `boardCode`:
- `notice`
- `free`

## UI 설계 방향
목록 화면은 공통 패턴을 갖는다.

- 카테고리 필터
- 키워드 검색
- 페이지네이션
- 결과 없음 상태
- 로딩 상태
- 접근 실패/세션 만료 상태

공지사항과 자유게시판의 차이는 반응/댓글/작성 권한이다.

- 공지사항: 조회 중심, 댓글/반응 제한 가능
- 자유게시판: 작성 진입, 댓글/추천/첨부 메타 표시 가능

## 검증 방향
초기 검증은 세 단계로 나눈다.

1. DB: MySQL 8에서 `docs/revised_schema_mysql8.sql` 실행 가능 여부 확인
2. API: 게시판 목록 API가 공통 응답 계약을 만족하는지 확인
3. UI: 공지/자유게시판 목록 화면이 검색, 필터, 빈 상태, 페이지네이션을 표시하는지 확인

## 보류 사항
- 앱 프레임워크가 정해지지 않아 실제 구현 경로는 아직 placeholder다.
- SQL 실행 검증 전까지 schema compatibility 리스크가 남는다.
- Quest 상세, Survey 상세, eBook/PDF 내부 뷰어는 파일럿 범위 밖이다.

## Decision Log
- 2026-04-24: 파일럿 도메인을 게시판 목록 공통 모델로 확정했다.
- 2026-04-24: `docs/revised_schema_mysql8.sql`을 구현 기준 schema로 채택했다.
