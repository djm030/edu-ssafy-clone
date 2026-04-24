# SSAFY 상세 시퀀스 다이어그램 - 관리자

## 작성 원칙
- 관리자 기능도 **기능 단위로 분리**
- 단순 조회보다 **등록/수정/답변/발송 같은 운영 행위 중심**으로 작성
- UI, API, DB, 파일 저장소를 함께 표현

---

## 1) 관리자 로그인 및 운영 메뉴 진입

```mermaid
sequenceDiagram
    actor Admin as 관리자
    participant UI as Admin Web UI
    participant Auth as Auth API
    participant DB as MySQL DB
    participant Session as Session Store

    Admin->>UI: 관리자 계정 입력
    UI->>Auth: POST /admin/login
    Auth->>DB: users 조회
    DB-->>Auth: 사용자/역할/상태 정보
    Auth->>Auth: 비밀번호 검증 + 관리자 권한 확인

    alt 관리자 인증 성공
        Auth->>Session: 관리자 세션 저장
        Session-->>Auth: 세션 키 반환
        Auth-->>UI: 로그인 성공
        UI-->>Admin: 운영 메뉴 표시
    else 권한 없음 또는 실패
        Auth-->>UI: 접근 거부/실패 응답
        UI-->>Admin: 오류 메시지 표시
    end
```

---

## 2) 공지 등록

```mermaid
sequenceDiagram
    actor Admin as 관리자
    participant UI as Admin Web UI
    participant Notice as Notice API
    participant DB as MySQL DB
    participant Storage as File Storage

    Admin->>UI: 공지 작성 화면 진입
    UI-->>Admin: 작성 폼 표시

    opt 첨부파일 업로드
        Admin->>UI: 파일 선택
        UI->>Storage: 파일 업로드
        Storage-->>UI: 저장 경로/메타데이터 반환
    end

    Admin->>UI: 제목/본문 입력 후 등록
    UI->>Notice: POST /admin/notices
    Notice->>DB: boards 조회(NOTICE 게시판 식별)
    DB-->>Notice: board_id 반환
    Notice->>DB: board_posts INSERT(notice_yn = true)
    DB-->>Notice: post_id 반환
    Notice->>DB: attachments INSERT
    DB-->>Notice: attachment_id 반환
    Notice->>DB: board_post_attachments INSERT
    DB-->>Notice: 첨부 연결 성공
    Notice-->>UI: 등록 성공
    UI-->>Admin: 공지 등록 완료 화면 표시
```

---

## 3) 공지 수정

```mermaid
sequenceDiagram
    actor Admin as 관리자
    participant UI as Admin Web UI
    participant Notice as Notice API
    participant DB as MySQL DB
    participant Storage as File Storage

    Admin->>UI: 기존 공지 상세 진입
    UI->>Notice: GET /admin/notices/{postId}
    Notice->>DB: board_posts 조회
    DB-->>Notice: 기존 제목/본문
    Notice->>DB: board_post_attachments + attachments 조회
    DB-->>Notice: 기존 첨부 목록
    Notice-->>UI: 수정 폼 데이터 반환
    UI-->>Admin: 수정 폼 표시

    opt 첨부파일 추가/교체
        Admin->>UI: 신규 파일 업로드
        UI->>Storage: 파일 업로드
        Storage-->>UI: 파일 메타데이터
    end

    Admin->>UI: 수정 내용 저장
    UI->>Notice: PUT /admin/notices/{postId}
    Notice->>DB: board_posts UPDATE
    DB-->>Notice: 본문 수정 성공
    Notice->>DB: board_post_attachments 추가/삭제 반영
    DB-->>Notice: 첨부 반영 성공
    Notice-->>UI: 수정 성공
    UI-->>Admin: 수정 완료 표시
```

---

## 4) 게시글 관리(상세 조회 및 상태 조치)

```mermaid
sequenceDiagram
    actor Admin as 관리자
    participant UI as Admin Web UI
    participant Board as Board API
    participant DB as MySQL DB

    Admin->>UI: 게시글 관리 메뉴 진입
    UI->>Board: GET /admin/boards/{boardCode}/posts
    Board->>DB: board_posts 목록 조회
    DB-->>Board: 게시글 목록
    Board-->>UI: 목록 반환
    UI-->>Admin: 관리 목록 표시

    Admin->>UI: 게시글 상세 선택
    UI->>Board: GET /admin/board-posts/{postId}
    Board->>DB: board_posts 상세 조회
    DB-->>Board: 본문/작성자/조회수
    Board->>DB: board_comments 조회
    DB-->>Board: 댓글 목록
    Board->>DB: board_post_reactions 집계 조회
    DB-->>Board: 반응 현황
    Board-->>UI: 상세 관리 정보 반환
    UI-->>Admin: 상세 관리 화면 표시

    opt 게시글 수정 또는 상태 변경
        Admin->>UI: 관리 액션 실행
        UI->>Board: PATCH /admin/board-posts/{postId}
        Board->>DB: board_posts UPDATE
        DB-->>Board: 처리 성공
        Board-->>UI: 처리 결과 반환
        UI-->>Admin: 결과 표시
    end
```

---

## 5) 1:1 문의 조회 및 메시지 답변 등록

```mermaid
sequenceDiagram
    actor Admin as 관리자
    participant UI as Admin Web UI
    participant Ticket as Support Ticket API
    participant Notify as Notification API
    participant DB as MySQL DB

    Admin->>UI: 1:1 문의 관리 메뉴 진입
    UI->>Ticket: GET /admin/support-tickets
    Ticket->>DB: support_tickets 목록 조회
    DB-->>Ticket: 문의 목록
    Ticket-->>UI: 목록 반환
    UI-->>Admin: 문의 목록 표시

    Admin->>UI: 문의 상세 선택
    UI->>Ticket: GET /admin/support-tickets/{ticketId}
    Ticket->>DB: support_tickets 상세 조회
    DB-->>Ticket: 문의 상태/기본 정보
    Ticket->>DB: support_ticket_messages 조회(order by created_at)
    DB-->>Ticket: 사용자 메시지/운영 답변/내부 메모 스레드
    Ticket->>DB: support_ticket_message_attachments + attachments 조회
    DB-->>Ticket: 메시지별 첨부 목록
    Ticket-->>UI: 상세 반환
    UI-->>Admin: 스레드 화면 표시

    Admin->>UI: 답변 또는 내부 메모 입력 후 등록
    UI->>Ticket: POST /admin/support-tickets/{ticketId}/messages
    Ticket->>DB: support_ticket_messages INSERT(message_type_code = admin_reply or internal_note)
    DB-->>Ticket: 메시지 저장 성공
    Ticket->>DB: support_tickets.status_code UPDATE
    DB-->>Ticket: 상태 변경 성공

    opt 사용자에게 알림 발송
        Ticket->>Notify: 문의 답변 알림 발송 요청
        Notify->>DB: notifications INSERT
        DB-->>Notify: notification_id 반환
        Notify->>DB: notification_recipients INSERT
        DB-->>Notify: 수신자 연결 성공
        Notify-->>Ticket: 발송 성공
    end

    Ticket-->>UI: 메시지 등록 성공
    UI-->>Admin: 갱신된 문의 스레드 표시
```

---

## 6) 사용자 대상 알림 발송

```mermaid
sequenceDiagram
    actor Admin as 관리자
    participant UI as Admin Web UI
    participant Notify as Notification API
    participant User as User API
    participant DB as MySQL DB

    Admin->>UI: 알림 발송 메뉴 진입
    Admin->>UI: 수신 조건/제목/본문 입력
    UI->>User: GET /admin/users?filter=
    User->>DB: users + 소속/트랙 기준 대상자 조회
    DB-->>User: 수신 대상 목록
    User-->>UI: 대상자 반환

    Admin->>UI: 발송 실행
    UI->>Notify: POST /admin/notifications
    Notify->>DB: notifications INSERT
    DB-->>Notify: notification_id 반환
    loop 대상자별 수신 행 생성
        Notify->>DB: notification_recipients INSERT
        DB-->>Notify: 수신자 행 저장
    end
    Notify-->>UI: 발송 성공
    UI-->>Admin: 발송 완료 메시지 표시
```

---

## 7) FAQ/공지 목록 운영 조회

```mermaid
sequenceDiagram
    actor Admin as 관리자
    participant UI as Admin Web UI
    participant Board as Board API
    participant DB as MySQL DB

    Admin->>UI: 공지/FAQ 관리 목록 진입
    UI->>Board: GET /admin/boards/{boardCode}/posts
    Board->>DB: boards 조회
    DB-->>Board: board_id
    Board->>DB: board_posts 목록 조회
    DB-->>Board: 게시글 목록
    Board-->>UI: 관리 목록 반환
    UI-->>Admin: 공지/FAQ 운영 목록 표시
```

