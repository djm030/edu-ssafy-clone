# Next Plan: EduSSAFY Full Clone Production Gap Plan

작성일: 2026-04-26 KST
최근 실서비스 재점검: 2026-04-26 KST, 실제 EduSSAFY 계정으로 읽기 전용 로그인 확인. 계정/비밀번호/쿠키/원본 HTML은 저장하지 않음.
작성 목적: 실제 EduSSAFY 로그인 후 확인된 메뉴/화면과 현재 클론 구현 범위를 비교해, 다음 개발 라운드에서 바로 실행 가능한 수준의 세부 백로그를 정의한다.

> 주의: 실제 서비스 계정/비밀번호/세션 쿠키/원본 HTML은 이 문서와 저장소에 저장하지 않는다. 실제 서비스 확인은 읽기 전용 비교 근거로만 사용하고, 자동 테스트에는 실제 계정을 넣지 않는다.


---

## 0.3 2026-04-27 재점검 시도 및 UI/UX Parity 냉정 판정

2026-04-27 KST에 EduSSAFY 실서비스 재확인을 다시 시도했다. 보안상 실제 계정/비밀번호/세션 쿠키/원본 HTML/원본 스크린샷은 저장소에 남기지 않는 조건으로만 진행했다. 이번 headless 로그인 자동화는 fresh sanitized artifact를 만들기 전에 종료되었으므로, 이 문서는 **2026-04-27 신규 원본 화면을 저장하지 않고** 다음 근거를 보수적으로 합산한다.

1. 2026-04-26 KST에 읽기 전용으로 확인한 실제 EduSSAFY 상단 메뉴/홈 구조 요약
2. 현재 저장소의 실제 route/API/test 구현 상태
3. 실서비스와 달라질 가능성이 큰 UI/UX 구조, 상태, 상호작용 gap

냉정한 결론은 다음과 같다.

| 영역 | 현재 판정 | 이유 |
|---|---:|---|
| 상단 대메뉴/하위 route 존재 | PASS/PARTIAL | 마이캠퍼스, 강의실, 커뮤니티, HELP DESK, 멘토링, 외부 서비스에 대응 route/API가 대부분 존재한다. |
| DB 기반 backend flow | PASS/PARTIAL | 주요 도메인은 Controller/Service/Repository/DTO/API test가 존재한다. 일부 UI fidelity 항목은 별도 보강 필요. |
| Frontend route/API 연결 | PASS/PARTIAL | 핵심 화면은 API client와 route가 연결되어 있다. 실서비스와 동일한 정보 밀도/상호작용은 아직 부족하다. |
| EduSSAFY 홈 대시보드 parity | PARTIAL | 홈 위젯은 구현되어 있으나 실제 홈의 배치, 밀도, 우선순위, 상단 유틸리티까지 완전 동일하다고 보기 어렵다. |
| 상세 화면 visual fidelity | PARTIAL | 각 화면의 데이터 흐름은 있으나 실제 서비스의 테이블 컬럼, 필터, 탭, 상태 badge, disabled/empty/error UX는 더 세밀한 비교가 필요하다. |
| 외부 SSO/JOB/Meeting/Git | PARTIAL/EXTERNAL | clone에서는 설정 기반 링크/로그/disabled 정책까지만 현실적이다. 실제 SSO 토큰 발급은 클론 범위 밖으로 둔다. |
| 시각 회귀/e2e evidence | PARTIAL | route smoke/build/lint/test는 있으나 실서비스 대비 screenshot baseline과 browser e2e가 더 필요하다. |

### 0.3.1 아직 “클론되지 않았다”고 봐야 하는 것

새 메뉴 자체보다 아래 **UI/UX·상태·운영 fidelity**가 미완성이다.

1. **홈 정보 구조와 시각 밀도**
   - 출석체크, 출석현황, 레벨/장학포인트, 알림, 주차별 커리큘럼, Quest/평가, 학습자료, 학습중 이러닝, 자유게시판, e-book, 공지사항이 실제처럼 한 화면에서 우선순위 있게 보여야 한다.
2. **상단 헤더/메가메뉴/유틸리티 영역**
   - 대메뉴 hover/click, 전체 메뉴, 알림/프로필/외부링크/로그아웃/session expired UX가 부족하다.
3. **상세 화면의 필터/탭/테이블/상태 badge fidelity**
   - route는 있어도 실서비스의 컬럼, 정렬, 기간 필터, 카테고리 count, 검색 폼, disabled button, 권한별 액션 차이를 더 맞춰야 한다.
4. **실제 브라우저 기반 시각 검증**
   - PASS 선언을 위해서는 핵심 화면의 screenshot baseline, responsive 상태, keyboard/focus, loading/error/empty까지 검증해야 한다.
5. **외부 서비스 연동의 현실적 경계 표시**
   - JOB SSAFY, SSAFY GIT, Meeting은 실제 SSO 완전 복제 대신 설정 기반 launch, access log, 권한/disabled 상태, 새 창 정책을 명확히 해야 한다.

### 0.3.2 UI/UX Fidelity PASS 기준

화면 하나를 UI/UX PASS로 분류하려면 다음을 모두 만족해야 한다.

- 실제 backend API 또는 DB 조회 기반 데이터가 렌더링된다.
- frontend route와 API client가 연결되어 있다.
- loading, error, empty, disabled 상태가 존재한다.
- 인증 필요한 기능은 로그인 사용자/소유자/권한 기준으로 동작한다.
- 핵심 액션은 pending, success, failure feedback이 분리되어 있다.
- 목록 화면은 검색, 필터, 탭, 정렬 또는 pagination 중 실서비스에 필요한 항목을 가진다.
- 상세 화면은 상태 badge, 기간, 작성자/대상자, 첨부/링크, 변경 이력을 표시한다.
- desktop 1280px, tablet 768px, mobile 390px layout이 깨지지 않는다.
- keyboard focus, aria-label, button/link semantics가 기본 수준을 만족한다.
- backend test 또는 frontend/static/browser smoke test가 있다.
- OpenAPI/Swagger 문서가 실제 controller와 크게 어긋나지 않는다.

### 0.3.3 UI/UX 구현 스타일 가이드

원본 CSS/HTML을 복사하지 않고 구조적 fidelity만 맞춘다.

- 전체 톤: 흰색/연회색 카드 배경, 파란색 계열 primary, 작은 helper text, 촘촘한 목록형 정보 배치
- Header: 로고 영역, 대메뉴, utility link, profile/session area, 알림 진입점
- Navigation: hover/click 가능한 대메뉴 또는 all-menu overlay, active menu 강조
- Cards: 작은 제목 + 상태 badge + 핵심 숫자 + CTA 버튼 조합
- Tables: 촘촘한 row height, category/status/date/count columns, empty row 안내
- Lists: 공지/게시판/자료는 title + meta + date + count 중심
- Forms: 검색 조건, 기간 선택, select, keyword input, submit/reset button pair
- Status: 진행중/완료/마감/대기/반려/보완요청/권한없음/외부링크 불가를 badge로 일관 표시
- Mutation: 제출/취소/찜 해제/동의/신청은 confirm, pending, success, failure 상태를 분리
- Accessibility: button/link semantics, focus ring, keyboard 이동, aria-label이 필요한 icon button

### 0.3.4 UI/UX Parity 최우선 백로그

#### P0-1. 홈 대시보드 visual parity

필수 위젯:

1. 출석체크/출석현황: 오늘 입실·퇴실 가능 여부, 현재 상태, 최근 이의신청 상태, 출석 상세 링크
2. 레벨/장학포인트: 현재 레벨, 다음 레벨 progress, 포인트/EXP, 등급 색상
3. 필독 알림/공지: mandatory badge, unread count, pinned notice mini list
4. 주차별 커리큘럼: 이번 주/오늘 session, 주차/시간/강의실/교재 link
5. Quest/평가: 예정/진행중/마감 임박/제출 완료 card
6. 학습자료: carousel 또는 2열 card, category, 조회/좋아요/찜 count
7. 학습중 이러닝: 진행률 bar, 최근 학습일, 이어보기 CTA
8. 자유/익명 게시판 미니 탭: 최신글, 댓글/조회 count, 작성일
9. e-book: 사용 가능/권한 없음/disabled state

Acceptance Criteria:

- `/`에서 위 모든 섹션이 실제 API 데이터로 렌더링된다.
- 각 섹션에 loading/error/empty/disabled 상태가 있다.
- desktop/tablet/mobile responsive grid가 깨지지 않는다.
- route smoke test와 frontend build/lint가 통과한다.
- 가능하면 Playwright screenshot baseline을 추가한다.

#### P0-2. Header / global navigation / all-menu overlay

필수 항목:

- 로고/홈 링크
- 대메뉴 hover/focus/click 상태
- 하위 메뉴 panel 또는 mega menu
- 현재 route active 표시
- 알림 icon/count
- 사용자 이름/역할/profile dropdown
- 외부 서비스 링크 group
- session expired/logout UX
- mobile hamburger/all-menu overlay

Acceptance Criteria:

- keyboard로 대메뉴와 하위 메뉴 접근 가능
- mobile에서 메뉴 열기/닫기 가능
- active route 표시가 현재 route와 일치
- 로그아웃/세션 만료 시 안전하게 login으로 이동

#### P1. 마이캠퍼스 화면별 UI/UX 보강

- 레벨&장학포인트: 단계형/원형 시각화, 포인트 이력 table, 획득/차감 reason badge
- 출석현황: 월간 calendar, 일자 상세 panel, 정상/지각/결석/보강/이의신청중 badge
- 학습중 이러닝: 실제 서비스에 가까운 썸네일 비율, 운영자/학습기간/차시 meta, resume 실패 상태
- 찜한 목록: 콘텐츠 유형별 tab label/count, 삭제 confirm/toast, 삭제 실패 row 복구
- 서류제출: 파일 확장자/용량 client validation, 보완 요청 timeline, 제출 마감/검토완료 disabled state
- 교육생 서약서: version/date, 재동의 불가/증빙, 긴 원문 scroll/fold UX
- 교육현황/e-book: 학기/트랙 chart+table, 권한 없음/준비중/외부 viewer disabled state

#### P2. 강의실/학습 화면별 UI/UX 보강

- 라이브 바로가기: 오픈 전/진행중/종료/권한 없음/외부 서비스 장애 상태, Meeting access log
- 다시보기: 기간, 트랙, 주차, 강사, keyword filter, 시청 이력/만료 상태
- 커리큘럼: 오전/오후/과제/교재/Meeting link가 있는 시간표 밀도
- 필수학습: 이수 조건, 만료, 재학습, 완료 badge
- Quest/평가: Quest/평가 타입 분리, 채점중/결과공개/재제출/첨부 evidence

#### P3. 커뮤니티/HELP DESK/멘토링 UI/UX 보강

- 게시판 공통: category select, 제목/작성자/기수/반/댓글/조회/추천/작성일 columns, pinned row, 첨부 icon
- 익명 게시판: 익명 표시 규칙, 신고/블라인드/삭제 권한 상태
- HELP DESK: 공지 필독/pinned, FAQ category count/accordion/search, QNA thread/attachment/닫힘 상태
- 학사규정: category/search/detail anchor, 파일 다운로드 disabled/available
- 멘토링: 답변자 권한, 공개/익명, 모집기간/정원/중복신청/선정 결과/후기 작성 조건

#### P4. 외부 서비스/운영/문서 parity

- 외부 서비스 URL은 환경설정 기반
- 실제 SSO 토큰 발급은 미구현 범위로 명시
- launch 시 access log 저장
- 권한 없음/준비중/외부 장애/unconfigured 상태 표시
- 새 창 열기, `rel="noopener"` 정책
- `/swagger-ui/index.html`, `/v3/api-docs`, `docs/openapi.json|yaml` 동기화 검증
- Playwright 또는 Cypress 기반 browser visual smoke 확대

### 0.3.5 이번 계획 기준 PASS 선언 금지 조건

다음 중 하나라도 남아 있으면 “EduSSAFY 전체 클론 PASS”라고 말하지 않는다.

- 실제 홈과 다른 단순 카드형 dashboard만 존재
- header/menu가 실제 대메뉴 UX와 다름
- 주요 화면에 loading/error/empty/disabled 상태가 없음
- mutation action에 pending/실패 복구가 없음
- 외부 서비스가 하드코딩 URL이거나 disabled/unconfigured 상태가 없음
- 실제 controller에 없는 API가 Swagger/OpenAPI에 있음
- 브라우저 build/lint/test 또는 최소 smoke evidence가 없음
- 실제 계정/개인정보를 fixture나 문서에 저장함


## 0. 현재 결론 — 2026-04-26 실서비스 재점검 반영

2026-04-26 KST에 실제 EduSSAFY에 읽기 전용으로 로그인해 상단 메뉴, 홈 대시보드 노출 영역, 주요 외부 링크 텍스트/href를 다시 확인했다. 개인 식별자, 계정, 비밀번호, 세션 쿠키, 원본 HTML은 저장하지 않았다.

냉정한 판정은 다음과 같다.

1. **상단 대메뉴/하위메뉴 기준으로 “아예 route가 없는” 메뉴 GAP는 현재 없다.**
   - 마이캠퍼스, 강의실, 커뮤니티, HELP DESK, 멘토링 게시판, 외부 링크로 확인된 모든 메뉴는 현재 클론에 대응 route 또는 기능 흐름이 있다.
   - 이전 문서의 `없음/GAP` 표기는 과거 상태이며, 현재 코드 기준으로는 대부분 `PASS/PARTIAL`로 재분류한다.
2. 그러나 **프로덕션 레벨 전체 클론이라고 말하기에는 아직 이르다.**
   - 메뉴 존재 여부는 PASS에 가깝지만, 실제 홈 화면의 정보 밀도/위젯 구성/상세 UX/권한별 빈 상태/외부 SSO 동작은 여전히 PARTIAL이다.
3. 다음 개발은 “새 메뉴 추가”보다 **실서비스 fidelity 보강**을 우선한다.
   - 최우선은 실제 홈 대시보드 parity다. 현재 클론 홈은 요약 카드 중심이고, 실제 EduSSAFY 홈은 출석체크, 장학포인트/레벨, 알림, 주차별 커리큘럼, Quest/평가, 학습자료 캐러셀, 학습중 이러닝, 자유게시판, e-book, 공지사항을 한 화면에 배치한다.
   - 이후 각 상세 화면의 검색/필터/카운트/상세 이동/권한별 액션/빈 상태를 실제 서비스와 더 맞춘다.

### 0.1 실서비스에서 확인된 상단 메뉴와 현재 클론 판정

| 실서비스 메뉴 | 실서비스 href/동작 관찰 | 현재 클론 대응 | 냉정 판정 | 남은 작업 |
|---|---|---|---:|---|
| 마이캠퍼스 > 레벨&장학포인트 | `/edu/mycampus/mylvlmlg/myLvlMlgView.do` | `/mycampus/level` | PARTIAL | 실제 Bronze/Silver 단계 표시, 장학포인트/EXP 카드 시각 구조 추가 |
| 마이캠퍼스 > 출석현황 | `/edu/mycampus/attendance/attendanceClassList.do` | `/mycampus/attendance`, `/mycampus/attendance/appeals/new` | PASS/PARTIAL | 홈 출석체크 위젯과 주말/평일 입퇴실 가능 상태 parity |
| 마이캠퍼스 > 학습중 이러닝 | `/edu/mycampus/myknowledgecont/myKnowledgeContList.do` | `/mycampus/elearning`, `/mycampus/elearning/1` | PASS/PARTIAL | 홈 이러닝 목록 카드와 운영자/콘텐츠 메타 fidelity |
| 마이캠퍼스 > 찜한 목록 | `/edu/mycampus/selectedcont/selectedContList.do` | `/mycampus/bookmarks` | PASS/PARTIAL | 실제 콘텐츠 유형별 탭/해제 UX 비교 보강 |
| 마이캠퍼스 > 서류제출 | `/edu/board/docReq/list.do` | `/mycampus/documents` | PASS/PARTIAL | 제출 파일 이력/상태/보완 요청 상세 fidelity |
| 마이캠퍼스 > 교육생 서약서 | `/edu/mycampus/pledgedoc/pledgeDocApplyList.do` | `/mycampus/pledges`, `/mycampus/pledges/1` | PASS/PARTIAL | 서약서 원문/동의 이력/재열람 UX 보강 |
| 마이캠퍼스 > SSAFY e-book | 계정 기준 상단 href는 `#none;`, 홈에 e-book 영역 노출 | `/mycampus/ebooks`, `/mycampus/ebooks/1` | PARTIAL | disabled/권한 없음 상태와 홈 e-book 영역 처리 추가 |
| 마이캠퍼스 > 교육현황 | `/edu/mycampus/gradestatus/gradeStatusList.do` | `/mycampus/education-status` | PASS/PARTIAL | 실제 교육현황의 학기/트랙/성취 지표 구조 비교 보강 |
| 강의실 > 라이브 바로가기 | 계정 기준 href는 `#none` | `/learning/live` | PARTIAL | 비활성/오픈 전/입장 가능 상태 구분 및 Meeting 연동 로그 보강 |
| 강의실 > 내강의 다시보기 | `/edu/lectureroom/lecturereplay/lectureReplayNMyList.do` | `/learning/replays/my` | PASS/PARTIAL | 실제 내강의 필터/시청 이력/재생 가능 상태 비교 |
| 강의실 > 전체강의 다시보기 | `/edu/lectureroom/lecturereplay/lectureReplayAllList.do` | `/learning/replays/all` | PASS/PARTIAL | 공개 범위/트랙/검색 필터 fidelity |
| 강의실 > 주차별 커리큘럼 | `/edu/lectureroom/curriculumn/curriculumnWeeklyList.do` | `/learning/curriculum`, `/learning/curriculum/1` | PASS/PARTIAL | 홈 주차 테이블, 교재 링크, 시간표 다중 세션 레이아웃 보강 |
| 강의실 > Quest/평가 | `/edu/lectureroom/questevaluation/questEvaluationList.do` | `/quest`, `/quest/1`, `/quest/1/submit` | PASS/PARTIAL | 홈 예정/완료 카드, 평가/Quest 타입별 결과/기간 표시 보강 |
| 강의실 > 필수학습 | `/edu/lectureroom/essentialstudy/essentialStudyList.do` | `/learning/required-studies`, `/learning/required-studies/1` | PASS/PARTIAL | 실제 필수학습 이수 조건/만료/재학습 상태 보강 |
| 강의실 > 학습자료 | `/edu/lectureroom/openlearning/openLearningList.do` | `/learning/materials`, `/learning/materials/1`, `/learning/materials/1/viewer` | PASS/PARTIAL | 홈 캐러셀, 재생수/좋아요/찜 수, 카테고리 breadcrumb fidelity |
| 커뮤니티 > 설문조사 | `/edu/lectureroom/survey/surveyList.do` | `/survey`, `/survey/1`, `/survey/1/respond` | PASS/PARTIAL | 실제 설문 기간/응답 여부/결과 공개 상태 보강 |
| 커뮤니티 > 열린 게시판 | `/edu/board/free/list.do` | `/community/free`, `/community/free/1`, `/community/free/write` | PASS/PARTIAL | 게시판 카테고리, 홈 탭, 작성자/기수/반 표시 fidelity |
| 커뮤니티 > 익명 게시판 | `/edu/board/anonymity/list.do` | `/community/anonymous`, `/community/anonymous/1`, `/community/anonymous/write` | PASS/PARTIAL | 실제 익명 표기 규칙과 신고/블라인드 정책 보강 |
| 커뮤니티 > 우리반 보기 | `/edu/community/search/searchStudentList.do` | `/community/classmates` | PASS/PARTIAL | 실제 검색 조건, 프로필 공개 범위, 알림 권한 UX 보강 |
| HELP DESK > 공지사항 | `/edu/board/notice/list.do` | `/help/notice`, `/help/notice/1` | PASS/PARTIAL | 홈 공지 리스트, 필독/일자/pinned 표시 fidelity |
| HELP DESK > FAQ | `/edu/board/faq/list.do` | `/help/faq`, `/help/faq/1` | PASS/PARTIAL | FAQ 카테고리 count와 accordion/search parity |
| HELP DESK > 1:1 문의 | `/edu/board/qna/list.do` | `/help/qna`, `/help/qna/new`, `/help/qna/tickets/1` | PASS/PARTIAL | 문의 thread/첨부/답변 상태 세부 UI 보강 |
| HELP DESK > 학사규정 | `/edu/board/rule/list.do` | `/help/academic-rules`, `/help/rules` | PASS/PARTIAL | 실제 규정 카테고리/검색/상세 앵커 구조 보강 |
| 멘토링 게시판 > 멘토 스토리 | `/edu/board/mentoState/list.do` | `/mentoring/stories`, `/mentoring/stories/1` | PASS/PARTIAL | 홈 알림 연계, 필독/공지성 스토리 표시 보강 |
| 멘토링 게시판 > 멘토링 | `/edu/board/mentoQna/list.do` | `/mentoring/questions`, `/mentoring/questions/new`, `/mentoring/questions/1` | PASS/PARTIAL | 멘토 답변 권한, 상태, 공개/익명 정책 세부 보강 |
| 멘토링 게시판 > 멘토링 공지사항 | `/edu/board/mentoNotice/list.do` | `/mentoring/notices`, `/mentoring/notices/1` | PASS/PARTIAL | 필독/카테고리/검색/상세 이동 fidelity |
| 멘토링 게시판 > 간담회 신청 | `/edu/community/meeting/menti/applyList.do` | `/mentoring/meetings`, `/mentoring/meetings/1`, `/mentoring/meetings/my-applications` | PASS/PARTIAL | 모집 기간/정원/중복 신청/취소 UX 세부 비교 |
| 멘토링 게시판 > 간담회 정보 | `/edu/community/meeting/menti/result.do` | `/mentoring/meeting-results`, `/mentoring/meeting-results/993` | PASS/PARTIAL | 선정/참석 결과, 종료 상태, 자료 링크 fidelity |
| 멘토링 게시판 > 간담회 후기 | `/edu/board/mentoReview/list.do` | `/mentoring/meeting-reviews`, `/mentoring/meeting-reviews/write`, `/mentoring/meeting-reviews/1301` | PASS/PARTIAL | 후기 작성 가능 조건, 평점/수정/삭제 권한 세부 보강 |
| 외부 링크 > JOB SSAFY | `/comm/login/SecurityJobLoginSSOForm.do` | `/external-services` 카드/로그 | PARTIAL/EXTERNAL | 실제 SSO는 미구현. launch link, disabled 상태, audit log까지만 clone |
| 외부 링크 > SSAFY GIT | `https://project.ssafy.com` | `/external-services` 카드/로그 | PARTIAL/EXTERNAL | 역할별 접근 가능/disabled/새 창 열기 세부 보강 |
| 외부 링크 > Meeting! SSAFY | `https://meeting.ssafy.com` | `/external-services`, `/learning/live` | PARTIAL/EXTERNAL | 라이브 세션 join URL과 Meeting access log 연동 강화 |

### 0.2 냉정한 남은 우선순위

현재 다음 라운드에서 가장 가치가 큰 작업은 아래 순서다.

1. **P0 홈 대시보드 실서비스 fidelity**
   - 실제 홈에 있는 출석체크 & 현황, 장학포인트/레벨, 알림, 주차별 커리큘럼 표, Quest/평가 카드, 학습자료 캐러셀, 학습중 이러닝, 자유게시판 탭, e-book, 공지사항을 clone 홈에 통합한다.
   - 현재 `DashboardPage`는 요약 카드/주요 서비스 링크 중심이라 실서비스 홈과 정보 구조가 가장 크게 다르다.
2. **P1 학습자료/커리큘럼/Quest 홈 위젯 parity**
   - 메인 홈에서 바로 보여지는 세 영역의 시각 구조, 더보기 링크, `상세보기`, `More`, 재생수/좋아요/찜 수를 보강한다.
3. **P2 외부 링크/SSO 현실화**
   - JOB SSAFY는 실제 SSO 경로, SSAFY GIT/Meeting은 외부 URL이다. 실제 SSO 토큰 발급은 clone 범위 밖으로 두되, 설정 기반 URL, disabled 상태, access log, 새 창 정책을 명확히 한다.
4. **P3 시각 회귀 확대**
   - 현재 기능 route는 많지만 실제 EduSSAFY 홈과 상세 화면의 픽셀/구조 fidelity를 계속 검증해야 한다. 홈 대시보드가 추가되면 visual baseline을 반드시 갱신한다.

---

## 1. 실제 EduSSAFY 확인 기반 메뉴 매핑

### 1.1 실제 EduSSAFY에서 확인된 대분류

| 실제 대분류 | 실제 하위 메뉴 | 현재 클론 상태 | 판정 |
|---|---|---:|---:|
| 마이캠퍼스 | 레벨&장학포인트 | `/mycampus/level` 존재 | PARTIAL |
| 마이캠퍼스 | 출석현황 | `/mycampus/attendance` 존재 | PASS/PARTIAL |
| 마이캠퍼스 | 학습중 이러닝 | `/mycampus/elearning`, `/mycampus/elearning/1` 존재 | PASS/PARTIAL |
| 마이캠퍼스 | 찜한 목록 | `/mycampus/bookmarks` 존재 | PASS/PARTIAL |
| 마이캠퍼스 | 서류제출 | `/mycampus/documents` 존재 | PASS/PARTIAL |
| 마이캠퍼스 | 교육생 서약서 | `/mycampus/pledges`, `/mycampus/pledges/1` 존재 | PASS/PARTIAL |
| 마이캠퍼스 | SSAFY e-book | `/mycampus/ebooks`, `/mycampus/ebooks/1` 존재 | PARTIAL |
| 마이캠퍼스 | 교육현황 | `/mycampus/education-status` 존재 | PASS/PARTIAL |
| 강의실 | 라이브 바로가기 | `/learning/live` 존재 | PARTIAL |
| 강의실 | 내강의 다시보기 | `/learning/replays/my` 존재 | PASS/PARTIAL |
| 강의실 | 전체강의 다시보기 | `/learning/replays/all` 존재 | PASS/PARTIAL |
| 강의실 | 주차별 커리큘럼 | `/learning/curriculum`, `/learning/curriculum/1` 존재 | PASS/PARTIAL |
| 강의실 | Quest/평가 | `/quest` 존재 | PARTIAL |
| 강의실 | 필수학습 | `/learning/required-studies`, `/learning/required-studies/1` 존재 | PASS/PARTIAL |
| 강의실 | 학습자료 | `/learning/materials` 존재 | PASS/PARTIAL |
| 커뮤니티 | 설문조사 | `/survey` 존재 | PASS/PARTIAL |
| 커뮤니티 | 열린 게시판 | `/community/free` 존재 | PASS/PARTIAL |
| 커뮤니티 | 익명 게시판 | `/community/anonymous`, `/community/anonymous/1`, `/community/anonymous/write` 존재 | PASS/PARTIAL |
| 커뮤니티 | 우리반 보기 | `/community/classmates` 존재 | PARTIAL |
| HELP DESK | 공지사항 | `/help/notice`, `/help/notice/1` 존재 | PASS/PARTIAL |
| HELP DESK | FAQ | `/help/faq`, `/help/faq/1` 존재 | PASS/PARTIAL |
| HELP DESK | 1:1 문의 | `/help/qna` 존재 | PASS/PARTIAL |
| HELP DESK | 학사규정 | `/help/academic-rules`, `/help/rules` 존재 | PASS/PARTIAL |
| 멘토링 게시판 | 멘토 스토리 | `/mentoring/stories`, `/mentoring/stories/1` 존재 | PASS/PARTIAL |
| 멘토링 게시판 | 멘토링 | `/mentoring/questions`, `/mentoring/questions/new`, `/mentoring/questions/1` 존재 | PASS/PARTIAL |
| 멘토링 게시판 | 멘토링 공지사항 | `/mentoring/notices`, `/mentoring/notices/1` 존재 | PASS/PARTIAL |
| 멘토링 게시판 | 간담회 신청 | `/mentoring/meetings`, `/mentoring/meetings/1`, `/mentoring/meetings/my-applications` 존재 | PASS/PARTIAL |
| 멘토링 게시판 | 간담회 정보 | `/mentoring/meeting-results`, `/mentoring/meeting-results/993` 존재 | PASS/PARTIAL |
| 멘토링 게시판 | 간담회 후기 | `/mentoring/meeting-reviews`, `/mentoring/meeting-reviews/write`, `/mentoring/meeting-reviews/1301` 존재 | PASS/PARTIAL |
| 외부 링크 | JOB SSAFY | `/external-services`에서 launch link/audit log 제공 | PARTIAL/EXTERNAL |
| 외부 링크 | SSAFY GIT | `/external-services`에서 launch link/audit log 제공 | PARTIAL/EXTERNAL |
| 외부 링크 | Meeting! SSAFY | `/external-services`, `/learning/live`와 연결 | PARTIAL/EXTERNAL |

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

2026-04-26 재점검 기준, 실제 화면에서 보이는 상단 메뉴는 모두 클론 route가 생겼다. 이 섹션은 “미구현 메뉴 추가”가 아니라 “해당 메뉴의 실서비스 fidelity를 PASS 수준으로 올리는 보강 backlog”로 해석한다.

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

실제 EduSSAFY에는 `마이캠퍼스 > 학습중 이러닝` 메뉴가 있다. 현재 클론에는 `/api/elearning/in-progress`, 상세, resume API와 `/mycampus/elearning`, `/mycampus/elearning/:courseId` route가 있다. 기능 흐름은 PASS에 가깝지만, 실제 화면과 같은 썸네일 비율/운영자 meta/홈 미니 위젯/외부 viewer 실패 UX는 아직 PARTIAL이다.

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

실제 EduSSAFY에는 `마이캠퍼스 > 찜한 목록`이 있다. 현재 클론에는 `/api/me/bookmarks` list/create/delete와 `/mycampus/bookmarks` route가 있다. 통합 찜 목록의 DB flow는 구현되었지만, 실서비스의 콘텐츠 유형 tab label/count, 삭제 confirm/toast, 실패 복구, 이동 대상 unavailable state는 UI/UX PARTIAL이다.

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

실제 EduSSAFY에는 `마이캠퍼스 > 서류제출`이 있다. 현재 클론에는 `/api/documents/requests`, 상세, 제출, 취소, attachment download API와 `/mycampus/documents`, `/mycampus/documents/:requestId` route가 있다. 제출 이력/상태/취소 pending guard는 구현되었지만, 파일 확장자/용량 client validation, 보완 요청 timeline, 제출 마감/검토 완료/반려 상태별 action disable은 UI/UX PARTIAL이다.

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

## 5. 다음 실행 순서 추천 — 2026-04-26 재점검 후

이제 상단 메뉴 route 추가는 1차로 완료된 상태다. 다음 라운드는 “없는 메뉴 만들기”가 아니라 실제 EduSSAFY 홈/상세 화면과의 차이를 줄이는 fidelity 보강으로 진행한다.

### Round 1: 홈 대시보드 parity

1. `feat(dashboard): implement EduSSAFY home widgets`
   - 출석체크 & 현황 카드
   - 장학포인트/레벨&경험치 카드
   - 알림 필독 리스트
   - 주차별 커리큘럼 표
   - Quest/평가 예정/완료 카드
   - 학습자료 캐러셀/리스트
   - 학습중 이러닝 미니 리스트
   - 자유게시판 탭/미니 리스트
   - e-book 영역
   - 공지사항 리스트
2. `test(e2e): cover dashboard live-fidelity widgets`
3. `test(visual): refresh dashboard visual baseline`

### Round 2: 홈 위젯과 상세 화면 연결 강화

1. `feat(curriculum): align dashboard weekly timetable`
2. `feat(materials): add dashboard material carousel metrics`
3. `feat(quest): expose dashboard quest evaluation cards`
4. `feat(notice): add dashboard notice and mandatory alerts`

### Round 3: 상세 화면 fidelity 보강

1. `feat(level): align level scholarship visual states`
2. `feat(learning): refine live disabled and join states`
3. `feat(ebook): support disabled and home ebook states`
4. `feat(board): align anonymous board safety states`
5. `feat(helpdesk): align academic rules anchors and search`

### Round 4: 외부 서비스/운영 검증

1. `feat(external): refine SSO launch and disabled policies`
2. `chore(obs): add live observability smoke evidence hooks`
3. `chore(ci): record hosted CI sign-off checklist`

---

## 6. 첫 번째로 바로 구현할 기능 추천

### 추천 1순위: 홈 대시보드 실서비스 fidelity

이유:

- 실제 EduSSAFY 로그인 직후 가장 먼저 보이는 화면이며, 현재 클론과 정보 구조 차이가 가장 크다.
- 상단 메뉴는 모두 대응 route가 생겼으므로, 이제 “메뉴 존재”보다 “홈에서 실제처럼 요약/진입/상태를 보여주는가”가 프로덕션 레벨 판단 기준이다.
- 이미 구현된 attendance, level, notification, curriculum, quest, materials, elearning, board, ebook, notice API/화면을 재사용할 수 있어 새 도메인보다 리스크가 낮다.
- 이 작업이 끝나야 visual baseline과 실제 서비스 비교가 의미 있게 작동한다.

### Acceptance Criteria

Backend/API:

- `GET /api/dashboard` 또는 기존 dashboard summary가 실제 홈 위젯에 필요한 데이터를 제공한다.
- 출석체크 상태, 레벨/포인트, 알림, 주차별 커리큘럼, Quest/평가, 학습자료, 학습중 이러닝, 게시판, e-book, 공지사항 요약이 DB 조회 기반으로 반환된다.
- 빈 데이터/권한 없음/외부 링크 비활성 상태를 명확하게 표현한다.

Frontend:

- `/` DashboardPage가 실제 EduSSAFY 홈에 가까운 섹션 구조를 가진다.
- 각 위젯은 loading/error/empty 상태를 갖는다.
- `더보기`, `More`, `상세보기`, 외부 링크 진입이 현재 클론 route 또는 설정 기반 외부 URL로 연결된다.
- 개인정보/실제 계정 식별자는 화면 fixture나 테스트에 저장하지 않는다.

Test/Verification:

- dashboard API 또는 service/controller test가 추가된다.
- Playwright core flow에 홈 위젯 smoke가 추가된다.
- visual baseline 대상에 홈 대시보드가 포함된다.
- `git diff --check`, backend targeted/full test, frontend build/lint, docker compose config를 통과한다.

예상 변경 파일:

Backend:

- `backend/src/main/java/com/edussafy/backend/priority/api/PriorityApiController.java` 또는 dashboard controller
- `backend/src/main/java/com/edussafy/backend/priority/service/PriorityApiService.java`
- `backend/src/main/java/com/edussafy/backend/priority/repository/PriorityApiRepository.java`
- `backend/src/main/java/com/edussafy/backend/priority/dto/PriorityDtos.java`
- dashboard 관련 test

Frontend:

- `frontend/src/pages/DashboardPage.tsx`
- `frontend/src/api/app.ts`
- `frontend/src/types.ts`
- `frontend/e2e/core-flows.spec.ts`
- visual baseline spec/snapshots if layout changes

주의:

- 실제 EduSSAFY 계정/쿠키/HTML을 test fixture나 문서에 저장하지 않는다.
- 실서비스 텍스트는 구조 판별용으로만 요약하고, 원본 콘텐츠 복제는 하지 않는다.
- 새 대형 도메인을 만들기보다 기존 API를 조합해 홈 fidelity를 높인다.
