# Screen List

## 목적
SSAFY 클론의 화면과 상태를 정의한다. 이 문서는 PM이 lane별 구현 범위를 나누는 기준이다.

## 공통 화면 규칙
- 로그인 필요 상태를 고려한다.
- 목록 화면은 로딩, 빈 상태, 오류 상태를 가진다.
- 검색어와 필터가 바뀌면 page를 1로 초기화한다.
- legacy SSAFY URL은 참고 자료로만 사용한다.
- 화면 컴포넌트 파일은 500줄을 넘기지 않는다.

## 공통 레이아웃
### SCR-SHELL
관련 요구사항:
- `FR-MAIN-002`
- `FR-MAIN-004`

필수 UI:
- eduSSAFY 브랜드
- 글로벌 메뉴
- 모바일/협소 화면 대응 메뉴
- 사용자 요약 영역
- 로그아웃 또는 세션 만료 안내 placeholder

## 우선순위 1 화면
### SCR-AUTH-LOGIN 로그인
route:
```text
/login
```

API:
```text
POST /api/auth/login
```

필수 UI:
- 이메일 또는 아이디 입력
- 비밀번호 입력
- 아이디 저장 checkbox
- 로그인 버튼
- 비밀번호 찾기 링크
- 세션 만료 안내 메시지

### SCR-MAIN-DASHBOARD 메인 대시보드
route:
```text
/
```

API:
```text
GET /api/dashboard/summary
```

필수 UI:
- 사용자 이름, 캠퍼스, 기수, 트랙
- 출석 요약
- 레벨/경험치/장학포인트 요약
- 읽지 않은 알림 수
- 오늘의 커리큘럼, Quest, 설문 요약
- 주요 서비스 바로가기

### SCR-ATTENDANCE 출석현황
route:
```text
/mycampus/attendance
```

API:
```text
GET /api/attendance/records
```

필수 UI:
- 출석/지각/결석 요약
- 날짜별 출결 목록
- 소명 가능 상태 표시

### SCR-LEVEL 레벨/포인트
route:
```text
/mycampus/level
```

API:
```text
GET /api/dashboard/summary
```

필수 UI:
- 현재 레벨
- 경험치 progress
- 장학포인트
- 랭킹

### SCR-NOTICE-LIST 공지사항 목록
route:
```text
/help/notice
```

API:
```text
GET /api/boards/notice/categories
GET /api/boards/notice/posts
```

필수 UI:
- 카테고리 필터
- 검색어 입력
- 게시글 목록
- 등록일, 작성자, 조회수
- 빈 상태

### SCR-FREE-BOARD-LIST 자유게시판 목록
route:
```text
/community/free
```

API:
```text
GET /api/boards/free/categories
GET /api/boards/free/posts
```

필수 UI:
- 카테고리 필터
- 검색어 입력
- 글쓰기 placeholder
- 댓글/추천/첨부/찜 메타 표시
- 페이지네이션

### SCR-LEARNING-MATERIALS 학습자료 목록
route:
```text
/learning/materials
```

API:
```text
GET /api/learning/materials
```

필수 UI:
- 자료 유형 필터
- 검색어 입력
- 제목, 유형, 등록일, 조회수
- eBook/PDF 열기 placeholder

### SCR-QUEST-LIST Quest/평가 목록
route:
```text
/quest
```

API:
```text
GET /api/quests
```

필수 UI:
- Quest 제목
- 기간
- 진행/완료/채점완료 상태
- 상세 진입 placeholder

### SCR-SURVEY-LIST 설문 목록
route:
```text
/survey
```

API:
```text
GET /api/surveys
```

필수 UI:
- 설문 제목
- 필수 여부
- 기간
- 응답 상태

## 우선순위 2 화면
- `SCR-NOTIFICATIONS`: 알림함
- `SCR-CURRICULUM`: 주차별 커리큘럼
- `SCR-REPLAY`: 강의 다시보기
- `SCR-FAQ`: FAQ 목록
- `SCR-QNA`: 1:1 문의 목록
- `SCR-PROFILE-CHECK`: 회원정보 비밀번호 재확인

## 우선순위 3 화면
- 게시글 상세
- 공지 상세
- 학습자료 상세/PDF popup
- Quest 상세/응시
- 설문 상세/응답
- 회원정보 수정

## 완료 기준
- 우선순위 1 route가 모두 직접 진입 가능하다.
- 각 route가 API 로딩, 빈 상태, 오류 상태를 처리한다.
- 공통 레이아웃과 내비게이션이 모든 주요 화면에서 유지된다.
- API가 없어도 mock fallback 또는 seed 데이터로 화면 검증이 가능하다.
