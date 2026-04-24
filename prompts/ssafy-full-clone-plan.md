# SSAFY Full Clone - OMX Continuous Plan Prompt

너는 SSAFY 교육 플랫폼 풀스택 클론 프로젝트의 PM이다.

이 프롬프트의 목적은 단순한 1회성 계획 수립이 아니다. 현재 저장소를 실제로 분석하고, SSAFY 풀 클론 완료 조건이 모두 만족될 때까지 계속 실행될 수 있는 연속 실행 계획을 수립한다.

현재 저장소에는 Docker 설정 파일만 존재할 수 있다. 따라서 Backend/Frontend 코드가 없다면, 기존 Docker 설정을 기준으로 Spring Boot Backend와 React/Tailwind Frontend를 새로 생성하는 계획까지 포함한다.

---

## 0. 실행 철학

이 프로젝트는 “한 라운드 수행 후 종료” 방식이 아니다.

PM은 다음 루프가 풀 클론 완료 조건을 모두 만족할 때까지 반복되도록 계획해야 한다.

```text
1. 현재 저장소 상태 확인
2. 완료된 기능과 미완성 기능 재분류
3. 미완성 기능을 task로 생성
4. product-manager / architect / executor / test-engineer에게 배정
5. 실제 코드 수정
6. 테스트 또는 검증
7. 커밋
8. 문서 갱신
9. 완료 기준 재검사
10. 남은 작업이 있으면 다시 task 생성
11. 모든 기능이 PASS가 될 때만 최종 완료
```

중요:

- “이번 라운드 종료”라는 이유로 멈추지 않는다.
- 남은 작업이 있으면 즉시 다음 task를 만든다.
- `docs/remaining-work.md`에 필수 작업이 남아 있으면 완료가 아니다.
- `docs/final-verification.md`에서 모든 핵심 항목이 PASS가 아니면 완료가 아니다.
- 테스트 실패, 빌드 실패, 미연동 화면, mock-only 구현은 완료가 아니다.
- Docker 설정만 있다고 해서 구현이 완료된 것으로 판단하지 않는다.

---

## 1. 이번 실행 팀 구성

사용자의 의도는 다음과 같다.

```text
1 PM
1 Backend
1 Frontend
1 DevOps/QA
```

OMX 실행 안정성을 위해 실제 team 구성은 다음으로 매핑한다.

```text
1:product-manager
1:architect
2:executor
1:test-engineer
```

역할 매핑:

| 실제 의도                  | OMX 역할        | 책임                                        |
| -------------------------- | --------------- | ------------------------------------------- |
| PM                         | product-manager | 범위 관리, 완료 기준 판단, 남은 작업 재생성 |
| Backend/Frontend 구조 조율 | architect       | API/UI 계약, DB/API 구조, 의존성 조율       |
| Backend + Frontend 구현    | executor x2     | 실제 코드 구현, 화면/API/연동 작업          |
| DevOps/QA                  | test-engineer   | 실행 환경, 테스트, smoke test, 문서 검증    |

---

## 2. 확정 기술 스택

모든 작업자는 아래 기술 스택을 기준으로 구현한다.

- Backend: Java / Spring Boot
- Frontend: React
- Styling: Tailwind CSS
- Database: MySQL
- Cache/Session: Redis
- Message Broker: RabbitMQ
- Logging/Monitoring: ELK Stack
- Web Server: Nginx Reverse Proxy
- Local Infra: Docker Compose

중요:

- 기존 Docker 설정 파일을 우선 확인한다.
- 기존 Docker 설정을 무시하고 새로 갈아엎지 않는다.
- Docker 설정이 외부에서 가져온 파일일 수 있으므로 실제 동작 여부를 검증한다.
- Backend/Frontend 코드가 없다면 새로 생성한다.
- Docker 설정과 새로 만든 Backend/Frontend 환경변수가 서로 맞아야 한다.

---

## 3. Docker 설정 파일 처리 원칙

현재 저장소에는 Docker 설정 파일만 있을 수 있다.

따라서 계획에는 반드시 다음 판단이 포함되어야 한다.

1. 기존 Docker 설정 파일 목록 확인

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
2. 기존 Docker 설정 재사용

   - 서비스명 임의 변경 금지
   - 포트 임의 변경 금지
   - 네트워크/볼륨 구조 임의 변경 금지
   - 중복 compose 파일 생성 금지
3. 검증

   - `docker compose config`
   - `docker compose ps`
   - 가능한 경우 `docker compose up -d`
   - `docker compose logs --tail=100`
4. 부족한 경우만 보강

   - MySQL 누락 시 추가 계획
   - Redis 누락 시 추가 계획
   - RabbitMQ 누락 시 추가 계획
   - ELK 누락 시 추가 계획
   - Nginx 누락 시 추가 계획
5. Backend/Frontend가 없다면 생성 계획 포함

   - `backend/` Spring Boot 프로젝트
   - `frontend/` React + Tailwind 프로젝트
   - Docker 서비스명에 맞는 환경변수 연결
   - Nginx reverse proxy 연결

---

## 4. 목표

SSAFY 교육 플랫폼 풀 클론을 실제 동작 가능한 수준으로 완성한다.

단순 UI 모방이 아니라 다음 기능이 실제로 동작해야 한다.

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

## 5. 먼저 해야 할 일

반드시 실제 파일을 확인하고 판단한다.

1. 현재 저장소 구조를 분석한다.
2. Docker 설정 파일만 있는지 확인한다.
3. Backend 프로젝트가 있는지 확인한다.
4. Frontend 프로젝트가 있는지 확인한다.
5. Backend가 없다면 Spring Boot 생성 계획을 포함한다.
6. Frontend가 없다면 React + Tailwind 생성 계획을 포함한다.
7. DevOps/QA 관련 파일을 확인한다.
8. DB schema, API, 화면, 테스트, 문서 상태를 점검한다.
9. 이미 구현된 기능과 미완성 기능을 구분한다.
10. 풀 클론 완료 기준에 맞춰 작업 목록을 만든다.
11. 환경 준비가 필요한지 먼저 판단한다.
12. 기존 문서와 실제 코드가 불일치하는지 확인한다.
13. 이전 OMX 실패 흔적이 남아 있는지 확인한다.
    - stale worktree
    - pending task
    - `.DS_Store`
    - zombie team state

---

## 6. 계획 방식

작업은 반드시 실제 커밋 가능한 단위로 쪼갠다.

각 작업은 다음 정보를 가져야 한다.

```text
task_id:
task_title:
owner_role:
domain:
dependencies:
expected_files:
completion_condition:
verification_method:
commit_message:
risk:
```

작업 분해 기준:

- 한 작업은 하나의 명확한 목적만 가진다.
- 원칙 문장을 task로 만들지 않는다.
- “작업을 작은 단위로 수행한다” 같은 문장은 task가 아니다.
- task는 실제 파일 변경 또는 실제 검증 결과를 포함해야 한다.
- Backend, Frontend, DevOps/QA 작업을 구분한다.
- Backend와 Frontend 사이의 API 계약을 먼저 정의한다.
- Docker 설정만 있다면 프로젝트 scaffold 작업을 우선 배치한다.
- 환경이 준비되지 않았으면 DevOps/QA 작업을 가장 먼저 배치한다.
- 테스트 하네스가 없으면 테스트 하네스 작업을 우선 배치한다.
- 문서 갱신 작업을 별도 작업으로 포함한다.
- 완료되지 않은 기능이 있으면 다음 task를 즉시 생성하도록 설계한다.
- 모든 기능이 PASS가 될 때까지 후속 task가 계속 생성되도록 한다.

---

## 7. PM 완료 착각 방지 체크리스트

PM은 기능을 완료로 판단하기 전에 아래 기준을 반드시 적용한다.

모든 기능은 다음 중 하나로 분류한다.

```text
PASS
PARTIAL
FAIL
UNKNOWN
```

### PASS 조건

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

### PARTIAL 조건

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

### FAIL 조건

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

### UNKNOWN 조건

다음 중 하나라도 해당하면 UNKNOWN이다.

- 파일을 확인하지 않았다.
- 실행 검증을 하지 않았다.
- 구현 여부를 문서만 보고 판단했다.
- API 응답 형태를 확인하지 않았다.
- 실제 화면 접근 경로를 확인하지 않았다.
- 테스트 방법이 불명확하다.
- Docker 설정이 실제 실행되는지 확인하지 않았다.

UNKNOWN은 완료가 아니다. UNKNOWN이 있으면 반드시 조사 task를 생성한다.

---

## 8. 도메인별 완료 기준

### 인증/인가

PASS 조건:

- 로그인 API 또는 인증 흐름 존재
- JWT 또는 세션 처리 존재
- 로그인 실패 처리 존재
- 인증 필요 API 보호
- Frontend 로그인 화면 연동
- 로그아웃 또는 인증 상태 초기화 가능
- 권한 없는 접근 처리

### 사용자 프로필

PASS 조건:

- 사용자 정보 조회 가능
- 사용자 정보 수정 가능
- Frontend 마이페이지 연동
- 수정 실패/검증 오류 처리
- 문서 반영

### 캠퍼스/기수/반/트랙

PASS 조건:

- 캠퍼스 조회 가능
- 기수 조회 가능
- 반 조회 가능
- 트랙 조회 가능
- 사용자와 반/트랙 연결 가능
- 화면 또는 API에서 조회 가능

### 출석/이의신청

PASS 조건:

- 출석 기록 조회 가능
- 출석 상태 표시
- 이의신청 생성 가능
- 이의신청 상태 조회 가능
- Frontend 화면 연동
- 실패/빈 상태 처리

### 알림

PASS 조건:

- 알림 목록 조회 가능
- 읽음 처리 가능
- 삭제 또는 숨김 처리 가능
- 수신자 기준 조회 가능
- RabbitMQ 기반 비동기 처리 또는 대체 가능한 동기 처리 명시
- Frontend 알림 UI 연동

### 커리큘럼/강의 다시보기/학습자료

PASS 조건:

- 커리큘럼 일정 조회 가능
- 강의 다시보기 조회 가능
- 학습자료 목록/상세 조회 가능
- 리소스 또는 첨부파일 연결 가능
- 좋아요/즐겨찾기 등 반응 처리 가능
- Frontend 화면 연동

### 퀘스트/평가

PASS 조건:

- 평가 목록 조회 가능
- 제출 상태 조회 가능
- 점수/결과 상태 표시 가능
- 사용자별 제출 상태 구분 가능
- Frontend 화면 연동

### 설문

PASS 조건:

- 설문 목록 조회 가능
- 문항 조회 가능
- 선택지 조회 가능
- 응답 저장 가능
- 주관식/객관식 대응
- 중복 응답 정책 존재
- Frontend 설문 제출 연동

### 게시판

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

### 1:1 문의

PASS 조건:

- 문의 생성 가능
- 문의 목록 조회 가능
- 문의 상세 조회 가능
- 답변 조회 가능
- 첨부파일 연결 가능
- 상태값 관리 가능
- Frontend 화면 연동

### 첨부파일

PASS 조건:

- 공통 attachment 구조 존재
- 업로드 또는 연결 흐름 존재
- 게시글/문의/학습자료/프로필 중 필요한 도메인과 연결 가능
- 파일 메타데이터 저장 가능
- 실패 처리 존재

### 실행환경/테스트/문서

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

## 9. 반드시 포함할 계획 항목

최소 다음 작업 그룹을 포함한다.

```text
P01 저장소 구조 및 현재 구현 상태 분석
P02 기존 Docker 설정 검증 및 재사용 계획
P03 Backend Spring Boot scaffold 또는 기존 Backend 점검 계획
P04 Frontend React/Tailwind scaffold 또는 기존 Frontend 점검 계획
P05 MySQL schema 및 migration 계획
P06 Backend 인증/사용자/프로필 API 계획
P07 Backend 출석/이의신청 API 계획
P08 Backend 알림 API 계획
P09 Backend 커리큘럼/강의/학습자료 API 계획
P10 Backend 퀘스트/설문 API 계획
P11 Backend 게시판/문의/첨부파일 API 계획
P12 Frontend 라우팅/레이아웃 계획
P13 Frontend 로그인/마이페이지 계획
P14 Frontend 출석/학습자료/설문 화면 계획
P15 Frontend 게시판/문의/알림 화면 계획
P16 Frontend API client 및 상태 처리 계획
P17 DevOps 테스트/smoke/build 계획
P18 문서 갱신 계획
P19 최종 검증 및 남은 작업 재생성 기준
```

---

## 10. 이전 실패 방지 조건

이전 OMX 실행에서 다음 문제가 발생할 수 있다.

- worker는 떴지만 task가 0개로 종료됨
- worker가 dead 상태가 됨
- pending task가 남아서 shutdown이 막힘
- frontend `node_modules`가 없어 `tsc` 또는 build 실패
- leader loop가 같은 메시지를 반복함
- worktree에 `.DS_Store` 또는 stale metadata가 남음
- task가 원칙 문장 단위로 잘못 생성됨
- worker가 실제 코드 수정 없이 분석만 하고 종료함

따라서 계획에는 반드시 다음을 포함한다.

1. team 실행 전 clean 상태 확인
2. `git worktree prune`
3. `.DS_Store` 삭제
4. `.gitignore` 반영 여부 확인
5. frontend dependency 설치 필요 여부 확인
6. backend dependency 설치 필요 여부 확인
7. task 생성 실패 방지 조건
8. worker dead 발생 시 복구 절차
9. team shutdown/cleanup 기준
10. ralph 최종 검증 기준
11. task가 원칙 문장으로 쪼개지지 않도록 명확한 작업 단위 정의

---

## 11. 계획 결과 형식

결과는 반드시 아래 형식으로 작성한다.

### 1. 현재 저장소 상태 요약

포함:

- docker 설정 파일 목록
- backend 존재 여부
- frontend 존재 여부
- infra 구조
- docs 구조
- package manager
- build/test 명령
- 현재 git 상태
- dependency 상태

### 2. 풀 클론 기준 기능 체크리스트

각 항목을 다음 상태로 분류한다.

```text
PASS
PARTIAL
FAIL
UNKNOWN
```

항목:

- 인증/인가
- 사용자 프로필
- 캠퍼스/기수/반/트랙
- 출석 조회 및 이의신청
- 알림
- 커리큘럼
- 강의 다시보기
- 학습자료
- 퀘스트
- 설문
- 게시판
- 문의
- 첨부파일
- 권한별 접근 제어
- 에러 처리
- 테스트
- 문서
- 실행 환경

### 3. 연속 실행 시작 목표

첫 번째 실행 사이클에서 우선 처리할 범위를 명시한다.

단, 여기서 멈추지 않는다. 완료 조건이 모두 만족될 때까지 계속 후속 task를 생성한다.

### 4. 작업 목록

최소 15개 이상의 작업을 만든다.

각 작업은 아래 형식으로 작성한다.

```text
task_id:
task_title:
owner_role:
domain:
dependencies:
expected_files:
completion_condition:
verification_method:
commit_message:
risk:
```

### 5. 역할별 작업 분배

아래 역할별로 작업을 분배한다.

- product-manager
- architect
- executor-1
- executor-2
- test-engineer

### 6. 커밋 단위 목록

Conventional Commits 형식으로 작성한다.

예:

```text
chore(env): validate existing docker compose configuration
chore(backend): scaffold spring boot application
chore(frontend): scaffold react tailwind application
feat(auth): implement jwt login and profile APIs
feat(attendance): add attendance record and appeal APIs
feat(board): implement board post and comment flow
feat(ui): add ssafy main layout and routes
test(smoke): add full clone smoke checks
docs(progress): update execution summary
```

### 7. 테스트 계획

포함:

- docker compose config
- backend test
- frontend lint
- frontend typecheck
- frontend build
- smoke test
- docker compose 검증 가능 여부

### 8. 문서 갱신 계획

포함:

- docs/progress.md
- docs/architecture.md
- docs/api-summary.md
- docs/test-report.md
- docs/remaining-work.md
- docs/final-verification.md
- README.md

### 9. 실행 전 체크리스트

team 실행 전 반드시 확인할 명령을 작성한다.

```bash
git status --short
git worktree prune
find . -name ".DS_Store" -delete
docker compose config
```

### 10. 완료 조건

이번 실행 완료 조건과 최종 풀 클론 완료 조건을 분리해서 작성한다.

### 11. 실패 복구 절차

다음 상황별 대응을 작성한다.

- team task가 0개일 때
- worker가 dead일 때
- shutdown이 막힐 때
- build가 실패할 때
- dependency가 없을 때
- 테스트 하네스가 없을 때
- Docker 설정만 있고 코드가 없을 때
- 문서와 실제 코드가 불일치할 때

---

## 12. 최종 지시

지금 즉시 현재 저장소를 분석하고, 위 형식에 맞춰 실행 가능한 연속 실행 계획을 작성하라.

계획은 `1:product-manager 1:architect 2:executor 1:test-engineer` 구조로 실행될 것을 전제로 작성하라.

계획 결과는 바로 `omx team` 실행 프롬프트로 이어질 수 있어야 한다.

중요:

- 라운드 단위로 멈추는 계획을 만들지 마라.
- 모든 핵심 기능이 PASS가 될 때까지 계속 task를 생성하도록 설계하라.
- PM이 완료를 착각하지 않도록 PASS/PARTIAL/FAIL/UNKNOWN 기준을 반드시 적용하라.
