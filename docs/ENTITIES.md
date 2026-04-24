# SSAFY 엔티티 초안

## 문서 목적
- 현재까지 수집된 대표 페이지 캡처/HTML/JSON만 근거로 **도메인 엔티티 후보**를 정리한다.
- 이후 기능요구사항서와 클론 코딩용 데이터 모델 설계의 출발점으로 사용한다.

## 근거 범위
- 기준 산출물: `../ssafy_pages/capture-manifest.json`
- 대표 캡처 22건 기준
- 아직 미확보된 상세 화면은 2건(Quest 상세, Survey 상세)이며, 본 문서에는 **추론 최소화** 원칙으로 반영하지 않았거나 필요한 경우 `추론`으로 명시했다.
- 학습자료 eBook/PDF 내부 뷰어는 진입 흐름까지는 확인됐으나, 내부 렌더링 화면은 후속 검증 대상으로 남아 있다.

## 표기 규칙
- `확인`: 캡처된 화면/폼/텍스트에서 직접 확인됨
- `추론`: 상세 화면 미확보 등으로 인해 보수적으로 추론한 항목

---

## 1. 핵심 공통 엔티티

### 1.1 User
SSAFY 사용자(학생/선생/운영자/관리자 포함) 기본 주체.

| 속성 | 상태 | 근거 |
|---|---|---|
| user_id / login_id | 확인 | 로그인 화면 `userId`, 비밀번호 찾기 화면 `아이디` |
| password | 확인 | 로그인/비밀번호 확인 화면 `userPwd`, `currentPwd` |
| learner_no | 확인 | 상단 헤더의 학번/식별번호 노출 |
| name | 확인 | 상단 헤더 `백관열님` |
| role | 갱신 | revised schema 기준 전역 역할은 `student`, `instructor`, `manager`, `admin` 4종으로 관리한다. |
| email | 추론 | 로그인 ID가 이메일 형식이며 비밀번호 찾기 흐름이 이메일 기반 안내 |

### 1.2 UserProfile
회원정보/개인정보 조회 및 수정 대상.

| 속성 | 상태 | 근거 |
|---|---|---|
| user_id | 확인 | 회원정보 진입 전 비밀번호 재확인 필요 |
| current_password_check | 확인 | `userPwdCheckForm.do`, `currentPwd` |
| profile_image | 확인 | 프로필 사진 업로드/삭제 UI, `LMS.MEMBER.PHOTO` |
| email | 확인 | 회원정보 수정 화면 `email` |
| zip_code | 확인 | `zipCd`, `zipCode` |
| address | 확인 | 기본주소/상세주소 입력 영역 |
| mobile_phone | 확인 | `indvPhone1`, `indvPhone2`, hidden `indvPhone` |
| emergency_phone | 확인 | `emrgncyPhone1`, `emrgncyPhone2`, hidden `emrgncyPhone` |
| marketing_opt_in | 확인 | 광고성 정보 수집 및 수신 동의 Y/N |

### 1.3 ClassMembership
사용자가 어떤 기수/캠퍼스/반/트랙에 속하는지 표현.

| 속성 | 상태 | 근거 |
|---|---|---|
| user_id | 확인 | 우리반 보기 화면의 학생 목록 |
| cohort / batch | 확인 | `15기` 다수 노출 |
| campus | 확인 | `서울` 표기 |
| class_name | 확인 | `서울14반` 표기 |
| track | 확인 | Java 전공, Python, Mobile, Data 등 여러 트랙 표기 |

---

## 2. 마이캠퍼스 / 대시보드 계열

### 2.1 LevelPointSummary
사용자의 레벨, 경험치, 장학포인트 현황.

| 속성 | 상태 | 근거 |
|---|---|---|
| user_id | 확인 | 개인별 레벨 화면 |
| level | 확인 | `Lv.3`, Bronze 등 |
| exp | 확인 | `1,138 EXP` |
| scholarship_point | 확인 | `0P` |
| rank_context | 확인 | 전체 인원 대비 현황 표시 |

### 2.2 AttendanceRecord
일자별 출결 기록.

| 속성 | 상태 | 근거 |
|---|---|---|
| user_id | 확인 | 출석현황 개인 화면 |
| attendance_date | 확인 | 달력/일자별 기록 |
| check_in_time | 확인 | `08:49 입실` 등 |
| check_out_time | 확인 | `18:00 퇴실` 등 |
| attendance_status | 확인 | 정상출석, 지각, 조퇴, 외출, 결석 |
| approval_type | 확인 | 사유승인, 임의 |
| monthly_rate | 확인 | 출석률 100% |

### 2.3 AttendanceAppeal
출결 소명 요청.

| 속성 | 상태 | 근거 |
|---|---|---|
| attendance_record_id | 추론 | 특정 일자/기록에 대한 소명 버튼 존재 |
| appeal_type | 확인 | 지각 소명, 조퇴 소명, 외출 소명 |
| reason | 추론 | 소명서 작성 흐름 존재 |
| approval_status | 추론 | 사유승인/임의 구분 존재 |

### 2.4 Notification
알림함의 알림 메시지.

| 속성 | 상태 | 근거 |
|---|---|---|
| notification_id | 확인 | 폼 필드 `ntcnHistSeq` |
| box_type | 확인 | 받은알림함 / 보낸알림함 |
| title | 확인 | 알림 목록 제목 |
| created_at | 확인 | `2026-04-21 17:20:00` 형식 |
| sender_name | 확인 | `운영자` |
| selected_yn | 확인 | 체크박스 기반 선택삭제 |

---

## 3. 강의실 / 학습 계열

### 3.1 CurriculumTerm
학기/기간 단위 커리큘럼 상위 개념.

| 속성 | 상태 | 근거 |
|---|---|---|
| term_id | 추론 | `1학기`, `2학기`, `Job Fair` 구분 |
| term_name | 확인 | 1학기, 2학기, 1차 Job Fair |
| progress_state | 확인 | 진행중 / 예정 |

### 3.2 CurriculumSchedule
주간/월간 커리큘럼의 실제 수업 일정.

| 속성 | 상태 | 근거 |
|---|---|---|
| schedule_id | 추론 | 일정 카드 반복 구조 |
| term_id | 추론 | 학기/단계 하위 일정 |
| week_no | 확인 | 1주차~25주차 |
| class_date | 확인 | `2026.04.20(월)` |
| time_range | 확인 | `09:00~18:00` |
| curriculum_type | 확인 | 코딩과정, 프로젝트 |
| track | 확인 | Web(Back) 등 |
| topic | 확인 | Servlet 2, JSP, Session/Cookie |
| instructor_name | 확인 | 박사홍 |
| classroom | 확인 | 서울 605 |

### 3.3 LectureReplay
다시보기 강의 콘텐츠.

| 속성 | 상태 | 근거 |
|---|---|---|
| replay_id / content_id | 확인 | 폼 hidden `contId` |
| group_name | 확인 | Java 전공 다시보기 등 |
| title | 확인 | 강의 다시보기 항목 제목 |
| latest_flag | 확인 | `최신강의 보러가기` |
| last_board_seq | 확인 | `lastLctrBrdSeq` hidden |

### 3.4 LearningMaterial
학습자료(오픈러닝) 콘텐츠.

| 속성 | 상태 | 근거 |
|---|---|---|
| content_id | 확인 | hidden `contId`, `fnView(contId)` |
| category_parent | 확인 | 커리큘럼 |
| category_child | 확인 | 교재 등 |
| content_type | 확인 | ebook, 동영상, 첨부파일, 유튜브, 사이트 |
| title | 확인 | 목록 제목 |
| summary | 확인 | 자료 설명 텍스트 |
| view_count | 확인 | 조회수 |
| like_count | 확인 | 좋아요수 |
| favorite_count | 확인 | 찜하기수 |
| detail_url | 확인 | `openLearningView.do` 실제 상세 진입 |
| ebook_attachment_id | 확인 | `fnEbook(atchId)` |

### 3.5 QuestEvaluation
Quest 및 평가 항목.

| 속성 | 상태 | 근거 |
|---|---|---|
| task_seq | 확인 | hidden `taskSeq`, `fnDetail(taskSeq, tpClfcCd)` |
| type | 확인 | Quest / 평가 |
| tp_clfc_cd | 확인 | hidden `tpClfcCd` |
| title | 확인 | 목록 제목 |
| open_close_at | 확인 | 시작/종료 시간 범위 |
| max_exp | 확인 | 획득 가능 경험치 |
| status | 확인 | 예정 / 완료 |
| result | 확인 | PASS |
| score | 확인 | 77점, 91점 등 |
| submission_state | 확인 | 채점완료 / 제출완료 |

### 3.6 Survey
설문조사 항목.

| 속성 | 상태 | 근거 |
|---|---|---|
| survey_id | 추론 | 상세 미확보, 목록 단위 식별자 필요 |
| title | 확인 | 설문 제목 |
| category | 확인 | 과목설문 / 기타 |
| required_flag | 확인 | `[필수]` 표기 |
| period_start / period_end | 확인 | 설문기간 |
| status | 확인 | 진행중 / 예정 / 종료 / 설문완료 |

---

## 4. 커뮤니티 / 게시판 / 안내 계열

### 4.1 Board
게시판 종류를 정의하는 상위 엔티티.

| 속성 | 상태 | 근거 |
|---|---|---|
| board_code | 확인 | free, notice, faq, qna 등 URL 패턴 |
| board_name | 확인 | 열린 게시판, 공지사항, FAQ, 1:1 문의 |
| board_group | 확인 | 커뮤니티 / HELP DESK |

### 4.2 BoardCategory
게시판 내 분류.

| 속성 | 상태 | 근거 |
|---|---|---|
| category_id | 추론 | 내부 코드 필요 |
| board_code | 확인 | 게시판 종속 |
| category_name | 확인 | 자유게시판, SSAFYcial, 학습, 평가, 기타 등 |

### 4.3 BoardPost
공지/열린게시판 등 게시글 공통 모델.

| 속성 | 상태 | 근거 |
|---|---|---|
| post_id | 확인 | `brdItmSeq`, `fnDetail('115458')` |
| board_code | 확인 | free / notice / faq / qna |
| category_id | 확인 | 목록 필터 존재 |
| title | 확인 | 게시글 제목 |
| author_name | 확인 | 운영자 등 |
| created_at | 확인 | 등록일/등록시각 |
| view_count | 확인 | 조회 |
| like_count | 확인 | 추천 |
| comment_count | 확인 | 댓글 |
| favorite_count | 확인 | 찜하기수 |
| attachment_flag | 확인 | `첨부 파일` 표기 |
| attachment_count | 확인 | 상세 화면 `첨부 파일 (1)` |
| previous_next_navigation | 확인 | 윗글/아랫글, 이전글/다음글 영역 |
| notice_flag | 확인 | 공지 게시글 존재 |

### 4.4 SupportTicket
1:1 문의 티켓 본체.

| 속성 | 상태 | 근거 |
|---|---|---|
| support_ticket_id | 갱신 | revised schema에서 문의 본체는 `support_tickets`로 관리 |
| requester_user_id | 확인 | 개인 문의 목록/작성 흐름 존재 |
| title | 확인 | 문의하기 기능 존재 |
| status_code | 갱신 | revised schema 기준 `open`, `waiting_user`, `answered`, `closed` 상태 사용 |
| created_at / updated_at / closed_at | 갱신 | 문의 수명주기 관리 필요 |

### 4.5 SupportTicketMessage
문의 스레드 내 개별 메시지.

| 속성 | 상태 | 근거 |
|---|---|---|
| support_ticket_message_id | 갱신 | revised schema에서 단일 답변 대신 메시지 스레드 구조 사용 |
| support_ticket_id | 갱신 | 문의 본체에 종속 |
| sender_user_id | 갱신 | 사용자/운영자/관리자 모두 메시지 작성 가능 |
| message_type_code | 갱신 | `user_message`, `admin_reply`, `internal_note` 코드 사용 |
| content | 확인 | 문의 및 답변 본문 필요 |
| created_at | 갱신 | 메시지 시간순 조회 필요 |

### 4.6 FaqItem
FAQ 문항.

| 속성 | 상태 | 근거 |
|---|---|---|
| faq_id | 추론 | FAQ 목록 구조 |
| category | 확인 | 교육, 출석, Quest/평가, 마일리지/포인트/레벨, 기타 |
| question | 추론 | FAQ 상세 미확보 |
| answer | 추론 | FAQ 상세 미확보 |

### 4.7 StudentDirectoryEntry
우리반 보기의 학생 검색/목록 항목.

| 속성 | 상태 | 근거 |
|---|---|---|
| user_id | 추론 | 학생 고유 식별 필요 |
| name | 확인 | 학생 이름 목록 |
| campus | 확인 | 서울 |
| class_name | 확인 | 서울14반 |
| can_send_notification | 확인 | `알림보내기` 버튼 |

---

## 5. 보조 엔티티

### 5.1 Attachment
게시글/자료 등에 연결되는 파일.

| 속성 | 상태 | 근거 |
|---|---|---|
| attachment_id | 추론 | 다운로드/삭제 스크립트 존재 |
| owner_type | 추론 | 게시글/자료/문의 등 공통 첨부 가능 |
| file_name | 확인 | 첨부 파일 표기 및 다운로드 URL |
| file_path_key | 확인 | 공통 file download 스크립트 |

### 5.2 PolicyDocument
이용약관 / 개인정보처리방침.

| 속성 | 상태 | 근거 |
|---|---|---|
| policy_type | 확인 | 이용약관, 개인정보처리방침 |
| content | 추론 | 팝업/문서 상세 필요 |

---

## 6. 관계 요약

- User 1:N Notification
- User 1:1 UserProfile
- User 1:N AttendanceRecord
- AttendanceRecord 1:N AttendanceAppeal
- User 1:N LevelPointSummary(기간 스냅샷 관점)
- CurriculumTerm 1:N CurriculumSchedule
- CurriculumSchedule N:1 ClassMembership/Track
- LearningMaterial N:1 CurriculumSchedule 또는 카테고리
- QuestEvaluation N:1 CurriculumSchedule/Track
- Survey N:1 Track 또는 이벤트/과목
- Board 1:N BoardPost
- BoardCategory 1:N BoardPost
- User 1:N BoardPost
- SupportTicket 1:N SupportTicketMessage
- User 1:N SupportTicket / SupportTicketMessage
- User N:M ClassMembership (기수/반/트랙 배정 관점)

---

## 7. 현재 기준에서 보류한 항목

- 학습자료 eBook 실제 뷰어 팝업 결과 화면
- Quest 상세/응시 상세 구조
- 설문 문항/응답 구조
- 자유게시판 상세의 댓글/첨부 세부 구조
- 공지사항 상세 본문 구조

위 항목은 상세 페이지 추가 수집 후 보강 필요.
