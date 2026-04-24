# API Spec Draft

## 원칙
- legacy SSAFY URL은 근거로만 사용한다.
- 구현 API는 `/api` 아래의 도메인 중심 route로 둔다.
- 공지사항과 자유게시판은 같은 게시판 API를 사용한다.
- DB 기준은 `docs/revised_schema_mysql8.sql`이다.

## 공통 타입
### BoardCode
```text
notice
free
```

### PageMeta
```json
{
  "page": 1,
  "size": 20,
  "totalItems": 0,
  "totalPages": 0
}
```

### BoardPostListItem
```json
{
  "id": 1,
  "boardCode": "notice",
  "category": {
    "id": 1,
    "name": "학습"
  },
  "title": "공지 제목",
  "authorName": "관리자",
  "createdAt": "2026-04-24T09:00:00+09:00",
  "viewCount": 0,
  "commentCount": 0,
  "reactionCount": 0,
  "bookmarkCount": 0,
  "hasAttachment": false,
  "isPinned": false
}
```

## GET /api/boards/{boardCode}/categories
게시판 카테고리 목록을 조회한다.

### Path Parameters
| 이름 | 필수 | 설명 |
|---|---:|---|
| `boardCode` | Y | `notice` 또는 `free` |

### Response 200
```json
{
  "items": [
    {
      "id": 1,
      "name": "학습",
      "sortOrder": 1
    }
  ]
}
```

### DB Mapping
- `boards.board_code`
- `board_categories.board_id`
- `board_categories.category_name`
- `board_categories.sort_order`

## GET /api/boards/{boardCode}/posts
게시글 목록을 조회한다.

### Path Parameters
| 이름 | 필수 | 설명 |
|---|---:|---|
| `boardCode` | Y | `notice` 또는 `free` |

### Query Parameters
| 이름 | 필수 | 기본값 | 설명 |
|---|---:|---|---|
| `categoryId` | N | 없음 | 카테고리 필터 |
| `keyword` | N | 없음 | 제목/본문 검색어 |
| `page` | N | `1` | 1부터 시작 |
| `size` | N | `20` | 페이지 크기 |
| `sort` | N | `createdAt,desc` | 정렬 |

### Response 200
```json
{
  "items": [
    {
      "id": 1,
      "boardCode": "free",
      "category": {
        "id": 3,
        "name": "자유"
      },
      "title": "게시글 제목",
      "authorName": "김싸피",
      "createdAt": "2026-04-24T09:00:00+09:00",
      "viewCount": 10,
      "commentCount": 2,
      "reactionCount": 1,
      "bookmarkCount": 0,
      "hasAttachment": true,
      "isPinned": false
    }
  ],
  "page": {
    "page": 1,
    "size": 20,
    "totalItems": 1,
    "totalPages": 1
  }
}
```

### Empty Response 200
```json
{
  "items": [],
  "page": {
    "page": 1,
    "size": 20,
    "totalItems": 0,
    "totalPages": 0
  }
}
```

### Error Shape
```json
{
  "error": {
    "code": "BOARD_NOT_FOUND",
    "message": "게시판을 찾을 수 없습니다."
  }
}
```

## Status Codes
| 상태 | 의미 |
|---:|---|
| 200 | 조회 성공 |
| 400 | 잘못된 page/size/sort |
| 401 | 인증 필요 |
| 403 | 접근 권한 없음 |
| 404 | boardCode 없음 |
| 500 | 서버 오류 |

## DB Mapping
### 목록 기본
- `boards`
- `board_categories`
- `board_posts`
- `users`

### 첨부 여부
- `board_post_attachments`

### 댓글 수
- `board_comments`

### 반응 수
- `board_post_reactions`

## Backend Acceptance Criteria
- `notice`와 `free`가 같은 handler/service 구조를 공유한다.
- category filter, keyword search, page/size가 동시에 동작한다.
- 없는 boardCode는 404로 응답한다.
- page/size가 유효하지 않으면 400으로 응답한다.

## Frontend Acceptance Criteria
- `items.length === 0`이면 빈 상태를 표시한다.
- `401`이면 로그인 필요 상태로 이동 또는 안내한다.
- `403`이면 접근 제한 메시지를 표시한다.
- `notice` 화면에서는 댓글/반응 UI를 제한할 수 있다.
- `free` 화면에서는 댓글/반응/첨부 메타를 표시한다.

## Open Questions
- 인증 방식과 세션 쿠키/API 토큰 방식은 앱 프레임워크 선택 후 확정한다.
- `bookmarkCount`를 `board_post_reactions`로 볼지 별도 모델이 필요한지는 상세 구현 전 확인한다.
- `notice_yn`을 상단 고정 게시글로 사용할지 별도 sort rule로 둘지 seed 이후 확정한다.
