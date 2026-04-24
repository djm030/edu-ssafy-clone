승인된 계획을 실행한다.

너희는 5명의 executor 팀이다.
PM 관점의 조율을 유지하면서 Backend, Frontend, DevOps/QA 작업을 병렬로 수행한다.

목표:
SSAFY 교육 플랫폼 풀 클론을 실제 동작 가능한 수준으로 구현한다.

실행 원칙:

1. 먼저 승인된 계획과 현재 저장소 상태를 확인한다.
2. 작업을 작은 단위로 수행한다.
3. 하나의 의미 있는 작업이 끝날 때마다 커밋한다.
4. 커밋 전 가능한 검증을 수행한다.
5. 실패하면 로그를 분석하고 수정한다.
6. 테스트 하네스가 부족하면 먼저 추가한다.
7. 구현 내용과 검증 결과를 문서에 기록한다.
8. 라운드 끝마다 남은 작업을 docs/remaining-work.md에 갱신한다.

역할 분배:

- Executor 1: Backend 인증/사용자/권한/API 기반 구조
- Executor 2: Backend 도메인 기능 구현
  - 출석
  - 학습자료
  - 퀘스트
  - 설문
  - 게시판
  - 문의
  - 알림
  - 첨부파일
- Executor 3: Frontend 화면 구현
  - 로그인
  - 메인
  - 마이페이지
  - 출석
  - 학습자료
  - 설문
  - 게시판
  - 문의
  - 알림
- Executor 4: Frontend API 연동 및 상태 처리
  - 로딩
  - 빈 화면
  - 에러
  - 권한 없음
  - 폼 검증
- Executor 5: DevOps/QA
  - Docker
  - env
  - 실행 스크립트
  - 테스트 스크립트
  - lint/build/typecheck
  - smoke test
  - 문서 검증

커밋 규칙:
Conventional Commits 형식을 사용한다.

예:

- feat(auth): implement login API
- feat(user): add profile update endpoint
- feat(attendance): add attendance appeal flow
- feat(board): implement post and comment APIs
- feat(survey): implement survey response save
- feat(ui): add learning material pages
- test(api): add smoke tests
- docs(progress): update implementation summary
- fix(build): resolve frontend build error

금지:

- 서로 다른 도메인 기능을 한 커밋에 섞지 않는다.
- 검증하지 않은 상태로 완료 처리하지 않는다.
- 문서 갱신 없이 라운드를 끝내지 않는다.
- 실패 로그를 무시하지 않는다.
- 임시 mock만 만들고 완료 처리하지 않는다.

필수 문서 갱신:
각 라운드 종료 시 아래 문서를 갱신한다.

1. docs/progress.md

   - 이번 라운드 목표
   - 완료 작업
   - 커밋 목록
   - 변경 파일 요약
2. docs/architecture.md

   - 전체 구조
   - Backend/Frontend 연결 방식
   - DB/API 구조
   - 파일 업로드 구조
3. docs/api-summary.md

   - 엔드포인트
   - 메서드
   - 요청/응답 요약
   - 인증 필요 여부
4. docs/test-report.md

   - 실행한 명령어
   - 성공/실패 결과
   - 실패 원인
   - 수정 내역
   - 재검증 결과
5. docs/remaining-work.md

   - 아직 미완성 기능
   - 다음 라운드 작업 후보
   - 위험 요소
   - 완료 판단

검증 명령:
저장소의 package manager와 빌드 도구를 확인한 뒤 적절히 실행한다.

가능한 경우:

- backend test
- frontend test
- lint
- typecheck
- build
- API smoke test
- docker compose up 검증

실패 처리:

- 실패 로그를 읽는다.
- 원인을 분류한다.
  - 코드 오류
  - 타입 오류
  - 빌드 설정 오류
  - 환경변수 누락
  - 테스트 하네스 부족
  - 의존성 문제
- 수정한다.
- 다시 검증한다.
- docs/test-report.md에 남긴다.

완료 기준:
다음이 모두 만족될 때만 완료로 판단한다.

- 로그인 가능
- 사용자 프로필 조회/수정 가능
- 캠퍼스/기수/반/트랙 구조 동작
- 출석 조회 및 이의신청 동작
- 알림 발송/수신/읽음 동작
- 커리큘럼 일정 조회 동작
- 강의 다시보기 조회 동작
- 학습자료/리소스/첨부파일/반응 동작
- 퀘스트/평가/제출 상태 동작
- 설문/문항/선택지/응답 저장 동작
- 게시판/카테고리/게시글/댓글/첨부파일/반응 동작
- 1:1 문의/답변/첨부파일 동작
- 권한별 접근 제어 적용
- 기본 에러 처리 적용
- 로컬에서 한 번의 명령으로 실행 가능
- 테스트 또는 smoke test 존재
- README와 실행 문서 최신화

라운드 종료 시:

- 남은 풀 클론 범위를 다시 점검한다.
- 미완성 기능이 있으면 다음 라운드 계획을 docs/remaining-work.md에 작성한다.
- 완료된 것처럼 보이더라도 최종 검증 전까지는 완료 선언하지 않는다.

지금부터 승인된 계획에 따라 실제 코드를 수정하고, 테스트하고, 커밋하고, 문서를 갱신하라.
