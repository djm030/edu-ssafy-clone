
# SSAFY Full Clone - OMX Team Execute Prompt

너희는 5명의 executor 팀이다.

이 프롬프트는 설명서가 아니라 실행 명령이다.
OMX team 실행 시 반드시 실제 작업 task를 생성하고, worker에게 배정하고, 코드 수정/테스트/커밋/문서화를 수행해야 한다.

---

## 0. 절대 규칙

반드시 지켜라.

1. task 없이 종료하지 마라.
2. `tasks: total=0` 상태로 complete 처리하지 마라.
3. 분석만 하고 종료하지 마라.
4. 계획만 작성하고 종료하지 마라.
5. 문서만 읽고 종료하지 마라.
6. 구현 없이 완료 처리하지 마라.
7. 모든 worker는 최소 1개 이상의 task를 가져야 한다.
8. 첫 실행 단계에서 반드시 15개 이상의 task를 생성하라.
9. task를 생성할 수 없으면 테스트, 문서, 환경 개선 task라도 생성하라.
10. 각 task는 실제 파일 변경을 포함해야 한다.
11. 각 task 완료 후 가능한 검증을 수행하라.
12. 각 task 완료 후 커밋하라.
13. 라운드 종료 시 문서를 갱신하라.
14. 최종 검증 전까지 완료 선언하지 마라.

---

## 1. 목표

SSAFY 교육 플랫폼 풀 클론을 실제 동작 가능한 수준으로 구현한다.

구현 범위는 다음과 같다.

- 인증/인가
- 사용자 프로필
- 캠퍼스/기수/반/트랙
- 출석 조회 및 이의신청
- 알림 발송/수신/읽음
- 커리큘럼 일정
- 강의 다시보기
- 학습자료/리소스/첨부파일/반응
- 퀘스트/평가/제출 상태
- 설문/문항/선택지/응답 저장
- 게시판/카테고리/게시글/댓글/첨부파일/반응
- 1:1 문의/답변/첨부파일
- 권한별 접근 제어
- 기본 에러 처리
- 로컬 실행 환경
- 테스트 또는 smoke test
- README 및 문서 최신화

---

## 2. 첫 번째로 반드시 할 일

현재 저장소를 확인한 뒤 즉시 task를 생성하라.

반드시 최소 15개 이상의 task를 만들어라.

각 task는 아래 형식을 가져야 한다.

task_id:
task_title:
assigned_worker:
domain:
expected_files:
completion_condition:
verification_method:
commit_message:

---

## 3. worker 역할

### worker-1: Backend Core

- 인증/인가
- 사용자
- 프로필
- 권한 구조
- 공통 API 응답
- 예외 처리
- Backend 기본 테스트

### worker-2: Backend Domain

- 출석
- 알림
- 커리큘럼
- 강의 다시보기
- 학습자료
- 퀘스트
- 설문
- 게시판
- 문의
- 첨부파일

### worker-3: Frontend Pages

- 로그인 화면
- 메인 화면
- 마이페이지
- 출석 화면
- 학습자료 화면
- 설문 화면
- 게시판 화면
- 문의 화면
- 알림 화면

### worker-4: Frontend Integration

- API client
- 인증 상태 관리
- 로딩 상태
- 빈 화면
- 에러 상태
- 권한 없음 상태
- 폼 검증

### worker-5: DevOps / QA

- Docker
- 환경변수
- 실행 스크립트
- 테스트 스크립트
- lint
- typecheck
- build
- smoke test
- 문서 검증

---

## 4. 필수 task 생성 기준

아래 task는 반드시 생성하라.

T01 저장소 구조 분석 및 구현 상태 문서화
T02 Backend 인증/사용자/프로필 API 점검 및 구현
T03 Backend 출석/이의신청 API 구현
T04 Backend 알림 API 구현
T05 Backend 학습자료/강의/커리큘럼 API 구현
T06 Backend 퀘스트/제출 API 구현
T07 Backend 설문/응답 API 구현
T08 Backend 게시판/댓글/반응 API 구현
T09 Backend 문의/답변/첨부파일 API 구현
T10 Frontend 라우팅/레이아웃 구성
T11 Frontend 로그인/마이페이지 구현
T12 Frontend 출석/학습자료/설문 화면 구현
T13 Frontend 게시판/문의/알림 화면 구현
T14 Frontend API client 및 상태 처리 구현
T15 DevOps 로컬 실행/테스트/smoke test 구성
T16 문서 갱신 및 최종 라운드 요약

필요하면 T17, T18, T19를 추가하라.

---

## 5. 실행 순서

1. 저장소 분석
2. 최소 15개 task 생성
3. worker별 task 배정
4. 실제 코드 수정
5. 검증
6. 커밋
7. 문서 갱신
8. 남은 작업 정리

---

## 6. 필수 문서

아래 문서를 작성하거나 갱신하라.

- docs/progress.md
- docs/architecture.md
- docs/api-summary.md
- docs/test-report.md
- docs/remaining-work.md

---

## 7. 커밋 규칙

Conventional Commits 형식을 사용한다.

예:

- feat(auth): implement login API
- feat(user): add profile update endpoint
- feat(attendance): add attendance appeal flow
- feat(board): implement post and comment APIs
- feat(survey): implement survey response save
- feat(ui): add learning material pages
- test(smoke): add full clone smoke test
- docs(progress): update implementation summary
- fix(build): resolve frontend build error

---

## 8. 실패 처리

실패하면 다음 순서로 처리하라.

1. 실패 로그 확인
2. 원인 분류
3. 수정
4. 재검증
5. docs/test-report.md 기록
6. 커밋

---

## 9. 최종 지시

지금 즉시 다음을 수행하라.

1. 현재 저장소를 분석하라.
2. 최소 15개 이상의 task를 생성하라.
3. 5명의 worker에게 task를 분배하라.
4. 각 worker는 실제 파일을 수정하라.
5. 검증을 수행하라.
6. 커밋하라.
7. 문서를 갱신하라.
8. 남은 작업을 정리하라.
9. task 없이 종료하지 마라.
10. `tasks: total=0` 상태로 complete 처리하지 마라.
