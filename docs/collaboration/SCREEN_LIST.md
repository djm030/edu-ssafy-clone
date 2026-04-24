# Screen List

## 목적
파일럿 구현에서 필요한 화면과 상태를 정의한다.

## 공통 화면 규칙
- 모든 화면은 로그인 필요 상태를 고려한다.
- 목록 화면은 로딩, 빈 상태, 오류 상태를 가진다.
- 검색/필터 변경 시 page는 1로 초기화한다.
- legacy SSAFY URL은 참고 자료로만 사용한다.

## 파일럿 화면
### SCR-NOTICE-LIST 공지사항 목록
관련 요구사항:
- `FR-NOTI-001`
- `FS-NOTI-001`

참고 캡처:
- `ssafy_pages/07_notice/*notice__list*.html`
- `ssafy_pages/07_notice/*notice__list*.png`
- `ssafy_pages/07_notice/*notice__list*.json`

내부 route 후보:
```text
/help/notice
```

API:
```text
GET /api/boards/notice/categories
GET /api/boards/notice/posts
```

필수 UI:
- 제목 영역
- 카테고리 필터
- 검색어 입력
- 검색 버튼
- 게시글 목록
- 등록일
- 작성자
- 조회수
- 페이지네이션
- 빈 상태

선택 UI:
- 첨부 아이콘
- 중요/고정 표시

제한:
- 댓글/추천/찜 UI는 파일럿에서 필수 아님
- 상세 진입은 route placeholder만 허용

### SCR-FREE-BOARD-LIST 자유게시판 목록
관련 요구사항:
- `FR-COMM-001`
- `FS-COMM-001`
- `FR-COMM-002`
- `FS-COMM-002`

참고 캡처:
- `ssafy_pages/06_community/*free__list*.html`
- `ssafy_pages/06_community/*free__list*.png`
- `ssafy_pages/06_community/*free__list*.json`

내부 route 후보:
```text
/community/free
```

API:
```text
GET /api/boards/free/categories
GET /api/boards/free/posts
```

필수 UI:
- 제목 영역
- 카테고리 필터
- 검색어 입력
- 검색 버튼
- 게시글 목록
- 작성자
- 작성일시
- 조회수
- 댓글 수
- 추천 수
- 첨부 여부
- 페이지네이션
- 빈 상태

선택 UI:
- 글쓰기 버튼
- 찜 수

제한:
- 글쓰기 기능은 파일럿에서 route placeholder만 허용
- 상세 본문/댓글 영역은 파일럿 범위 밖

## 공통 상태
### Loading
- 목록 요청 중 skeleton 또는 loading text를 표시한다.

### Empty
- `items: []` 응답이면 "게시글이 없습니다." 계열 메시지를 표시한다.

### Error
- 400: 검색 조건 또는 페이지 조건 오류
- 401: 로그인 필요
- 403: 접근 권한 없음
- 404: 게시판 없음
- 500: 서버 오류

## 화면 완료 기준
- API 응답 없이 mock data로도 렌더링 가능하다.
- API 연동 후 notice/free가 같은 목록 컴포넌트 또는 같은 상태 모델을 공유한다.
- 검색/카테고리/페이지 변경이 API query와 연결된다.
- 빈 상태와 오류 상태가 깨지지 않는다.

## 후속 화면
파일럿 통과 후 다음 우선순위 1 화면을 같은 방식으로 확장한다.

- 로그인
- 메인 대시보드
- 출석현황
- 레벨/포인트
- 학습자료 목록
- Quest/평가 목록
- 설문 목록
