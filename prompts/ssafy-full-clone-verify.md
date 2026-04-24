
# SSAFY Full Clone - OMX Final Verification Prompt

너는 SSAFY 교육 플랫폼 풀스택 클론 프로젝트의 최종 검증 책임자다.

이 프롬프트의 목적은 단순히 파일 존재 여부를 확인하는 것이 아니다.
실제 기능 완성도, 실행 가능성, API/Frontend 연동, 테스트 결과, 문서 최신성을 기준으로 프로젝트가 완료되었는지 엄격하게 판단한다.

---

## 1. 목표

SSAFY 풀 클론 프로젝트가 실제로 완료되었는지 검증한다.

완료 판단은 다음 기준을 모두 고려한다.

- 코드가 실제로 존재하는가
- Backend가 실행 가능한가
- Frontend가 실행 또는 빌드 가능한가
- DB schema가 코드와 일치하는가
- API가 실제로 동작하는가
- Frontend 화면에서 API가 연동되는가
- 테스트 또는 smoke test가 존재하는가
- Docker/env 설정이 실제 실행 가능한가
- README와 docs 문서가 최신 상태인가
- Swagger/OpenAPI 문서가 실제 API와 일치하는가

완료 기준을 만족하지 못하면 완료라고 판단하지 않는다.

---

## 2. 검증 대상

다음 항목을 반드시 확인한다.

- 코드
- DB schema
- API
- Frontend 화면
- API 연동
- 테스트
- Docker/env
- README
- docs/progress.md
- docs/architecture.md
- docs/api-summary.md
- docs/test-report.md
- docs/remaining-work.md
- docs/final-verification.md
- docs/openapi.yaml
- docs/openapi.json
- Swagger UI
- /v3/api-docs endpoint

---

## 3. 검증 절차

반드시 아래 순서로 검증한다.

1. 저장소 구조를 확인한다.
2. 최근 커밋 목록을 확인한다.
3. 문서와 실제 코드가 일치하는지 확인한다.
4. Docker/env 설정을 확인한다.
5. Backend 실행 가능 여부를 확인한다.
6. Frontend 실행 또는 빌드 가능 여부를 확인한다.
7. 테스트 또는 smoke test를 실행한다.
8. Swagger/OpenAPI 문서 생성 여부를 확인한다.
9. OpenAPI 문서와 실제 Backend API가 일치하는지 확인한다.
10. 핵심 기능 체크리스트를 하나씩 검증한다.
11. 실패한 항목은 원인과 수정 방향을 기록한다.
12. 테스트 하네스가 부족하면 최소 smoke test를 추가한다.
13. 수정 가능한 문제는 직접 수정한다.
14. 수정 후 다시 검증한다.
15. 최종 결과를 docs/final-verification.md에 기록한다.

---

## 4. 핵심 기능 체크리스트

각 항목을 반드시 검증하고 PASS / PARTIAL / FAIL / UNKNOWN 중 하나로 분류한다.

- 인증/인가
- 사용자 프로필
- 캠퍼스/기수/반/트랙
- 출석 조회
- 출석 이의신청
- 알림 발송/수신/읽음
- 커리큘럼 일정
- 강의 다시보기
- 학습자료
- 학습자료 리소스
- 첨부파일
- 학습자료 반응
- 퀘스트/평가
- 퀘스트 제출 상태
- 설문 생성/조회
- 설문 문항/선택지
- 설문 응답 저장
- 게시판
- 게시글
- 댓글/대댓글
- 게시글 첨부파일
- 게시글 반응
- 1:1 문의
- 문의 답변
- 문의 첨부파일
- 권한별 접근 제어
- 에러 처리
- 로컬 실행
- 테스트
- 문서 최신화
- Swagger/OpenAPI 문서 생성 및 실제 API와의 일치 여부

---

## 5. 판정 기준

각 항목은 다음 기준으로 판단한다.

### PASS

다음 조건을 만족하면 PASS로 판단한다.

- 실제 구현이 존재한다.
- 실행 또는 빌드 검증 근거가 있다.
- Backend와 Frontend가 필요한 범위에서 연결되어 있다.
- 정상 케이스와 실패 케이스가 처리된다.
- 인증/인가가 필요한 기능은 권한 검사가 있다.
- 테스트 또는 smoke test 근거가 있다.
- 문서가 실제 코드와 일치한다.
- mock-only 구현이 아니다.
- TODO만 남긴 상태가 아니다.

### PARTIAL

다음 중 하나라도 해당하면 PARTIAL로 판단한다.

- 일부 구현은 있으나 Backend와 Frontend 연동이 부족하다.
- Backend만 있고 화면 연동이 없다.
- Frontend 화면만 있고 실제 API 연동이 없다.
- 정상 케이스만 있고 에러 처리가 부족하다.
- 권한 검사가 부족하다.
- 테스트가 부족하다.
- 문서와 실제 코드가 일부 불일치한다.
- mock 데이터에 의존한다.
- 기능은 있으나 UX 상태 처리가 부족하다.

### FAIL

다음 중 하나라도 해당하면 FAIL로 판단한다.

- 구현이 없다.
- 실행 시 크래시가 난다.
- 빌드가 실패한다.
- API 호출이 불가능하다.
- DB schema와 코드가 맞지 않는다.
- 화면에서 해당 기능에 접근할 수 없다.
- 테스트가 계속 실패한다.
- 인증이 필요한 기능이 공개되어 있다.
- 데이터 저장이 되지 않는다.
- Docker compose 설정이 깨져 있다.

### UNKNOWN

다음 중 하나라도 해당하면 UNKNOWN으로 판단한다.

- 파일을 확인하지 않았다.
- 실행 검증을 하지 않았다.
- 구현 여부를 문서만 보고 판단했다.
- API 응답 형태를 확인하지 않았다.
- 실제 화면 접근 경로를 확인하지 않았다.
- 테스트 방법이 불명확하다.
- Docker 설정이 실제 실행되는지 확인하지 않았다.
- OpenAPI 문서와 실제 Controller 일치 여부를 확인하지 않았다.

UNKNOWN은 완료가 아니다.
UNKNOWN이 있으면 반드시 조사 또는 검증 task를 남긴다.

---

## 6. Swagger / OpenAPI 검증 및 생성

최종 검증 시 API 문서화 상태도 반드시 확인한다.

### 6.1 목표

- 실제 Backend API와 일치하는 Swagger/OpenAPI 문서를 생성하거나 갱신한다.
- 단순 수동 요약이 아니라 실제 Controller, Route, DTO 기준으로 검증 가능한 API 명세를 남긴다.
- 문서만 보고 완료 처리하지 말고, 실제 API 구현과 OpenAPI 명세가 일치하는지 확인한다.

### 6.2 검증 대상

- docs/openapi.yaml
- docs/openapi.json
- docs/api-summary.md
- Spring Boot Swagger UI
- /v3/api-docs endpoint

### 6.3 우선순위

1. Spring Boot 프로젝트에 OpenAPI 자동 생성 구성이 있으면 이를 우선 사용한다.
2. /v3/api-docs가 동작하면 그 결과를 docs/openapi.json 또는 docs/openapi.yaml로 저장한다.
3. 자동 생성 구성이 없으면 실제 Controller, API, DTO 코드를 분석해서 docs/openapi.yaml을 작성한다.
4. API가 아직 미완성이라면 미완성 항목을 docs/remaining-work.md에 기록한다.
5. OpenAPI 문서와 docs/api-summary.md의 내용이 일치하는지 확인한다.

### 6.4 금지

- 실제 코드에 없는 API를 문서에 넣지 않는다.
- 구현되지 않은 API를 PASS로 처리하지 않는다.
- 문서만 존재한다고 API 구현 완료로 판단하지 않는다.
- Swagger 파일과 실제 Controller가 불일치하면 PASS가 아니다.
- API 명세만 만들고 Controller 구현 없이 완료 처리하지 않는다.

### 6.5 OpenAPI 문서 필수 정보

OpenAPI 문서에는 최소한 다음 정보를 포함한다.

- endpoint path
- HTTP method
- request parameters
- request body
- response body
- status code
- authentication required 여부
- error response
- 관련 domain tag

### 6.6 필수 API 그룹

OpenAPI 문서에는 최소한 다음 API 그룹을 검토한다.

- Auth API
- User/Profile API
- Campus/Cohort/Class/Track API
- Attendance API
- Attendance Appeal API
- Notification API
- Curriculum API
- Lecture Replay API
- Learning Material API
- Quest API
- Survey API
- Board API
- Comment API
- Support Ticket API
- Attachment API

### 6.7 검증 명령 예시

Spring Boot 서버가 실행 중이면 다음을 확인한다.

```bash
curl http://localhost:8080/v3/api-docs
```

정적 파일 존재 여부는 다음으로 확인한다.

```bash
test -f docs/openapi.yaml || test -f docs/openapi.json
```

OpenAPI JSON 유효성은 다음으로 확인한다.

```bash
python3 - <<'PY'
import json
import pathlib

json_path = pathlib.Path("docs/openapi.json")
yaml_path = pathlib.Path("docs/openapi.yaml")

if json_path.exists():
    json.loads(json_path.read_text())
    print("openapi.json is valid JSON")
elif yaml_path.exists():
    print("openapi.yaml exists")
else:
    raise SystemExit("OpenAPI file not found")
PY
```

### 6.8 OpenAPI 판정 기준

- PASS: 실제 API 기준 OpenAPI 문서가 존재하고, 주요 도메인 API가 반영되어 있으며, 실행 또는 정적 검증 근거가 있다.
- PARTIAL: OpenAPI 문서는 있지만 일부 도메인 API가 누락되었거나 실제 구현과 일부 불일치한다.
- FAIL: OpenAPI 문서가 없고 API 문서도 최신이 아니다.
- UNKNOWN: API 구현 여부 또는 문서 일치 여부를 확인하지 않았다.

### 6.9 최종 산출물 반영

최종 산출물에 반드시 포함한다.

1. docs/openapi.yaml 또는 docs/openapi.json
2. docs/api-summary.md
3. docs/final-verification.md 내 Swagger/OpenAPI 검증 결과
4. API 명세와 실제 구현의 일치 여부
5. 누락된 API 목록
6. OpenAPI 기준 PASS / PARTIAL / FAIL / UNKNOWN 판정

---

## 7. Docker / 실행환경 검증

Docker 설정은 단순 존재 여부가 아니라 실제 실행 가능성을 기준으로 검증한다.

확인 대상:

- docker-compose.yml
- docker-compose.override.yml
- .env
- .env.example
- infra/
- nginx/
- mysql/
- redis/
- rabbitmq/
- elk/

검증 명령 예시:

```bash
docker compose config
```

가능한 경우 다음도 확인한다.

```bash
docker compose ps
docker compose logs --tail=100
```

Docker 실행이 불가능하면 원인을 docs/test-report.md에 기록하고, docs/remaining-work.md에 수정 task를 남긴다.

---

## 8. Backend 검증

Spring Boot Backend가 있다면 다음을 확인한다.

- build.gradle 또는 pom.xml 존재
- application.yml 또는 application.properties 존재
- MySQL 연결 설정
- Redis 연결 설정
- RabbitMQ 연결 설정
- Controller 존재
- Service/Repository 구조 존재
- 인증/인가 설정 존재
- 테스트 또는 smoke test 존재

검증 명령은 저장소 구조에 맞게 선택한다.

예:

```bash
./gradlew test
```

```bash
./gradlew build
```

```bash
mvn test
```

```bash
mvn package
```

---

## 9. Frontend 검증

React Frontend가 있다면 다음을 확인한다.

- package.json 존재
- React routing 존재
- Tailwind CSS 설정 존재
- API client 존재
- 인증 상태 처리 존재
- 로딩/빈 화면/에러 상태 처리 존재
- 주요 화면 접근 가능
- build 가능

검증 명령은 package manager에 맞게 선택한다.

예:

```bash
npm install
npm run build
```

```bash
pnpm install
pnpm build
```

```bash
yarn install
yarn build
```

---

## 10. 문서 검증

다음 문서가 실제 코드와 일치하는지 확인한다.

- README.md
- docs/progress.md
- docs/architecture.md
- docs/api-summary.md
- docs/test-report.md
- docs/remaining-work.md
- docs/final-verification.md
- docs/openapi.yaml
- docs/openapi.json

문서만 있고 실제 구현이 없으면 PASS가 아니다.
구현만 있고 문서가 갱신되지 않았으면 PARTIAL 또는 FAIL로 판단한다.

---

## 11. 최종 산출물

최종 검증 결과는 docs/final-verification.md 파일을 작성하거나 갱신한다.

반드시 다음 내용을 포함한다.

1. 최종 검증 요약
2. 실행한 명령어
3. 테스트 결과
4. 기능별 PASS / PARTIAL / FAIL / UNKNOWN 표
5. Swagger/OpenAPI 문서 생성 결과
6. API 명세와 실제 구현의 일치 여부
7. 발견한 문제
8. 즉시 수정한 내용
9. 남은 작업
10. 최종 판단

---

## 12. 실패 처리

실패한 테스트를 무시하지 않는다.

실패 시 다음을 수행한다.

1. 실패 로그를 읽는다.
2. 원인을 분류한다.
3. 수정 가능한 문제는 직접 수정한다.
4. 수정 후 다시 검증한다.
5. 수정 내용을 커밋한다.
6. 수정 불가능하거나 범위가 큰 항목은 docs/remaining-work.md에 구체적 task로 남긴다.

원인 분류 예:

- 코드 오류
- 타입 오류
- 빌드 설정 오류
- 환경변수 누락
- Docker 설정 문제
- 테스트 하네스 부족
- 의존성 문제
- API 문서 불일치
- Frontend 연동 누락
- 권한 검증 누락

---

## 13. 완료 선언 금지 조건

다음 중 하나라도 해당하면 완료라고 하지 않는다.

- FAIL 항목이 있다.
- UNKNOWN 항목이 있다.
- 핵심 기능에 PARTIAL 항목이 남아 있다.
- Backend가 실행되지 않는다.
- Frontend가 빌드되지 않는다.
- Docker compose 설정이 깨져 있다.
- 테스트 또는 smoke test가 없다.
- Swagger/OpenAPI 문서가 없다.
- OpenAPI 문서와 실제 API가 불일치한다.
- README가 실제 실행 방법과 다르다.
- docs/remaining-work.md에 필수 작업이 남아 있다.
- 커밋되지 않은 변경사항이 있다.

---

## 14. 최종 지시

지금 즉시 최종 검증을 수행한다.

1. 저장소 상태를 확인한다.
2. 코드, Docker, Backend, Frontend, 문서를 검증한다.
3. 가능한 테스트와 빌드를 실행한다.
4. Swagger/OpenAPI 문서를 생성하거나 갱신한다.
5. OpenAPI 문서와 실제 API 구현을 비교한다.
6. 기능별 PASS / PARTIAL / FAIL / UNKNOWN 표를 작성한다.
7. docs/final-verification.md를 작성하거나 갱신한다.
8. 수정 가능한 문제는 직접 수정하고 커밋한다.
9. 남은 작업은 docs/remaining-work.md에 구체적인 task로 남긴다.
10. 완료 기준을 만족하지 못하면 완료라고 선언하지 않는다.
