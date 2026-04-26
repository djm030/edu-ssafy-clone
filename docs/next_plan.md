# Next Plan: EduSSAFY Full Clone Production Gap Plan

작성일: 2026-04-26 KST
작성 목적: 실제 EduSSAFY 로그인 후 확인된 메뉴/화면과 현재 클론 구현 범위를 비교해, 다음 개발 라운드에서 바로 실행 가능한 수준의 세부 백로그를 정의한다.

> 주의: 실제 서비스 계정/비밀번호/세션 쿠키/원본 HTML은 이 문서와 저장소에 저장하지 않는다. 실제 서비스 확인은 읽기 전용 비교 근거로만 사용하고, 자동 테스트에는 실제 계정을 넣지 않는다.

---

## 0. 현재 결론

현재 저장소는 기존 우선순위 1~9 기준의 핵심 MVP는 대부분 PASS 상태다. 그러나 실제 EduSSAFY 전체 메뉴 기준으로 보면 아직 **프로덕션 레벨 전체 클론**이라고 보기 어려운 기능 영역이 남아 있다.

가장 큰 미구현 축은 다음이다.

1. 마이캠퍼스 부가 기능
   - 학습중 이러닝
   - 찜한 목록
   - 서류제출
   - 교육생 서약서
   - SSAFY e-book
   - 교육현황
2. 강의실 기능 심화
   - 라이브 바로가기
   - 내강의 다시보기 / 전체강의 다시보기 분리
   - 필수학습
   - 주차별 커리큘럼 상세도 강화
   - Quest/평가 상세 상태 강화
3. 커뮤니티/HELP DESK 확장
   - 익명 게시판
   - 학사규정
   - 실제 공지/FAQ/게시판 UX fidelity 강화
4. 멘토링/간담회 영역
   - 멘토 스토리
   - 멘토링 Q&A
   - 멘토링 공지사항
   - 간담회 신청
   - 간담회 정보
   - 간담회 후기
5. 외부 연동
   - JOB SSAFY
   - SSAFY GIT
   - Meeting! SSAFY
6. 프로덕션 검증
   - 브라우저 E2E
   - visual baseline
   - 운영 secret-store 배포 검증
   - 외부 환경 CI/CD sign-off
   - observability 실제 연동

---

## 1. 실제 EduSSAFY 확인 기반 메뉴 매핑

### 1.1 실제 EduSSAFY에서 확인된 대분류

| 실제 대분류 | 실제 하위 메뉴 | 현재 클론 상태 | 판정 |
|---|---|---:|---:|
| 마이캠퍼스 | 레벨&장학포인트 | `/mycampus/level` 존재 | PARTIAL |
| 마이캠퍼스 | 출석현황 | `/mycampus/attendance` 존재 | PASS/PARTIAL |
| 마이캠퍼스 | 학습중 이러닝 | 없음 | GAP |
| 마이캠퍼스 | 찜한 목록 | 없음 | GAP |
| 마이캠퍼스 | 서류제출 | 없음 | GAP |
| 마이캠퍼스 | 교육생 서약서 | 없음 | GAP |
| 마이캠퍼스 | SSAFY e-book | 없음 | GAP |
| 마이캠퍼스 | 교육현황 | 없음 | GAP |
| 강의실 | 라이브 바로가기 | 없음 | GAP |
| 강의실 | 내강의 다시보기 | `/learning/replays`와 통합 | PARTIAL |
| 강의실 | 전체강의 다시보기 | `/learning/replays`와 통합 | PARTIAL |
| 강의실 | 주차별 커리큘럼 | `/learning/curriculum` 존재 | PARTIAL |
| 강의실 | Quest/평가 | `/quest` 존재 | PARTIAL |
| 강의실 | 필수학습 | 없음 | GAP |
| 강의실 | 학습자료 | `/learning/materials` 존재 | PASS/PARTIAL |
| 커뮤니티 | 설문조사 | `/survey` 존재 | PASS/PARTIAL |
| 커뮤니티 | 열린 게시판 | `/community/free` 존재 | PASS/PARTIAL |
| 커뮤니티 | 익명 게시판 | 없음 | GAP |
| 커뮤니티 | 우리반 보기 | `/community/classmates` 존재 | PARTIAL |
| HELP DESK | 공지사항 | `/help/notice` 존재 | PARTIAL |
| HELP DESK | FAQ | `/help/faq` 존재 | PARTIAL |
| HELP DESK | 1:1 문의 | `/help/qna` 존재 | PASS/PARTIAL |
| HELP DESK | 학사규정 | 없음 | GAP |
| 멘토링 게시판 | 멘토 스토리 | 없음 | GAP |
| 멘토링 게시판 | 멘토링 | 없음 | GAP |
| 멘토링 게시판 | 멘토링 공지사항 | 없음 | GAP |
| 멘토링 게시판 | 간담회 신청 | 없음 | GAP |
| 멘토링 게시판 | 간담회 정보 | 없음 | GAP |
| 멘토링 게시판 | 간담회 후기 | 없음 | GAP |
| 외부 링크 | JOB SSAFY | 없음 | GAP/EXTERNAL |
| 외부 링크 | SSAFY GIT | 없음 | GAP/EXTERNAL |
| 외부 링크 | Meeting! SSAFY | 없음 | GAP/EXTERNAL |

### 1.2 현재 클론에 이미 존재하는 주요 라우트

현재 `frontend/src/routes.ts` 기준 존재하는 핵심 라우트:

- `/`
- `/login`
- `/profile/check`
- `/profile/edit`
- `/mycampus/attendance`
- `/mycampus/attendance/appeals/new`
- `/mycampus/level`
- `/mycampus/notifications`
- `/community/free`
- `/community/free/1`
- `/community/free/write`
- `/community/classmates`
- `/survey`
- `/survey/1`
- `/survey/1/respond`
- `/help/qna`
- `/help/qna/new`
- `/help/qna/tickets/1`
- `/help/notice`
- `/help/faq`
- `/learning/curriculum`
- `/learning/materials`
- `/learning/materials/1`
- `/learning/materials/1/viewer`
- `/learning/replays`
- `/quest`
- `/quest/1`
- `/quest/1/submit`
- `/ops/readiness`
- `/admin/campus`

---

## 2. 다음 개발 라운드 운영 원칙

### 2.1 반복 단위

다음 라운드는 반드시 **한 번에 하나의 기능 흐름**만 구현한다.

좋은 반복 단위 예시:

- `feat(elearning): implement in-progress e-learning flow`
- `feat(mypage): implement bookmarked learning list`
- `feat(documents): implement learner document submission flow`
- `feat(pledge): implement learner pledge agreement flow`
- `feat(community): implement anonymous board flow`
- `feat(helpdesk): implement academic rules flow`
- `feat(mentoring): implement mentor story board flow`
- `feat(meeting): implement mentoring meeting application flow`

나쁜 반복 단위 예시:

- “마이캠퍼스 전체 구현”
- “멘토링 전체 구현”
- “실제 EduSSAFY 완전 복제”
- “남은 것 전부 한 커밋”

### 2.2 한 기능의 완료 기준

각 기능은 최소 다음을 만족해야 한다.

- Backend
  - Entity 또는 기존 Entity 확장
  - Repository
  - Service
  - Controller
  - Request/Response DTO
  - 권한/소유자 검증
  - 정상/빈 데이터/오류 처리
- Frontend
  - API client 연결
  - route 등록
  - 화면 컴포넌트
  - loading/error/empty 상태
  - mutation이 있으면 성공/실패 메시지
- Test
  - Service 또는 Controller test
  - 권한/오류 케이스 test
  - 필요 시 static frontend smoke test
  - route smoke manifest 반영
- Verification
  - `git diff --check`
  - `git diff --stat`
  - backend targeted test
  - 가능하면 full backend test
  - `frontend npm run build && npm run lint`
  - `docker compose --profile app config`

### 2.3 커밋 규칙

- 기능 하나당 커밋 하나.
- 관련 파일만 stage.
- `prompts`, `.omx`, `docs/collaboration`, `AGENTS.md`는 건드리지 않음.
- 실제 코드 없이 문서만 변경하는 반복은 피함. 단, 이 문서는 다음 계획 수립용으로 사용자가 명시 요청한 예외 문서다.

---

## 3. 우선순위 제안

### Priority A: 실제 EduSSAFY 주요 메뉴 GAP 해소

실제 화면에서 메뉴가 보이지만 현재 클론에 아예 없는 기능을 먼저 만든다.

1. 학습중 이러닝
2. 찜한 목록
3. 서류제출
4. 교육생 서약서
5. 교육현황
6. 필수학습
7. 익명 게시판
8. 학사규정
9. 멘토 스토리
10. 멘토링 Q&A
11. 멘토링 공지사항
12. 간담회 신청/정보/후기

### Priority B: 이미 있는 기능의 실제 서비스 fidelity 강화

1. 레벨&장학포인트 상세화
2. 출석현황 월간/일간 상세화
3. 주차별 커리큘럼 학기/트랙/주차 필터 강화
4. 내강의/전체강의 다시보기 분리
5. Quest/평가 상태/기간/제출/결과 상세화
6. 우리반 보기 검색/알림 보내기 UX 강화
7. 공지/FAQ 카테고리/검색/상세 강화

### Priority C: 프로덕션 검증 강화

1. Playwright 또는 Cypress 기반 브라우저 E2E
2. visual baseline screenshot 비교
3. 운영 profile secret-store 문서/검증
4. 실제 CI hosted run evidence
5. observability stack 연결 smoke

---

## 4. 상세 기능 백로그

## A1. 학습중 이러닝

### 현재 상태

실제 EduSSAFY에는 `마이캠퍼스 > 학습중 이러닝` 메뉴가 있다. 현재 클론에는 해당 route/API가 없다.

### 목표

사용자가 진행 중인 이러닝 콘텐츠 목록을 조회하고, 진행률/최근 학습일/이어보기 상태를 확인할 수 있게 한다.

### Backend 설계

#### Entity 후보

`ElearningCourse`

- `id`
- `title`
- `category`
- `thumbnailUrl`
- `provider`
- `description`
- `totalLessons`
- `totalDurationSeconds`
- `active`
- `createdAt`
- `updatedAt`

`LearnerElearningProgress`

- `id`
- `userId`
- `courseId`
- `progressPercent`
- `completedLessons`
- `lastLessonTitle`
- `lastLearningAt`
- `status`
  - `NOT_STARTED`
  - `IN_PROGRESS`
  - `COMPLETED`
- `resumeUrl`
- `createdAt`
- `updatedAt`

#### API

`GET /api/elearning/in-progress`

Query:

- `page`
- `size`
- `status`
- `keyword`

Response:

```json
{
  "items": [
    {
      "courseId": 1,
      "title": "Java 기초 이러닝",
      "category": "Java",
      "progressPercent": 42,
      "completedLessons": 5,
      "totalLessons": 12,
      "lastLessonTitle": "객체지향 핵심",
      "lastLearningAt": "2026-04-25T10:15:00+09:00",
      "status": "IN_PROGRESS",
      "resumeUrl": "/learning/elearning/1/resume"
    }
  ],
  "page": 1,
  "size": 10,
  "totalItems": 1,
  "totalPages": 1
}
```

`GET /api/elearning/in-progress/{courseId}`

- 상세 진행 현황
- 차시 목록
- 완료/미완료 상태

`POST /api/elearning/in-progress/{courseId}/resume`

- 실제 외부 플레이어 연동 전에는 내부 resume log 저장
- response는 redirect URL 또는 viewer path 반환

#### 권한

- 로그인 사용자만 조회 가능.
- learner는 본인 progress만 조회 가능.
- coach/admin은 추후 관리 화면에서 조회 가능하되 이번 기능에는 포함하지 않는다.

### Frontend 설계

Route:

- `/mycampus/elearning`
- `/mycampus/elearning/:courseId`

화면 요소:

- 제목: 학습중 이러닝
- 상태 필터: 전체 / 학습중 / 완료
- 검색어 입력
- 카드 목록
  - 썸네일
  - 과정명
  - 진행률 bar
  - 완료 차시 / 전체 차시
  - 최근 학습일
  - 이어보기 버튼
- empty 상태: “학습중인 이러닝이 없습니다.”
- error 상태: requestId 표시

### Test

Backend:

- 진행 중 이러닝 목록 조회
- 빈 목록 조회
- 본인 외 progress 접근 차단
- status filter
- keyword filter

Frontend/static:

- route manifest에 `/mycampus/elearning` 포함
- page가 DataState/LoadingRows 사용
- API client path 포함

### Acceptance Criteria

- 로그인 후 `/mycampus/elearning` 접근 가능.
- DB seed 기준 최소 1개 진행 중 이러닝 표시.
- progress bar가 실제 API 값으로 렌더링.
- 빈 데이터/오류 상태 존재.
- backend targeted test 통과.
- frontend build/lint 통과.

---

## A2. 찜한 목록

### 현재 상태

실제 EduSSAFY에는 `마이캠퍼스 > 찜한 목록`이 있다. 현재 클론에는 학습자료 반응/북마크성 기능 일부가 있으나, 통합 찜 목록 화면은 없다.

### 목표

사용자가 찜한 오픈러닝/이러닝/학습자료를 한 화면에서 조회하고 해제할 수 있게 한다.

### Backend 설계

기존 material like/bookmark가 있다면 재사용한다. 없거나 범위가 좁으면 공통 bookmark 모델로 확장한다.

#### Entity 후보

`LearnerBookmark`

- `id`
- `userId`
- `targetType`
  - `MATERIAL`
  - `ELEARNING`
  - `REPLAY`
  - `QUEST_REFERENCE`
- `targetId`
- `titleSnapshot`
- `descriptionSnapshot`
- `thumbnailUrl`
- `createdAt`

#### API

`GET /api/me/bookmarks`

Query:

- `targetType`
- `page`
- `size`

`POST /api/me/bookmarks`

Body:

```json
{
  "targetType": "MATERIAL",
  "targetId": 1
}
```

`DELETE /api/me/bookmarks/{bookmarkId}`

또는 idempotent delete:

`DELETE /api/me/bookmarks?targetType=MATERIAL&targetId=1`

### Frontend 설계

Route:

- `/mycampus/bookmarks`

화면 요소:

- 탭: 전체 / 오픈러닝 / 이러닝 / 다시보기
- 목록 카드
- 이동 버튼
- 찜 해제 버튼
- empty/error/loading 상태

### Test

- 목록 조회
- target type 필터
- 중복 찜 방지
- 삭제 권한: 본인 bookmark만 삭제
- nonexistent target 오류

### Acceptance Criteria

- 실제 DB에 저장된 bookmark가 조회된다.
- 삭제 후 목록에서 제거된다.
- mock fallback 없이 API 기반으로 동작한다.

---

## A3. 서류제출

### 현재 상태

실제 EduSSAFY에는 `마이캠퍼스 > 서류제출`이 있다. 현재 클론에는 support/material/quest/board 쪽 첨부파일은 있으나, 교육생 서류 제출 흐름은 없다.

### 목표

교육생이 제출해야 하는 서류 목록을 보고, 파일을 업로드하고, 제출 상태를 확인할 수 있게 한다.

### Backend 설계

#### Entity 후보

`DocumentRequest`

- `id`
- `title`
- `description`
- `category`
- `required`
- `allowedExtensions`
- `maxFileSizeBytes`
- `startsAt`
- `dueAt`
- `active`
- `createdBy`
- `createdAt`

`LearnerDocumentSubmission`

- `id`
- `documentRequestId`
- `userId`
- `status`
  - `NOT_SUBMITTED`
  - `SUBMITTED`
  - `REJECTED`
  - `APPROVED`
- `submittedAt`
- `reviewedAt`
- `reviewedBy`
- `reviewComment`

`LearnerDocumentAttachment`

- `id`
- `submissionId`
- `originalFileName`
- `contentType`
- `sizeBytes`
- `storageKey`
- `createdAt`

#### API

`GET /api/documents/requests`

- 로그인 사용자 기준 제출 상태 포함

`GET /api/documents/requests/{requestId}`

`POST /api/documents/requests/{requestId}/submissions`

- multipart file upload
- metadata 저장

`GET /api/documents/submissions/{submissionId}/attachments/{attachmentId}`

- 본인 또는 staff/admin만 다운로드

`DELETE /api/documents/submissions/{submissionId}`

- 마감 전/검토 전만 취소 가능

Staff/Admin future:

`GET /api/admin/documents/submissions`

- 이번 라운드에는 최소 learner flow만 구현하고 admin review는 다음 라운드로 분리 가능.

### Frontend 설계

Route:

- `/mycampus/documents`
- `/mycampus/documents/:requestId`

화면 요소:

- 제출 요청 목록
- 상태 badge
- 마감일
- 필수 여부
- 파일 업로드 input
- 제출/취소 버튼
- 검토 의견 표시

### Test

- 요청 목록 + 사용자 제출 상태 조합
- 제출 upload 성공
- 파일 확장자/크기 제한
- 본인 외 attachment download 금지
- 마감 후 제출 금지

### Acceptance Criteria

- 실제 DB 저장/조회 흐름이 있다.
- 파일 metadata와 byte 저장 또는 기존 attachment storage를 재사용한다.
- user ownership 권한 테스트가 있다.

---

## A4. 교육생 서약서

### 현재 상태

실제 EduSSAFY에는 `마이캠퍼스 > 교육생 서약서`가 있다. 현재 클론에는 없다.

### 목표

교육생이 서약서 목록을 확인하고, 약관/서약 내용을 읽고, 동의/제출 상태를 저장할 수 있게 한다.

### Backend 설계

#### Entity 후보

`PledgeDocument`

- `id`
- `title`
- `content`
- `version`
- `required`
- `startsAt`
- `dueAt`
- `active`
- `createdAt`

`LearnerPledgeAgreement`

- `id`
- `pledgeDocumentId`
- `userId`
- `agreed`
- `agreedAt`
- `agreementIpHash`
- `userAgentHash`
- `versionSnapshot`

#### API

`GET /api/pledges`

- 서약서 목록 + 내 동의 상태

`GET /api/pledges/{pledgeId}`

`POST /api/pledges/{pledgeId}/agreements`

Body:

```json
{
  "agreed": true
}
```

### Frontend 설계

Route:

- `/mycampus/pledges`
- `/mycampus/pledges/:pledgeId`

화면 요소:

- 서약서 목록
- 필수/선택 badge
- 제출 상태
- 내용 보기
- 동의 checkbox
- 제출 버튼
- 제출 후 timestamp 표시

### Test

- 목록 조회
- 동의 저장
- 중복 제출 idempotent 처리
- active 기간 외 제출 금지
- 본인 상태만 조회

### Acceptance Criteria

- 서약서 동의 기록이 DB에 남는다.
- 동의 후 새로고침해도 상태 유지.
- error/empty/loading 처리.

---

## A5. 교육현황

### 현재 상태

실제 EduSSAFY에는 `마이캠퍼스 > 교육현황`이 있고 출결/과제/학습/포인트 등 요약 지표를 보여준다. 현재 클론 Dashboard/Level/Attendance가 일부 역할을 하지만 통합 교육현황 화면은 없다.

### 목표

교육생의 학습 현황을 한 화면에서 통합 요약한다.

### Backend 설계

#### API

`GET /api/mycampus/education-status`

Response:

```json
{
  "attendance": {
    "month": "2026-04",
    "presentDays": 18,
    "lateDays": 1,
    "absentDays": 0,
    "appealPendingCount": 1
  },
  "learning": {
    "inProgressElearningCount": 3,
    "completedRequiredStudyCount": 5,
    "totalRequiredStudyCount": 8,
    "replayWatchMinutes": 320
  },
  "quests": {
    "openCount": 2,
    "submittedCount": 5,
    "lateCount": 0
  },
  "points": {
    "scholarshipPoint": 0,
    "experiencePoint": 1153,
    "levelName": "Bronze Lv.3"
  }
}
```

### Frontend 설계

Route:

- `/mycampus/education-status`

화면 요소:

- 출결 요약 카드
- 학습 요약 카드
- Quest/평가 요약 카드
- 포인트/레벨 요약 카드
- 각 상세 화면 이동 버튼

### Test

- summary aggregation service test
- 빈 데이터 default 0 처리
- 로그인 사용자 기준 조회

### Acceptance Criteria

- 여러 도메인의 요약을 DB 조회로 합성한다.
- Dashboard와 중복되더라도 실제 메뉴 기준 별도 화면을 제공한다.

---

## A6. SSAFY e-book

### 현재 상태

실제 메뉴에는 `SSAFY e-book`이 있고 외부/내부 viewer 성격이다. 현재 클론에는 없다.

### 목표

학습 자료와 별도로 e-book 목록/열람 링크를 제공한다.

### Backend 설계

`Ebook`

- `id`
- `title`
- `description`
- `thumbnailUrl`
- `category`
- `externalUrl`
- `active`
- `createdAt`

`EbookAccessLog`

- `id`
- `ebookId`
- `userId`
- `accessedAt`

API:

- `GET /api/ebooks`
- `GET /api/ebooks/{ebookId}`
- `POST /api/ebooks/{ebookId}/access-log`

### Frontend 설계

Route:

- `/mycampus/ebooks`
- `/mycampus/ebooks/:ebookId`

### Test

- list/detail
- access log 저장
- inactive e-book 제외

### Acceptance Criteria

- 목록/상세/view action이 mock 없이 동작.

---

## B1. 필수학습

### 현재 상태

실제 EduSSAFY 강의실에는 `필수학습` 메뉴가 있다. 현재 클론에는 없다.

### 목표

필수 학습 콘텐츠 목록과 이수 상태를 관리한다.

### Backend 설계

`RequiredStudy`

- `id`
- `title`
- `description`
- `category`
- `requiredForTrack`
- `dueAt`
- `contentType`
- `contentUrl`
- `active`

`LearnerRequiredStudyProgress`

- `id`
- `requiredStudyId`
- `userId`
- `status`
  - `NOT_STARTED`
  - `IN_PROGRESS`
  - `COMPLETED`
  - `OVERDUE`
- `progressPercent`
- `completedAt`

API:

- `GET /api/required-studies`
- `GET /api/required-studies/{studyId}`
- `POST /api/required-studies/{studyId}/complete`

### Frontend 설계

Route:

- `/learning/required-studies`
- `/learning/required-studies/:studyId`

### Test

- due date status calculation
- complete action
- 본인 progress만 조회

---

## B2. 라이브 바로가기

### 현재 상태

실제 EduSSAFY에는 `라이브 바로가기`가 있다. 현재 클론에는 없다.

### 목표

오늘/현재 진행 중인 라이브 강의 링크를 보여주고 입장 로그를 남긴다.

### Backend 설계

`LiveSession`

- `id`
- `title`
- `track`
- `cohort`
- `classRoom`
- `startsAt`
- `endsAt`
- `joinUrl`
- `status`
  - `SCHEDULED`
  - `LIVE`
  - `ENDED`
- `createdAt`

`LiveSessionJoinLog`

- `id`
- `sessionId`
- `userId`
- `joinedAt`

API:

- `GET /api/live-sessions/today`
- `GET /api/live-sessions/current`
- `POST /api/live-sessions/{sessionId}/join`

### Frontend 설계

Route:

- `/learning/live`

화면 요소:

- 현재 라이브 카드
- 오늘 일정 목록
- 입장 버튼
- 종료/예정 상태 표시

### 주의

실제 화상회의 시스템과의 SSO는 별도 외부 연동 영역이다. 초기 구현은 URL launch + join log로 제한한다.

---

## B3. 내강의 다시보기 / 전체강의 다시보기 분리

### 현재 상태

현재 클론은 `/learning/replays` 하나로 통합되어 있다. 실제 EduSSAFY는 `내강의 다시보기`와 `전체강의 다시보기`가 분리되어 있다.

### 목표

다시보기 영역을 사용자의 수강 대상 강의와 전체 강의 검색으로 분리한다.

### Backend API

- `GET /api/replays/my`
- `GET /api/replays/all`
- `GET /api/replays/{replayId}`
- `POST /api/replays/{replayId}/watch-log`

### Frontend Route

- `/learning/replays/my`
- `/learning/replays/all`
- 기존 `/learning/replays`는 `/learning/replays/my`로 안내하거나 탭 화면으로 유지

### Test

- my replay는 user track/class 기준 필터
- all replay는 공개 범위 기준 필터
- watch log 저장

---

## B4. 커리큘럼 상세화

### 현재 상태

커리큘럼은 존재하지만 실제 서비스처럼 학기/주차/트랙/상태 구조가 충분히 세밀하지 않다.

### 목표

주차별 커리큘럼 화면을 실제 EduSSAFY에 가깝게 만든다.

### Backend 보강

`CurriculumWeek`

- `semester`
- `weekNumber`
- `track`
- `startsAt`
- `endsAt`
- `status`

`CurriculumSession`

- `weekId`
- `date`
- `period`
- `title`
- `instructor`
- `location`
- `sessionType`

API:

- `GET /api/curriculum/weeks?semester=&track=&status=`
- `GET /api/curriculum/weeks/{weekId}`

### Frontend 보강

- 학기 탭
- 주차 카드
- 진행중 badge
- 일자별 시간표
- 트랙 필터

---

## C1. 익명 게시판

### 현재 상태

실제 EduSSAFY에는 `익명 게시판`이 있다. 현재 클론에는 자유게시판 중심만 있다.

### 목표

작성자 익명화가 보장되는 게시판을 구현한다.

### Backend 설계

기존 board 도메인을 재사용하되 board code `anonymous`를 추가한다.

중요 정책:

- DB에는 실제 작성자 `authorId`를 저장한다.
- API 응답에서는 일반 사용자에게 작성자 식별자를 노출하지 않는다.
- 본인/관리자 권한 판단은 서버 내부 authorId로 수행한다.
- 목록/상세에서는 `익명` 또는 `익명N` 표시.
- 댓글도 익명 처리.

API:

- `GET /api/boards/anonymous/posts`
- `POST /api/boards/anonymous/posts`
- `GET /api/boards/anonymous/posts/{postId}`
- `PUT /api/boards/anonymous/posts/{postId}`
- `DELETE /api/boards/anonymous/posts/{postId}`
- comments/reactions는 기존 board API 재사용 가능

Frontend:

- `/community/anonymous`
- `/community/anonymous/:postId`
- `/community/anonymous/write`

Test:

- 익명 게시글 작성자 미노출
- 본인 수정/삭제 가능
- 타인 수정/삭제 불가
- admin/moderator 가능 여부 정책 결정 후 테스트

Acceptance:

- 작성자 정보가 response에 노출되지 않는다.
- 권한은 정상 동작한다.

---

## C2. 학사규정

### 현재 상태

실제 HELP DESK에는 `학사규정` 메뉴가 있다. 현재 클론에는 없다.

### 목표

출결/평가/포인트/수료 등 학사규정을 카테고리별 아코디언으로 조회한다.

### Backend 설계

`AcademicRuleCategory`

- `id`
- `name`
- `displayOrder`
- `active`

`AcademicRule`

- `id`
- `categoryId`
- `question`
- `answer`
- `displayOrder`
- `active`
- `updatedAt`

API:

- `GET /api/help/academic-rules`
- `GET /api/help/academic-rules/{ruleId}`

Frontend:

- `/help/rules`
- category tabs
- accordion list
- search
- “답변이 충분하지 않으면 1:1 문의” link

Test:

- category grouping
- active only
- search
- empty state

---

## C3. 공지사항/FAQ fidelity 강화

### 현재 상태

공지사항/FAQ route는 있으나 실제 EduSSAFY의 카테고리, 검색, count, 상세 UX와 차이가 있다.

### 목표

실제 HELP DESK 화면처럼 카테고리 count, 검색, 상세/목록 이동을 제공한다.

### Backend 보강

- notice category count endpoint
- FAQ category count endpoint
- detail view count
- pinned/important notice

API:

- `GET /api/help/notices/categories`
- `GET /api/help/notices`
- `GET /api/help/notices/{noticeId}`
- `GET /api/help/faqs/categories`
- `GET /api/help/faqs`

Frontend:

- category pill with count
- search input
- pinned notice style
- accordion FAQ

---

## D1. 멘토 스토리

### 현재 상태

실제 EduSSAFY에는 `멘토링 게시판 > 멘토 스토리`가 있다. 현재 클론에는 없다.

### 목표

멘토 스토리 게시판 목록/상세를 구현한다.

### Backend 설계

공통 board를 확장하거나 별도 mentoring 도메인을 둔다.

권장: 별도 `mentoring` 도메인으로 분리. 이유는 멘토링 Q&A/공지/간담회와 연결될 가능성이 높다.

`MentorStory`

- `id`
- `title`
- `content`
- `mentorName`
- `mentorCompany`
- `mentorRole`
- `thumbnailUrl`
- `viewCount`
- `publishedAt`
- `active`

API:

- `GET /api/mentoring/stories`
- `GET /api/mentoring/stories/{storyId}`

Frontend:

- `/mentoring/stories`
- `/mentoring/stories/:storyId`

Test:

- list paging
- detail increments view count
- inactive excluded

---

## D2. 멘토링 Q&A

### 목표

멘티가 현업 멘토에게 질문하고, 답변/상태를 확인하는 게시판을 구현한다.

### Backend 설계

`MentoringQuestion`

- `id`
- `authorId`
- `title`
- `content`
- `category`
- `status`
  - `OPEN`
  - `ANSWERED`
  - `CLOSED`
- `anonymousAllowed`
- `createdAt`

`MentoringAnswer`

- `id`
- `questionId`
- `mentorId`
- `content`
- `createdAt`

API:

- `GET /api/mentoring/questions`
- `POST /api/mentoring/questions`
- `GET /api/mentoring/questions/{questionId}`
- `POST /api/mentoring/questions/{questionId}/answers`
- `PATCH /api/mentoring/questions/{questionId}/close`

권한:

- learner: 질문 작성, 본인 질문 수정/닫기
- mentor/staff/admin: 답변 작성
- 전체 공개 여부는 정책 결정 필요. 기본은 로그인 사용자 전체 조회.

Frontend:

- `/mentoring/questions`
- `/mentoring/questions/new`
- `/mentoring/questions/:questionId`

Test:

- learner question create
- mentor answer create
- learner answer forbidden
- owner close

---

## D3. 멘토링 공지사항

### 목표

멘토링 관련 공지 목록/상세를 구현한다.

API:

- `GET /api/mentoring/notices`
- `GET /api/mentoring/notices/{noticeId}`

Frontend:

- `/mentoring/notices`
- `/mentoring/notices/:noticeId`

Test:

- pinned notice
- category/search
- active only

---

## D4. 간담회 신청

### 목표

교육생이 모집 중인 간담회에 신청하고, 신청 상태를 확인할 수 있게 한다.

### Backend 설계

`MentoringMeeting`

- `id`
- `title`
- `description`
- `meetingType`
  - `ONLINE`
  - `OFFLINE`
- `topic`
- `capacity`
- `startsAt`
- `endsAt`
- `applicationStartsAt`
- `applicationEndsAt`
- `status`
  - `RECRUITING`
  - `CLOSED`
  - `DONE`
- `location`
- `meetingUrl`

`MentoringMeetingApplication`

- `id`
- `meetingId`
- `userId`
- `motivation`
- `status`
  - `APPLIED`
  - `CANCELLED`
  - `SELECTED`
  - `REJECTED`
- `appliedAt`
- `cancelledAt`

API:

- `GET /api/mentoring/meetings`
- `GET /api/mentoring/meetings/{meetingId}`
- `POST /api/mentoring/meetings/{meetingId}/applications`
- `DELETE /api/mentoring/meetings/{meetingId}/applications/me`
- `GET /api/mentoring/meetings/applications/me`

Frontend:

- `/mentoring/meetings`
- `/mentoring/meetings/:meetingId`
- `/mentoring/meetings/my-applications`

Test:

- 모집기간 내 신청 가능
- 모집기간 외 신청 불가
- capacity 초과 방지
- 중복 신청 방지
- 본인 신청 취소

---

## D5. 간담회 정보 / 후기

### 목표

간담회가 종료된 뒤 정보와 후기를 조회/작성할 수 있게 한다.

### Backend

`MentoringMeetingReview`

- `id`
- `meetingId`
- `authorId`
- `title`
- `content`
- `rating`
- `createdAt`
- `updatedAt`

API:

- `GET /api/mentoring/meeting-results`
- `GET /api/mentoring/meeting-results/{meetingId}`
- `GET /api/mentoring/meeting-reviews`
- `POST /api/mentoring/meeting-reviews`
- `GET /api/mentoring/meeting-reviews/{reviewId}`
- `PUT /api/mentoring/meeting-reviews/{reviewId}`
- `DELETE /api/mentoring/meeting-reviews/{reviewId}`

Frontend:

- `/mentoring/meeting-results`
- `/mentoring/meeting-results/:meetingId`
- `/mentoring/meeting-reviews`
- `/mentoring/meeting-reviews/write`
- `/mentoring/meeting-reviews/:reviewId`

Test:

- selected/applied users only can write review if policy requires
- owner edit/delete
- list/detail

---

## E1. 외부 연동: JOB SSAFY

### 현재 상태

실제 EduSSAFY에는 `JOB SSAFY` 링크가 있다. 현재 클론에는 없다.

### 목표

외부 SSO를 실제로 구현할지, 단순 launch link로 둘지 정책을 분리한다.

### 1차 구현 권장

- 운영 점검 화면과 AppShell에 외부 서비스 링크 카드 제공
- 외부 URL은 env/config에서 주입
- 클릭 로그 저장
- 실제 SSO 토큰 생성은 구현하지 않음

### Backend

`ExternalServiceLink`

- `id`
- `code`
- `name`
- `url`
- `description`
- `enabled`

`ExternalServiceAccessLog`

- `id`
- `userId`
- `serviceCode`
- `accessedAt`

API:

- `GET /api/external-services`
- `POST /api/external-services/{code}/access-log`

### Frontend

- `/external-services`
- AppShell quick links

### Acceptance

- URL 하드코딩 최소화.
- 민감한 SSO credential 없음.
- 외부 링크 disabled 상태 처리.

---

## E2. 외부 연동: SSAFY GIT

### 목표

`SSAFY GIT` 외부 프로젝트 시스템 링크를 제공한다.

1차 구현은 E1 공통 external service framework에 포함한다.

추가 고려:

- git project deep link
- 새 창 열기
- 접근 권한 role check
- audit log

---

## E3. 외부 연동: Meeting! SSAFY

### 목표

Meeting 서비스 링크/입장 로그를 제공한다.

Live Session과 연동 가능:

- live session join URL이 Meeting! SSAFY인 경우
- `/api/live-sessions/{sessionId}/join`에서 access log 저장 후 URL 반환

---

## F1. 브라우저 E2E

### 현재 상태

현재 CI는 backend tests, frontend lint/build, POSIX smoke contract, route smoke manifest 중심이다. 실제 브라우저에서 로그인/탐색/폼 제출을 검증하는 테스트는 없다.

### 목표

Playwright 또는 Cypress를 도입해 핵심 사용자 플로우를 검증한다.

### 권장 도구

Playwright.

이유:

- Chromium/WebKit/Firefox 지원
- trace/screenshot/video 관리 용이
- CI에서 headless 실행 쉬움

### E2E 시나리오 1차

1. 로그인
   - demo user 로그인
   - `/api/me` bootstrap 확인
   - Dashboard 이동
2. 출석
   - 출석현황 목록 확인
   - 이의신청 작성
   - 신청 후 pending 상태 확인
3. 게시판
   - 게시글 작성
   - 댓글 작성
   - 수정/삭제 권한 확인
4. 설문
   - 설문 목록
   - 상세 문항
   - 응답 저장
5. 1:1 문의
   - 문의 작성
   - 상세 thread 확인
6. 학습자료
   - 목록
   - 상세
   - viewer
   - bookmark/like
7. Quest
   - 목록
   - 상세
   - 제출
8. 운영 readiness
   - `/ops/readiness` 화면에서 priority matrix 확인

### CI 추가

- `frontend/e2e/*.spec.ts`
- `npm run e2e`
- Docker compose app profile 기동 후 실행

### 주의

- 외부 EduSSAFY 계정으로 E2E를 돌리지 않는다.
- 로컬 seed demo 계정만 사용한다.

---

## F2. Visual Baseline

### 목표

실제 EduSSAFY 화면과 pixel-perfect까지는 아니더라도, 주요 화면의 구조적 fidelity를 회귀 검증한다.

### 대상 화면

- 로그인
- 대시보드
- 출석현황
- 레벨&장학포인트
- 학습중 이러닝
- 찜한 목록
- 서류제출
- 교육생 서약서
- 교육현황
- 커리큘럼
- Quest/평가
- 학습자료
- 설문
- 열린 게시판
- 익명 게시판
- 우리반 보기
- 공지사항
- FAQ
- 학사규정
- 1:1 문의
- 멘토 스토리
- 멘토링
- 간담회

### 구현 방식

- Playwright screenshot baseline
- viewport 1440x900, mobile 390x844
- dynamic timestamp/user fields masking
- threshold 설정

### Acceptance

- 주요 10개 화면 baseline PASS
- 신규 화면 추가 시 baseline도 추가

---

## F3. 운영 Secret/Env Hardening

### 현재 상태

`.env.example`은 placeholder 정책을 갖고 있고 prod profile은 secret fallback을 피하도록 되어 있다. 그러나 실제 secret-store 연동은 없다.

### 목표

운영 배포 시 민감정보가 파일/이미지에 남지 않도록 검증한다.

### 할 일

- Docker compose production override 예시 작성 여부 결정
- Kubernetes/Cloud 배포를 할 경우 secret mapping 문서화
- backend startup에서 prod profile 필수 env 누락 시 fail-fast
- CI secret scanning 추가 가능성 검토

### Test

- prod profile missing datasource password fails
- prod cookie secure/strict default 유지
- `.env.example`에 실제 secret 없음

---

## F4. Observability

### 현재 상태

observability compose 파일은 존재하지만 실제 운영 지표/로그/trace까지 end-to-end 확인된 것은 아니다.

### 목표

운영에서 장애를 찾을 수 있도록 health, metrics, logs를 묶는다.

### 구현 후보

- Spring actuator metrics exposure
- Prometheus scrape config
- Grafana dashboard seed
- structured JSON logs
- requestId 로그 correlation
- Nginx access log correlation

### Smoke

- `GET /actuator/health`
- `GET /actuator/metrics`
- Prometheus target up
- Grafana dashboard provisioning file 존재

---

## 5. 다음 실행 순서 추천

### Round 1: 마이캠퍼스 GAP 1차

1. `feat(elearning): implement in-progress e-learning flow`
2. `feat(bookmark): implement learner bookmarked content list`
3. `feat(document): implement learner document submission flow`
4. `feat(pledge): implement learner pledge agreement flow`
5. `feat(mycampus): implement education status summary`

### Round 2: 강의실 GAP 1차

1. `feat(learning): implement required study flow`
2. `feat(learning): implement live session entry flow`
3. `feat(replay): split my and all replay flows`
4. `feat(curriculum): deepen weekly curriculum schedule`

### Round 3: 커뮤니티/HELP DESK GAP

1. `feat(board): implement anonymous board flow`
2. `feat(helpdesk): implement academic rules flow`
3. `feat(helpdesk): deepen notice and faq categories`

### Round 4: 멘토링/간담회

1. `feat(mentoring): implement mentor story board`
2. `feat(mentoring): implement mentoring question flow`
3. `feat(mentoring): implement mentoring notices`
4. `feat(meeting): implement mentoring meeting applications`
5. `feat(meeting): implement meeting results and reviews`

### Round 5: 외부 연동/운영 검증

1. `feat(external): expose external service launch links`
2. `test(e2e): add browser smoke for core flows`
3. `test(visual): add visual baseline smoke`
4. `chore(ops): harden production secret validation`
5. `chore(obs): add observability smoke gates`

---

## 6. 첫 번째로 바로 구현할 기능 추천

### 추천 1순위: 학습중 이러닝

이유:

- 실제 EduSSAFY 주요 메뉴에 존재한다.
- 현재 클론에 완전히 없다.
- 마이캠퍼스 핵심 사용자 체류 기능이다.
- 학습자료/다시보기/필수학습과 재사용 가능한 도메인 모델을 만든다.
- 파일 업로드나 외부 SSO보다 위험도가 낮다.

예상 변경 파일:

Backend:

- `backend/src/main/java/com/edussafy/backend/priority/domain/ElearningCourse.java` 또는 별도 `elearning/domain`
- `backend/src/main/java/com/edussafy/backend/priority/domain/LearnerElearningProgress.java`
- `backend/src/main/java/com/edussafy/backend/priority/repository/...`
- `backend/src/main/java/com/edussafy/backend/priority/service/...`
- `backend/src/main/java/com/edussafy/backend/priority/api/...`
- DTO classes

Frontend:

- `frontend/src/pages/ElearningPage.tsx`
- `frontend/src/pages/ElearningDetailPage.tsx`
- `frontend/src/api/app.ts`
- `frontend/src/routes.ts`
- `frontend/src/App.tsx`

Tests:

- `backend/src/test/java/com/edussafy/backend/priority/service/PriorityApiServiceTest.java` 또는 신규 test
- `backend/src/test/java/com/edussafy/backend/priority/api/PriorityApiControllerTest.java` 또는 신규 test
- `backend/src/test/java/com/edussafy/backend/docs/FrontendRouteSmokeCoverageTest.java`

Verification:

```bash
git diff --check
git diff --stat
docker run --rm -v "$PWD:/workspace" -w /workspace/backend maven:3.9.9-eclipse-temurin-21 mvn -q -Dtest=PriorityApiServiceTest,PriorityApiControllerTest,FrontendRouteSmokeCoverageTest test
cd frontend && npm run build && npm run lint
docker compose --profile app config >/tmp/compose-app-config.yml
```

Commit:

```text
feat(elearning): implement in-progress learning flow
```

---

## 7. 기능별 상세 Acceptance Checklist

각 기능 구현 후 아래 체크리스트를 반드시 확인한다.

### 공통 Backend Checklist

- [ ] Controller endpoint가 있다.
- [ ] Service에서 비즈니스 로직을 처리한다.
- [ ] Repository/DB 조회 흐름이 있다.
- [ ] DTO가 Entity를 직접 노출하지 않는다.
- [ ] 로그인 사용자 기준 필터링이 있다.
- [ ] 본인/역할 권한 검증이 있다.
- [ ] 빈 데이터 응답이 정상 shape로 반환된다.
- [ ] 잘못된 요청은 400/404/403 중 적절한 오류를 반환한다.
- [ ] requestId가 오류 응답에 포함된다.
- [ ] 테스트가 정상/오류/권한 케이스를 포함한다.

### 공통 Frontend Checklist

- [ ] route 등록.
- [ ] navigation 등록.
- [ ] API client 함수 추가.
- [ ] loading 상태.
- [ ] empty 상태.
- [ ] error 상태.
- [ ] requestId 표시.
- [ ] mutation 성공/실패 메시지.
- [ ] 모바일에서 깨지지 않는 Tailwind layout.
- [ ] smoke route manifest에 포함.

### 공통 Docs/Test Checklist

- [ ] `docs/api-summary.md`는 필요한 경우만 짧게 갱신.
- [ ] `docs/test-report.md`에는 검증 결과 추가.
- [ ] REST Docs가 필요한 API면 snippet 추가.
- [ ] `scripts/dev/smoke.sh` 또는 `smoke-routes.sh`가 필요한 경우 확장.
- [ ] CI guard가 깨지지 않는다.

---

## 8. 데이터 모델 통합 주의점

### 8.1 User 기준

모든 learner 개인화 기능은 반드시 현재 로그인 사용자 id를 기준으로 필터링한다.

적용 대상:

- 학습중 이러닝
- 찜한 목록
- 서류제출
- 서약서 동의
- 교육현황
- 필수학습 진행률
- 라이브 입장 로그
- 다시보기 시청 로그
- 간담회 신청

### 8.2 Attachment 재사용

이미 support/material/quest/board 쪽 attachment 흐름이 있다. 서류제출에서도 새 storage 방식을 만들기 전에 기존 byte storage/download pattern을 재사용한다.

재사용 기준:

- original filename
- content type
- size
- storage key 또는 bytes
- owner authorization
- download endpoint

### 8.3 Board 재사용과 분리

익명 게시판은 기존 board domain을 재사용하는 것이 좋지만, response privacy 정책이 다르다.

주의:

- 일반 board DTO를 그대로 쓰면 author name/email이 노출될 수 있다.
- anonymous board 전용 response mapper를 두는 것이 안전하다.

### 8.4 Mentoring 분리

멘토링은 일반 board와 다르게 다음 개념이 필요하다.

- mentor role
- answer status
- meeting application
- selected/rejected state
- review eligibility

따라서 장기적으로는 `mentoring` 별도 도메인 패키지를 권장한다.

---

## 9. 실제 서비스 fidelity에서 중요한 UI 패턴

실제 EduSSAFY에서 반복적으로 보이는 UI 패턴:

- 좌측/상단 대분류 네비게이션
- 현재 메뉴 title + breadcrumb
- 카테고리 탭
- 목록 count 표시
- 검색/필터 영역
- 목록 table/card
- empty message
- 더보기/상세 이동
- 알림함/회원정보/로그아웃 user menu
- 외부 링크 quick access

클론 화면을 추가할 때 위 패턴을 재사용해야 화면 일관성이 높아진다.

---

## 10. 보안/개인정보 주의사항

1. 실제 EduSSAFY 계정 정보를 코드/문서/test에 넣지 않는다.
2. 실제 서비스 HTML 원문을 저장소에 커밋하지 않는다.
3. 실제 교육생 이름/학번/알림 내용은 문서화하지 않는다.
4. 익명 게시판은 서버 내부 권한 판단용 authorId와 API response 익명 표시를 분리한다.
5. 파일 업로드는 확장자/크기/content type 검증을 한다.
6. 외부 링크/SSO는 token 없이 launch link부터 구현한다.
7. 운영 profile에서는 secret fallback을 허용하지 않는다.

---

## 11. 다음 플래닝 때 결정해야 할 질문

### 기능 범위 질문

1. 다음 라운드는 실제 메뉴 GAP를 우선할 것인가, 기존 기능 fidelity를 우선할 것인가?
2. 마이캠퍼스 GAP를 한 라운드에 몇 개까지 처리할 것인가?
3. 멘토링을 일반 board 확장으로 볼 것인가, 별도 domain으로 분리할 것인가?
4. 외부 SSO는 launch link까지만 할 것인가, 실제 token handoff까지 설계할 것인가?
5. visual fidelity 목표는 pixel-perfect인가, 구조/동선 유사성인가?

### 기술 결정 질문

1. E2E 도구는 Playwright로 확정할 것인가?
2. 파일 저장소는 DB byte, local volume, S3-compatible 중 무엇으로 갈 것인가?
3. 운영 배포 target은 Docker Compose, VM, Kubernetes 중 무엇인가?
4. observability는 Prometheus/Grafana/ELK를 실제로 띄울 것인가?

---

## 12. 추천 최종 로드맵

### Milestone 1: 실제 메뉴 GAP 해소

목표: 실제 EduSSAFY 네비게이션에서 보이는 내부 메뉴를 대부분 route/API로 제공.

완료 조건:

- 마이캠퍼스 GAP 6개 중 5개 이상 구현
- 강의실 GAP 3개 이상 구현
- 익명 게시판/학사규정 구현
- screen smoke routes 45개 이상

### Milestone 2: 멘토링/간담회 구현

목표: 실제 서비스의 멘토링 게시판 섹션 제공.

완료 조건:

- 멘토 스토리
- 멘토링 Q&A
- 멘토링 공지
- 간담회 신청
- 간담회 정보/후기

### Milestone 3: 프로덕션 검증

목표: “로컬에서 돌아감”을 넘어 “릴리즈 후보 검증 가능” 상태로 만든다.

완료 조건:

- Playwright E2E 8개 이상
- visual baseline 10개 이상
- CI에서 backend/frontend/E2E 실행
- app profile fresh volume smoke
- prod profile secret fail-fast test
- observability smoke

### Milestone 4: 외부 연동

목표: JOB/GIT/Meeting 외부 서비스 접근점을 안전하게 제공.

완료 조건:

- 외부 service link API
- role-based visibility
- access log
- disabled/maintenance state
- 실제 SSO는 별도 승인 후 진행

---

## 13. 다음 작업자가 바로 시작할 명령형 지시 예시

다음 실행 프롬프트는 아래처럼 주면 된다.

```text
현재 저장소에서 docs/next_plan.md를 기준으로 다음 기능을 하나 구현하라.
우선순위는 A1 학습중 이러닝이다.
한 번에 하나의 기능만 구현하고, backend/frontend/test를 모두 연결하라.
실제 코드 변경 후 검증하고 관련 파일만 commit하라.
push는 하지 마라.
```

또는 특정 기능을 지정할 수 있다.

```text
docs/next_plan.md의 C1 익명 게시판을 구현하라.
기존 board domain을 재사용하되 API response에서 작성자 식별 정보가 노출되지 않게 하라.
권한 테스트와 frontend route smoke를 추가하고 커밋하라.
```

---

## 14. 최종 요약

현재 클론은 핵심 MVP는 통과했지만 실제 EduSSAFY 전체 서비스 기준으로는 다음이 가장 중요하게 남아 있다.

1. 마이캠퍼스 부가 메뉴 구현
2. 강의실 학습 흐름 심화
3. 익명 게시판/학사규정 구현
4. 멘토링/간담회 섹션 구현
5. 외부 서비스 링크/SSO 정책 구현
6. 브라우저 E2E/visual/운영 배포 검증

다음 개발은 `학습중 이러닝`부터 시작하는 것이 가장 안전하고 효과적이다.
