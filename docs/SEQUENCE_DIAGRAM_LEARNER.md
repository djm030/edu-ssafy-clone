# SSAFY 상세 시퀀스 다이어그램 - 학습자

## 작성 원칙
- **기능 1개 = 시퀀스 다이어그램 1개**로 분리
- 화면(UI), 애플리케이션 서비스, DB, 파일 저장소/뷰어를 구분
- 현재 확보된 캡처와 기능명세를 기준으로 작성
- 미확보 상세(Quest 상세, Survey 상세 일부)는 현재 요구사항 수준에서 보수적으로 표현

## 공통 참여자 표기
- `Web UI`: 브라우저 화면
- `Auth API`: 인증/인가 처리
- `App API`: 도메인별 애플리케이션 서비스
- `MySQL DB`: 정규화 스키마 기준 DB
- `File Storage`: 첨부/프로필 이미지 저장소
- `Viewer`: eBook/PDF 새 창 뷰어

---

## 1) 로그인 후 세션 생성 및 메인 진입

```mermaid
sequenceDiagram
    actor Learner as 학습자
    participant UI as Web UI
    participant Auth as Auth API
    participant DB as MySQL DB
    participant Session as Session Store
    participant Main as Main API

    Learner->>UI: 아이디/비밀번호 입력
    UI->>Auth: POST /login
    Auth->>DB: users 조회(email/학번 기준)
    DB-->>Auth: 사용자 계정/상태/비밀번호 해시 반환
    Auth->>Auth: 비밀번호 검증, 상태 확인

    alt 로그인 성공
        Auth->>Session: 세션/인증 토큰 저장
        Session-->>Auth: 세션 키 반환
        Auth-->>UI: 로그인 성공 + 세션 쿠키
        UI->>Main: GET /main
        Main->>DB: 사용자 요약 정보 조회
        DB-->>Main: 대시보드 요약 데이터
        Main-->>UI: 메인 데이터 반환
        UI-->>Learner: 메인/대시보드 표시
    else 로그인 실패
        Auth-->>UI: 오류 메시지 반환
        UI-->>Learner: 로그인 실패 안내
    end
```

---

## 2) 메인 대시보드 조회(알림/출결/레벨 위젯)

```mermaid
sequenceDiagram
    actor Learner as 학습자
    participant UI as Web UI
    participant Main as Main API
    participant DB as MySQL DB

    Learner->>UI: 메인 페이지 진입
    UI->>Main: GET /main/dashboard-summary
    Main->>DB: user_level_statuses 조회
    DB-->>Main: 레벨/EXP/장학포인트
    Main->>DB: attendance_records 최근 현황 조회
    DB-->>Main: 출결 요약
    Main->>DB: notification_recipients + notifications 조회
    DB-->>Main: 최근 알림 목록
    Main-->>UI: 대시보드 위젯 데이터 조합 반환
    UI-->>Learner: 개인화 메인 화면 표시
```

---

## 3) 알림함 조회 및 읽음 처리

```mermaid
sequenceDiagram
    actor Learner as 학습자
    participant UI as Web UI
    participant Notify as Notification API
    participant DB as MySQL DB

    Learner->>UI: 알림함 열기
    UI->>Notify: GET /notifications
    Notify->>DB: notification_recipients 조회(user_id 기준)
    DB-->>Notify: 수신 상태 목록
    Notify->>DB: notifications 본문/제목 조회
    DB-->>Notify: 알림 본문 데이터
    Notify-->>UI: 알림 목록 반환
    UI-->>Learner: 알림함 표시

    Learner->>UI: 알림 1건 선택
    UI->>Notify: PATCH /notifications/{id}/read
    Notify->>DB: notification_recipients.read_at 업데이트
    DB-->>Notify: 업데이트 성공
    Notify-->>UI: 읽음 처리 결과
    UI-->>Learner: 읽음 상태 반영
```

---

## 4) 출결 현황 조회 및 소명서 진입

```mermaid
sequenceDiagram
    actor Learner as 학습자
    participant UI as Web UI
    participant Attendance as Attendance API
    participant DB as MySQL DB

    Learner->>UI: 출결 현황 메뉴 진입
    UI->>Attendance: GET /attendance
    Attendance->>DB: attendance_records 조회(user_id, 기간 기준)
    DB-->>Attendance: 출결 목록/상태 반환
    Attendance-->>UI: 출결 현황 데이터 반환
    UI-->>Learner: 출결 현황 표시

    opt 소명서 작성 진입
        Learner->>UI: 소명 요청 클릭
        UI->>Attendance: POST /attendance/appeals/draft
        Attendance->>DB: attendance_records 대상 검증
        DB-->>Attendance: 대상 출결 반환
        Attendance->>DB: attendance_appeals 초안 생성
        DB-->>Attendance: 초안 ID 반환
        Attendance-->>UI: 소명서 작성 화면 데이터
        UI-->>Learner: 소명서 작성 폼 표시
    end
```

---

## 5) 커리큘럼 조회

```mermaid
sequenceDiagram
    actor Learner as 학습자
    participant UI as Web UI
    participant Curriculum as Curriculum API
    participant DB as MySQL DB

    Learner->>UI: 주차별 커리큘럼 메뉴 진입
    UI->>Curriculum: GET /curriculum?termId=&weekNo=
    Curriculum->>DB: terms 조회
    DB-->>Curriculum: 학기 정보
    Curriculum->>DB: curriculum_schedules 조회(term/track/week 기준)
    DB-->>Curriculum: 주차별 수업 일정
    Curriculum-->>UI: 커리큘럼 목록 반환
    UI-->>Learner: 주차별 커리큘럼 표시
```

---

## 6) 학습자료 목록 조회 → 상세 조회

```mermaid
sequenceDiagram
    actor Learner as 학습자
    participant UI as Web UI
    participant Material as Learning Material API
    participant DB as MySQL DB

    Learner->>UI: 학습자료 메뉴 진입
    UI->>Material: GET /learning-materials
    Material->>DB: learning_materials 조회(트랙/카테고리/검색조건)
    DB-->>Material: 학습자료 목록
    Material-->>UI: 목록 반환
    UI-->>Learner: 학습자료 목록 표시

    Learner->>UI: 학습자료 상세 선택
    UI->>Material: GET /learning-materials/{contentId}
    Material->>DB: learning_materials 상세 조회
    DB-->>Material: 학습자료 본문/메타데이터
    Material->>DB: learning_material_resources 조회
    DB-->>Material: eBook/PDF/외부링크 리소스 목록
    Material-->>UI: 상세 + 리소스 반환
    UI-->>Learner: 상세 화면 표시
```

---

## 7) 학습자료 상세 → eBook/PDF 새 창 열기

```mermaid
sequenceDiagram
    actor Learner as 학습자
    participant UI as Web UI
    participant Resource as Learning Resource API
    participant DB as MySQL DB
    participant Storage as File Storage
    participant Viewer as eBook/PDF Viewer

    Learner->>UI: eBook/PDF 열기 클릭
    UI->>Resource: GET /learning-material-resources/{resourceId}/launch
    Resource->>DB: learning_material_resources 조회
    DB-->>Resource: launch_mode_code, target_url, external_resource_id
    Resource->>DB: learning_material_resource_attachments 조회
    DB-->>Resource: attachment_id 목록
    Resource->>DB: attachments 조회
    DB-->>Resource: 파일 경로/메타데이터

    alt 파일 저장소 기반 리소스
        Resource->>Storage: 파일/압축해제 경로 확인
        Storage-->>Resource: 실제 접근 URL 반환
    end

    Resource-->>UI: 뷰어 URL 반환
    UI->>Viewer: 새 창/팝업 오픈
    Viewer-->>Learner: eBook/PDF 렌더링
```

---

## 8) 자유게시판 목록 → 상세 → 댓글 작성

```mermaid
sequenceDiagram
    actor Learner as 학습자
    participant UI as Web UI
    participant Board as Board API
    participant Comment as Comment API
    participant DB as MySQL DB

    Learner->>UI: 자유게시판 진입
    UI->>Board: GET /boards/free/posts
    Board->>DB: board_posts 조회(board_id, 검색/정렬 조건)
    DB-->>Board: 게시글 목록
    Board-->>UI: 목록 반환
    UI-->>Learner: 게시글 목록 표시

    Learner->>UI: 게시글 상세 선택
    UI->>Board: GET /board-posts/{postId}
    Board->>DB: board_posts 상세 조회
    DB-->>Board: 본문/작성자/조회수/메타
    Board->>DB: board_post_attachments + attachments 조회
    DB-->>Board: 첨부 목록
    Board->>DB: board_comments 조회
    DB-->>Board: 댓글 목록
    Board->>DB: board_post_reactions 집계 조회
    DB-->>Board: 좋아요/찜 수
    Board-->>UI: 상세 데이터 반환
    UI-->>Learner: 상세 화면 표시

    Learner->>UI: 댓글 입력 후 등록
    UI->>Comment: POST /board-posts/{postId}/comments
    Comment->>DB: board_comments INSERT
    DB-->>Comment: 댓글 저장 성공
    Comment-->>UI: 등록 성공
    UI->>Board: GET /board-posts/{postId}
    Board->>DB: 댓글 포함 상세 재조회
    DB-->>Board: 갱신된 상세 데이터
    Board-->>UI: 재조회 결과 반환
    UI-->>Learner: 댓글 반영 화면 표시
```

---

## 9) 자유게시판 반응(좋아요/찜) 처리

```mermaid
sequenceDiagram
    actor Learner as 학습자
    participant UI as Web UI
    participant Reaction as Reaction API
    participant DB as MySQL DB

    Learner->>UI: 좋아요/찜 클릭
    UI->>Reaction: POST /board-posts/{postId}/reactions
    Reaction->>DB: board_post_reactions 기존 반응 존재 여부 조회
    DB-->>Reaction: 기존 반응 결과

    alt 기존 반응 없음
        Reaction->>DB: board_post_reactions INSERT
        DB-->>Reaction: 저장 성공
    else 기존 반응 있음
        Reaction->>DB: board_post_reactions DELETE
        DB-->>Reaction: 취소 성공
    end

    Reaction->>DB: 반응 수 재집계 조회
    DB-->>Reaction: 최신 반응 수
    Reaction-->>UI: 반응 상태/카운트 반환
    UI-->>Learner: 화면 카운트 갱신
```

---

## 10) 공지사항 목록 → 상세 조회

```mermaid
sequenceDiagram
    actor Learner as 학습자
    participant UI as Web UI
    participant Notice as Notice API
    participant DB as MySQL DB

    Learner->>UI: 공지사항 메뉴 진입
    UI->>Notice: GET /boards/notice/posts
    Notice->>DB: board_posts 조회(board_code = NOTICE)
    DB-->>Notice: 공지 목록
    Notice-->>UI: 목록 반환
    UI-->>Learner: 공지 목록 표시

    Learner->>UI: 공지 상세 선택
    UI->>Notice: GET /board-posts/{postId}
    Notice->>DB: board_posts 상세 조회
    DB-->>Notice: 제목/본문/작성자/등록일
    Notice->>DB: board_post_attachments + attachments 조회
    DB-->>Notice: 첨부 목록
    Notice-->>UI: 공지 상세 반환
    UI-->>Learner: 공지 상세 표시
```

---

## 11) 1:1 문의 등록

```mermaid
sequenceDiagram
    actor Learner as 학습자
    participant UI as Web UI
    participant Ticket as Support Ticket API
    participant DB as MySQL DB
    participant Storage as File Storage

    Learner->>UI: 1:1 문의 작성 진입
    UI-->>Learner: 문의 작성 폼 표시

    opt 첨부파일 업로드
        Learner->>UI: 첨부파일 선택
        UI->>Storage: 파일 업로드
        Storage-->>UI: 저장 경로/파일 메타데이터
    end

    Learner->>UI: 제목/본문 입력 후 등록
    UI->>Ticket: POST /support-tickets
    Ticket->>DB: support_tickets INSERT
    DB-->>Ticket: 문의 ID 반환
    Ticket->>DB: attachments / support_ticket_attachments INSERT
    DB-->>Ticket: 첨부 연결 성공
    Ticket-->>UI: 등록 성공
    UI-->>Learner: 문의 등록 완료 화면 표시
```

---

## 12) 회원정보 수정

```mermaid
sequenceDiagram
    actor Learner as 학습자
    participant UI as Web UI
    participant Auth as Auth API
    participant Profile as Profile API
    participant DB as MySQL DB
    participant Storage as File Storage

    Learner->>UI: 회원정보 수정 메뉴 진입
    UI-->>Learner: 비밀번호 재확인 화면 표시
    Learner->>UI: 비밀번호 입력
    UI->>Auth: POST /profile/check-password
    Auth->>DB: users 조회
    DB-->>Auth: 비밀번호 해시
    Auth-->>UI: 확인 성공

    UI->>Profile: GET /profile
    Profile->>DB: users + user_profiles 조회
    DB-->>Profile: 프로필 기본 정보
    Profile->>DB: user_profile_attachments + attachments 조회
    DB-->>Profile: 프로필 이미지 정보
    Profile-->>UI: 회원정보 수정 폼 데이터 반환
    UI-->>Learner: 수정 폼 표시

    opt 프로필 이미지 변경
        Learner->>UI: 프로필 이미지 업로드
        UI->>Storage: 파일 업로드
        Storage-->>UI: 저장 성공/파일 메타데이터
    end

    Learner->>UI: 주소/연락처/수신동의 수정 후 저장
    UI->>Profile: PUT /profile
    Profile->>DB: user_profiles UPDATE
    DB-->>Profile: 수정 성공
    Profile->>DB: user_profile_attachments 갱신
    DB-->>Profile: 이미지 연결 반영
    Profile-->>UI: 저장 성공
    UI-->>Learner: 수정 완료 메시지 표시
```

