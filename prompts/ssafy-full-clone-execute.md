# SSAFY Full Clone - OMX Continuous Execute Prompt

너희는 SSAFY 교육 플랫폼 풀스택 클론 프로젝트를 끝까지 완성하는 team이다.

이 프롬프트는 설명서가 아니라 실행 명령이다.

OMX team 실행 시 반드시 실제 작업 task를 생성하고, worker에게 배정하고, 코드 수정/테스트/커밋/문서화를 수행해야 한다.

현재 저장소에는 Docker 설정 파일만 존재할 수 있다. Docker 설정만 있고 Backend/Frontend 코드가 없다면, 기존 Docker 설정을 재사용하면서 Spring Boot Backend와 React/Tailwind Frontend를 새로 생성한다.

---

## 0. 핵심 실행 원칙

이 실행은 라운드 단위로 종료하지 않는다.

다음 루프를 SSAFY 풀 클론 완료 조건이 모두 PASS가 될 때까지 반복한다.

```text
1. 현재 상태 확인
2. 완료된 기능 확인
3. 미완성 기능 확인
4. 미완성 기능을 task로 생성
5. task를 worker에게 배정
6. 코드 수정
7. 테스트 또는 검증
8. 커밋
9. 문서 갱신
10. 완료 조건 재검사
11. 남은 작업이 있으면 다시 3번으로 돌아감
```

중요:

- 한 번 작업하고 종료하지 마라.
- 라운드 종료라는 이유로 멈추지 마라.
- 남은 작업이 있으면 즉시 다음 task를 생성하라.
- 모든 기능이 PASS가 되기 전에는 complete 처리하지 마라.
- `docs/remaining-work.md`에 필수 작업이 남아 있으면 완료가 아니다.
- `docs/final-verification.md`에서 모든 핵심 항목이 PASS가 아니면 완료가 아니다.
- 단, 한 번의 team 실행에서 처리 가능한 범위를 초과하면 완료라고 선언하지 말고 남은 작업을 명확한 task로 문서화한 뒤 다음 실행이 이어서 처리할 수 있게 한다.

---

## 1. 팀 구성

이번 실행은 다음 구조를 전제로 한다.

```text
1:product-manager
1:architect
2:executor
1:test-engineer
```

역할:

| 역할            | 책임                                                           |
| --------------- | -------------------------------------------------------------- |
| product-manager | PM, 범위 관리, 남은 작업 판단, 완료 착각 방지                  |
| architect       | 구조 설계, API/UI 계약, Docker와 애플리케이션 연결 구조 조율   |
| executor-1      | Backend 중심 구현: Spring Boot, MySQL, Redis, RabbitMQ 연동    |
| executor-2      | Frontend 중심 구현: React, Tailwind CSS, API client, 상태 처리 |
| test-engineer   | DevOps/QA: Docker Compose, Nginx, ELK, smoke test, 문서 검증   |

---

## 2. 확정 기술 스택 및 기존 Docker 설정 준수

모든 worker는 아래 기술 스택을 기준으로 작업한다.

- Backend: Java / Spring Boot
- Frontend: React
- Styling: Tailwind CSS
- Database: MySQL
- Cache/Session: Redis
- Message Broker: RabbitMQ
- Logging/Monitoring: ELK Stack
- Web Server: Nginx Reverse Proxy
- Local Infra: Docker Compose

중요 규칙:

1. 기존 Docker 설정 파일을 먼저 확인한다.
2. 기존 Docker 설정을 삭제하거나 대체하지 않는다.
3. 기존 서비스명, 포트, 네트워크, 볼륨을 임의로 바꾸지 않는다.
4. 같은 목적의 compose 파일을 중복 생성하지 않는다.
5. MySQL, Redis, RabbitMQ, ELK, Nginx가 이미 있으면 재사용한다.
6. Docker 설정만 있고 Backend/Frontend가 없으면 애플리케이션 코드를 새로 생성한다.
7. Backend/Frontend 설정이 기존 Docker 서비스명과 맞지 않을 때만 수정한다.
8. 실행 실패가 있으면 docker compose logs를 확인하고 최소 수정한다.
9. 수정 내용은 docs/architecture.md와 docs/test-report.md에 기록한다.
10. 인프라 수정만 하고 종료하지 말고 핵심 기능 구현으로 이어간다.

---

## 3. 절대 금지

다음은 금지한다.

1. task 없이 종료
2. `tasks: total=0` 상태로 complete 처리
3. 분석만 하고 종료
4. 계획만 작성하고 종료
5. 문서만 읽고 종료
6. 문서만 갱신하고 종료
7. 구현 없이 완료 처리
8. mock-only 구현을 완료로 처리
9. TODO만 남기고 완료 처리
10. 테스트 실패 상태에서 종료
11. 빌드 실패 상태에서 완료 처리
12. worker가 최소 1개 task도 수행하지 않고 종료
13. PM이 PASS/PARTIAL/FAIL/UNKNOWN 표 없이 완료 판단
14. `docs/remaining-work.md`에 필수 작업이 있는데 완료 선언
15. 최종 검증 없이 완료 선언
16. Tailwind CSS 외의 CSS 프레임워크를 임의로 도입
17. 기존 Docker 설정을 확인하지 않고 새 인프라 파일 생성
18. Docker 설정만 있는 상태를 구현 완료로 판단

---

## 4. 첫 번째로 반드시 할 일

현재 저장소를 확인한 뒤 즉시 task를 생성하라.

반드시 최소 18개 이상의 초기 task를 만들어라.

각 task는 아래 형식을 가져야 한다.

```text
task_id:
task_title:
assigned_worker:
domain:
dependencies:
expected_files:
completion_condition:
verification_method:
commit_message:
```

task가 0개면 실행 실패다.

---

## 5. 필수 초기 task

아래 task는 반드시 생성하라.

```text
T01 저장소 구조 및 현재 구현 상태 분석
T02 기존 Docker Compose 설정 검증 및 서비스명/포트/네트워크 확인
T03 Backend Spring Boot 프로젝트 존재 여부 확인 및 없으면 scaffold 생성
T04 Frontend React/Tailwind 프로젝트 존재 여부 확인 및 없으면 scaffold 생성
T05 MySQL schema/migration 또는 초기 DDL 구성
T06 Backend 인증/사용자/프로필 API 점검 및 구현
T07 Backend 출석/이의신청 API 구현
T08 Backend 알림 API 구현 및 RabbitMQ 연동 또는 대체 동작 명시
T09 Backend 커리큘럼/강의/학습자료 API 구현
T10 Backend 퀘스트/제출 API 구현
T11 Backend 설문/응답 API 구현
T12 Backend 게시판/댓글/반응 API 구현
T13 Backend 문의/답변/첨부파일 API 구현
T14 Frontend 라우팅/레이아웃 구성 및 Tailwind 적용
T15 Frontend 로그인/마이페이지 구현
T16 Frontend 출석/학습자료/설문 화면 구현
T17 Frontend 게시판/문의/알림 화면 구현
T18 Frontend API client 및 로딩/빈 화면/에러/권한 없음 처리
T19 DevOps 로컬 실행/테스트/smoke test 구성
T20 Nginx Reverse Proxy 연결 검증
T21 ELK 로그 수집 가능 여부 확인 및 문서화
T22 문서 갱신
T23 최종 검증표 작성 및 남은 작업 재검사
```

필요하면 T24, T25, T26을 추가하라.

---

## 6. PM 완료 착각 방지 체크리스트

product-manager는 모든 기능을 아래 기준으로 판정한다.

```text
PASS
PARTIAL
FAIL
UNKNOWN
```

### 6.1 PASS 조건

아래 조건을 모두 만족해야 PASS다.

1. Backend 구현이 존재한다.
2. Frontend 화면 또는 호출 지점이 존재한다.
3. Backend와 Frontend가 실제로 연결되어 있다.
4. 인증/인가가 필요한 기능은 권한 검사가 있다.
5. 정상 응답 처리가 있다.
6. 에러 응답 처리가 있다.
7. 빈 데이터 상태 처리가 있다.
8. 로딩 상태 처리가 있다.
9. 가능한 테스트 또는 smoke test가 있다.
10. 문서에 API/화면/검증 결과가 반영되어 있다.
11. 관련 변경이 커밋되어 있다.
12. mock-only 구현이 아니다.
13. TODO만 남겨둔 상태가 아니다.
14. 실행 또는 빌드 검증에서 치명적 실패가 없다.
15. Docker 설정과 실제 애플리케이션 설정이 연결되어 있다.

### 6.2 PARTIAL 조건

다음 중 하나라도 해당하면 PARTIAL이다.

- Docker 설정만 있고 Backend/Frontend 코드가 없다.
- Backend만 있고 Frontend 연동이 없다.
- Frontend 화면만 있고 API 연동이 없다.
- mock 데이터만 사용한다.
- 정상 케이스만 되고 에러 처리가 없다.
- 권한 검사가 없다.
- 테스트 또는 smoke test가 없다.
- 문서가 실제 코드와 맞지 않는다.
- 기능은 있지만 UX 상태 처리가 부족하다.
- 파일 업로드/첨부파일이 실제 저장 흐름 없이 형식만 있다.

### 6.3 FAIL 조건

다음 중 하나라도 해당하면 FAIL이다.

- 기능 구현이 없다.
- 실행 시 크래시가 난다.
- 빌드가 실패한다.
- API가 호출되지 않는다.
- DB schema와 코드가 맞지 않는다.
- 화면에서 해당 기능에 접근할 수 없다.
- 테스트가 계속 실패한다.
- 인증이 필요한 기능이 공개되어 있다.
- 데이터 저장이 되지 않는다.
- Docker compose 설정이 깨져 있다.

### 6.4 UNKNOWN 조건

다음 중 하나라도 해당하면 UNKNOWN이다.

- 파일을 확인하지 않았다.
- 실행 검증을 하지 않았다.
- 구현 여부를 문서만 보고 판단했다.
- API 응답 형태를 확인하지 않았다.
- 실제 화면 접근 경로를 확인하지 않았다.
- 테스트 방법이 불명확하다.
- Docker 설정이 실제 실행되는지 확인하지 않았다.

UNKNOWN은 완료가 아니다. UNKNOWN이 있으면 반드시 조사 task를 생성하라.

---

## 7. 도메인별 완료 기준

### 7.1 인증/인가

PASS 조건:

- 로그인 API 또는 인증 흐름 존재
- JWT 또는 세션 처리 존재
- 로그인 실패 처리 존재
- 인증 필요 API 보호
- Frontend 로그인 화면 연동
- 로그아웃 또는 인증 상태 초기화 가능
- 권한 없는 접근 처리

### 7.2 사용자 프로필

PASS 조건:

- 사용자 정보 조회 가능
- 사용자 정보 수정 가능
- Frontend 마이페이지 연동
- 수정 실패/검증 오류 처리
- 문서 반영

### 7.3 캠퍼스/기수/반/트랙

PASS 조건:

- 캠퍼스 조회 가능
- 기수 조회 가능
- 반 조회 가능
- 트랙 조회 가능
- 사용자와 반/트랙 연결 가능
- 화면 또는 API에서 조회 가능

### 7.4 출석/이의신청

PASS 조건:

- 출석 기록 조회 가능
- 출석 상태 표시
- 이의신청 생성 가능
- 이의신청 상태 조회 가능
- Frontend 화면 연동
- 실패/빈 상태 처리

### 7.5 알림

PASS 조건:

- 알림 목록 조회 가능
- 읽음 처리 가능
- 삭제 또는 숨김 처리 가능
- 수신자 기준 조회 가능
- RabbitMQ 기반 비동기 처리 또는 대체 가능한 동기 처리 명시
- Frontend 알림 UI 연동

### 7.6 커리큘럼/강의 다시보기/학습자료

PASS 조건:

- 커리큘럼 일정 조회 가능
- 강의 다시보기 조회 가능
- 학습자료 목록/상세 조회 가능
- 리소스 또는 첨부파일 연결 가능
- 좋아요/즐겨찾기 등 반응 처리 가능
- Frontend 화면 연동

### 7.7 퀘스트/평가

PASS 조건:

- 평가 목록 조회 가능
- 제출 상태 조회 가능
- 점수/결과 상태 표시 가능
- 사용자별 제출 상태 구분 가능
- Frontend 화면 연동

### 7.8 설문

PASS 조건:

- 설문 목록 조회 가능
- 문항 조회 가능
- 선택지 조회 가능
- 응답 저장 가능
- 주관식/객관식 대응
- 중복 응답 정책 존재
- Frontend 설문 제출 연동

### 7.9 게시판

PASS 조건:

- 게시판 목록 또는 코드 기반 접근 가능
- 카테고리 조회 가능
- 게시글 목록 조회 가능
- 게시글 상세 조회 가능
- 게시글 작성/수정/삭제 중 필요한 범위 구현
- 댓글/대댓글 가능
- 반응 가능
- 첨부파일 연결 가능
- Frontend 화면 연동

### 7.10 1:1 문의

PASS 조건:

- 문의 생성 가능
- 문의 목록 조회 가능
- 문의 상세 조회 가능
- 답변 조회 가능
- 첨부파일 연결 가능
- 상태값 관리 가능
- Frontend 화면 연동

### 7.11 첨부파일

PASS 조건:

- 공통 attachment 구조 존재
- 업로드 또는 연결 흐름 존재
- 게시글/문의/학습자료/프로필 중 필요한 도메인과 연결 가능
- 파일 메타데이터 저장 가능
- 실패 처리 존재

### 7.12 실행환경/테스트/문서

PASS 조건:

- 로컬 실행 방법 존재
- Docker Compose 검증 가능
- 환경변수 문서화
- Backend 검증 명령 존재
- Frontend 검증 명령 존재
- build/lint/typecheck 중 가능한 명령 실행
- smoke test 존재
- README 최신화
- docs 문서 최신화

---

## 8. 역할별 실행 책임

### 8.1 product-manager

반드시 수행:

- 전체 기능 체크리스트 작성
- PASS/PARTIAL/FAIL/UNKNOWN 표 유지
- 남은 작업 확인
- 후속 task 생성
- 완료 착각 방지
- docs/remaining-work.md 관리
- docs/final-verification.md 관리

product-manager는 다음 조건에서 완료 선언을 금지한다.

- PARTIAL 존재
- FAIL 존재
- UNKNOWN 존재
- 테스트 실패
- 빌드 실패
- mock-only 구현 존재
- 문서와 코드 불일치
- 커밋되지 않은 변경 존재
- worktree에 병합되지 않은 변경 존재

### 8.2 architect

반드시 수행:

- Backend/Frontend/API 계약 정의
- DB/API 구조 점검
- Docker 서비스명과 애플리케이션 설정 매핑
- 화면/API 매핑 점검
- 파일 업로드 구조 점검
- 인증/인가 구조 점검
- 도메인 간 의존성 조율

### 8.3 executor-1

Backend 중심으로 수행:

- Spring Boot scaffold 또는 기존 backend 정리
- MySQL 연결
- Redis 연결
- RabbitMQ 연결
- 인증/사용자/프로필
- 출석/알림
- 커리큘럼/학습자료
- 퀘스트/설문
- 게시판/문의
- 첨부파일
- API 문서 갱신

### 8.4 executor-2

Frontend 중심으로 수행:

- React/Tailwind scaffold 또는 기존 frontend 정리
- 라우팅/레이아웃
- 로그인/마이페이지
- 출석 화면
- 학습자료 화면
- 설문 화면
- 게시판 화면
- 문의 화면
- 알림 화면
- API client
- 로딩/빈 화면/에러/권한 없음 상태

### 8.5 test-engineer

DevOps/QA 중심으로 수행:

- 기존 Docker Compose 검증
- dependency 확인
- Docker/env 확인
- Nginx reverse proxy 확인
- ELK 로그 수집 확인
- 실행 스크립트 확인
- lint/build/typecheck/test 실행
- smoke test 작성
- test-report 작성
- README 검증
- docs 검증

---

## 9. 실행환경 bootstrap 규칙

작업 전에 반드시 확인한다.

```bash
git status --short
git worktree prune
find . -name ".DS_Store" -delete
docker compose config
```

Docker 설정 확인 대상:

- `docker-compose.yml`
- `docker-compose.override.yml`
- `.env`
- `.env.example`
- `infra/`
- `nginx/`
- `elk/`
- `mysql/`
- `redis/`
- `rabbitmq/`

Frontend가 존재하면 package manager를 확인한다.

- `package-lock.json` 있으면 npm 기준
- `pnpm-lock.yaml` 있으면 pnpm 기준
- `yarn.lock` 있으면 yarn 기준

`frontend/`가 없으면 React + Tailwind 프로젝트 생성 task를 수행한다.

Backend가 존재하면 다음을 확인한다.

- `build.gradle`
- `pom.xml`
- Spring Boot version
- MySQL dependency
- Redis dependency
- RabbitMQ dependency
- Security/JWT dependency

`backend/`가 없으면 Spring Boot 프로젝트 생성 task를 수행한다.

---

## 10. 커밋 규칙

Conventional Commits 형식을 사용한다.

예:

```text
chore(env): validate docker compose services
chore(backend): scaffold spring boot application
chore(frontend): scaffold react tailwind application
feat(auth): implement jwt login and profile APIs
feat(attendance): add attendance appeal flow
feat(board): implement post and comment APIs
feat(survey): implement survey response save
feat(ui): add learning material pages
test(smoke): add full clone smoke checks
docs(progress): update implementation summary
fix(build): resolve spring boot dependency conflict
```

서로 다른 도메인을 한 커밋에 섞지 않는다.

---

## 11. 필수 문서

아래 문서를 작성하거나 갱신한다.

- docs/progress.md
- docs/architecture.md
- docs/api-summary.md
- docs/test-report.md
- docs/remaining-work.md
- docs/final-verification.md
- README.md

---

## 12. docs/progress.md 작성 기준

포함:

- 현재 실행 목표
- 생성된 task 목록
- worker별 담당 작업
- 완료된 작업
- 진행 중 작업
- 남은 작업
- 커밋 목록
- 변경 파일 요약

---

## 13. docs/architecture.md 작성 기준

포함:

- 전체 구조
- Docker Compose 서비스 구조
- Backend Spring Boot 구조
- Frontend React 구조
- DB/API 구조
- Redis/RabbitMQ 사용 지점
- Nginx reverse proxy 흐름
- ELK 로그 흐름
- 파일 업로드 구조
- 인증/인가 구조
- 주요 데이터 흐름

---

## 14. docs/api-summary.md 작성 기준

포함:

- API 엔드포인트
- HTTP 메서드
- 요청 파라미터
- 응답 요약
- 인증 필요 여부
- Frontend 연결 화면

---

## 15. docs/test-report.md 작성 기준

포함:

- 실행한 검증 명령
- 성공/실패 결과
- 실패 로그 요약
- Docker compose 검증 결과
- Nginx 검증 결과
- ELK 로그 확인 결과
- 수정 내용
- 재검증 결과
- 검증하지 못한 항목과 이유

---

## 16. docs/remaining-work.md 작성 기준

포함:

- 아직 PASS가 아닌 항목
- PARTIAL 항목
- FAIL 항목
- UNKNOWN 항목
- 다음에 생성해야 할 task
- 위험 요소
- known issue
- 완료 판단

`docs/remaining-work.md`에 필수 작업이 남아 있으면 완료가 아니다.

---

## 17. docs/final-verification.md 작성 기준

최종 완료 직전에 작성한다.

포함:

```text
기능명
상태: PASS / PARTIAL / FAIL / UNKNOWN
근거 파일
검증 명령
검증 결과
남은 작업
```

모든 핵심 기능이 PASS일 때만 최종 완료를 선언한다.

---

## 18. 실패 처리

실패하면 중단하지 말고 다음 순서로 처리한다.

1. 실패 로그를 읽는다.
2. 원인을 분류한다.
   - 코드 오류
   - 타입 오류
   - 빌드 설정 오류
   - 환경변수 누락
   - 테스트 하네스 부족
   - 의존성 문제
   - 문서 불일치
   - Docker 설정 불일치
   - worker/runtime 문제
3. 수정한다.
4. 다시 검증한다.
5. docs/test-report.md에 기록한다.
6. 수정 커밋을 만든다.
7. 남은 작업이 있으면 다음 task를 생성한다.

---

## 19. worker dead 또는 zombie team 방지

worker가 죽으면 상태를 기록하고 복구 task를 만든다.

다음 상황은 정상 완료가 아니다.

- worker dead
- pending task 존재
- in_progress task가 있는데 worker 없음
- shutdown blocked
- leader loop 반복
- task가 원칙 문장 단위로 생성됨
- 실제 파일 변경 없는 task만 반복됨

worker는 직접 cleanup을 실행하지 않는다.

대신 다음 내용을 docs/test-report.md 또는 docs/remaining-work.md에 기록한다.

- team name
- dead worker 목록
- pending task 수
- in_progress task 수
- 마지막 로그
- 권장 복구 명령

권장 복구 명령은 다음과 같다.

```bash
omx team status <team-name>
omx team api cleanup --input '{"team_name":"<team-name>","force":true,"confirm_issues":true}' --json
git worktree prune
find . -name ".DS_Store" -delete
git status --short
```

실제 cleanup은 사용자가 직접 실행한다.

---

## 20. 남은 작업이 없다고 판단될 때

남은 작업이 없다고 판단하기 전에 product-manager는 다음을 확인한다.

1. 모든 핵심 기능이 PASS인가
2. PARTIAL이 0개인가
3. FAIL이 0개인가
4. UNKNOWN이 0개인가
5. 테스트 결과가 문서화되었는가
6. smoke test가 존재하는가
7. README가 최신인가
8. API 문서가 최신인가
9. 실행환경 문서가 최신인가
10. Docker 설정과 Backend/Frontend 설정이 연결되어 있는가
11. 커밋되지 않은 변경이 없는가
12. worker worktree에 병합되지 않은 변경이 없는가
13. `docs/remaining-work.md`에 필수 작업이 없는가
14. `docs/final-verification.md`가 작성되었는가

하나라도 아니면 완료가 아니다. 즉시 추가 task를 생성하고 계속 실행한다.

---

## 21. 최종 지시

지금 즉시 연속 실행을 시작하라.

1. 현재 저장소를 분석하라.
2. Docker 설정만 있는지 확인하라.
3. Backend가 없으면 Spring Boot 프로젝트를 생성하라.
4. Frontend가 없으면 React + Tailwind 프로젝트를 생성하라.
5. 최소 18개 이상의 초기 task를 생성하라.
6. 5명의 역할에게 task를 분배하라.
7. 각 worker는 실제 파일을 수정하라.
8. 검증을 수행하라.
9. 커밋하라.
10. 문서를 갱신하라.
11. 완료 조건을 다시 점검하라.
12. 미완성 기능이 있으면 즉시 추가 task를 생성하라.
13. 추가 task를 다시 worker에게 배정하라.
14. 이 과정을 풀 클론 완료 조건이 모두 PASS가 될 때까지 반복하라.

중간에 “라운드 종료”라는 이유로 멈추지 마라.

작업이 끝났다고 판단되면 다음을 수행하라.

1. docs/final-verification.md 작성
2. 전체 기능 PASS/PARTIAL/FAIL/UNKNOWN 표 작성
3. 가능한 테스트 재실행
4. 실패 항목이 있으면 다시 task 생성
5. 모든 항목이 PASS일 때만 최종 완료 선언
