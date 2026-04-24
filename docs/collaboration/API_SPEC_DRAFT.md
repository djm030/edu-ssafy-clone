# API Spec Draft

## 원칙
- legacy SSAFY URL은 캡처 근거로만 사용한다.
- 구현 API는 `/api` 아래 도메인 중심 route로 둔다.
- Runtime 데이터 기준은 MySQL 8 schema와 seed다.
- 우선순위 1에서는 인증을 실제 보안 기능이 아니라 데모 세션 계약으로 제한한다.
- 오류 응답은 모든 API에서 같은 형태를 쓴다.

## 공통 응답
### Error
```json
{
  "error": {
    "code": "INVALID_REQUEST",
    "message": "요청 값을 확인해 주세요."
  }
}
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

## 인증과 사용자
### POST /api/auth/login
데모 로그인을 수행한다.

Request:
```json
{
  "email": "student@ssafy.com",
  "password": "password"
}
```

Response 200:
```json
{
  "user": {
    "id": 1,
    "name": "김싸피",
    "email": "student@ssafy.com",
    "role": "learner",
    "campusName": "서울",
    "cohortName": "12기",
    "trackName": "Java"
  }
}
```

### GET /api/me
현재 데모 사용자 정보를 조회한다.

### POST /api/profile/password-check
회원정보 수정 전 비밀번호 재확인을 수행한다. 우선순위 1에서는 성공/실패 상태만 반환한다.

## 메인과 마이캠퍼스
### GET /api/dashboard/summary
메인 대시보드 위젯을 한 번에 조회한다.

Response 200:
```json
{
  "user": {
    "name": "김싸피",
    "campusName": "서울",
    "cohortName": "12기",
    "trackName": "Java"
  },
  "level": {
    "level": 5,
    "exp": 4200,
    "nextLevelExp": 5000,
    "scholarshipPoints": 85,
    "rank": 12
  },
  "attendance": {
    "present": 18,
    "late": 1,
    "absent": 0,
    "appealAvailable": true
  },
  "notifications": {
    "unreadCount": 3,
    "latest": []
  },
  "today": {
    "curriculumTitle": "Spring Boot REST API",
    "questTitle": "게시판 API 구현",
    "surveyTitle": "주간 만족도 조사"
  }
}
```

### GET /api/attendance/records
출석 현황 목록을 조회한다.

### GET /api/notifications
알림함 목록을 조회한다.

## 강의실과 학습
### GET /api/learning/curriculum
주차별 커리큘럼을 조회한다.

### GET /api/learning/replays
강의 다시보기 목록을 조회한다.

### GET /api/learning/materials
학습자료 목록을 조회한다.

Query:
| 이름 | 필수 | 설명 |
|---|---:|---|
| `keyword` | N | 제목 검색어 |
| `type` | N | `ebook`, `video`, `file`, `link` |
| `page` | N | 기본값 `1` |
| `size` | N | 기본값 `20` |

## Quest와 설문
### GET /api/quests
Quest/평가 목록을 조회한다.

### GET /api/surveys
설문 목록을 조회한다.

## 게시판
### BoardCode
```text
notice
free
faq
qna
```

### GET /api/boards/{boardCode}/categories
게시판 카테고리 목록을 조회한다.

### GET /api/boards/{boardCode}/posts
게시글 목록을 조회한다.

Query:
| 이름 | 필수 | 기본값 | 설명 |
|---|---:|---|---|
| `categoryId` | N | 없음 | 카테고리 필터 |
| `keyword` | N | 없음 | 제목/본문 검색어 |
| `page` | N | `1` | 1부터 시작 |
| `size` | N | `20` | 페이지 크기 |
| `sort` | N | `createdAt,desc` | 정렬 |

Response 200:
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

## 우선순위 1 Acceptance Criteria
- 로그인, 메인 대시보드, 출석, 레벨/포인트, 공지, 자유게시판, 학습자료, Quest, 설문 화면이 API와 연결된다.
- notice/free/faq/qna는 공통 board API를 재사용한다.
- 목록 API는 빈 응답, 검색, 필터, 페이지네이션 상태를 깨지지 않게 처리한다.
- 직접 작성 코드 파일은 500줄 이하로 유지한다.
