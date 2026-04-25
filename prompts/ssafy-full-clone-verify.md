# SSAFY Full Clone - VERIFY Prompt

너는 SSAFY 교육 플랫폼 풀스택 클론 프로젝트의 **최종 검증 책임자**다.

이 프롬프트는 PLAN 또는 TEAM 실행용이 아니다. 구현이 어느 정도 진행된 뒤, Ralph 또는 별도 검증 단계에서 프로젝트가 실제로 완료되었는지 엄격하게 판단하기 위한 프롬프트다.

---

## 0. VERIFY 단계의 책임

VERIFY는 다음을 수행한다.

1. 저장소 구조와 최근 커밋을 확인한다.
2. Backend 실행/테스트 가능 여부를 확인한다.
3. Frontend typecheck/build 가능 여부를 확인한다.
4. Docker/env/Nginx 실행 가능성을 확인한다.
5. DB schema와 코드의 일치 여부를 확인한다.
6. 실제 API와 Frontend 연동 여부를 확인한다.
7. Spring REST Docs 문서 생성/서빙 여부를 확인한다.
8. 핵심 기능별 PASS/PARTIAL/FAIL/UNKNOWN을 판정한다.
9. 실패한 항목의 원인과 수정 방향을 남긴다.
10. 최종 결과를 `docs/final-verification.md`에 기록한다.

VERIFY는 문서 존재만 보고 완료 처리하지 않는다.

---

## 1. 완료 판단 기준

완료 판단은 다음 기준을 모두 고려한다.

- 코드가 실제로 존재하는가
- Backend가 실행 가능하고 테스트가 통과하는가
- Frontend가 typecheck/build 가능한가
- DB schema가 코드와 일치하는가
- API가 실제로 동작하는가
- Frontend 화면에서 API가 연동되는가
- 권한/에러/빈 데이터/로딩 상태가 처리되는가
- 테스트 또는 smoke test가 존재하는가
- Docker/env/Nginx 설정이 실제 실행 가능한가
- README와 docs 문서가 최신 상태인가
- Spring REST Docs가 실제 API와 일치하는가

완료 기준을 만족하지 못하면 완료라고 판단하지 않는다.

---

## 2. 검증 대상

반드시 확인한다.

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
- docs/spring-rest-docs.md
- Spring REST Docs generated HTML
- Backend docs route: `/docs/api/index.html`
- Nginx docs route: `/docs/api/index.html`

Swagger UI 또는 `/v3/api-docs`는 이 프로젝트의 완료 기준이 아니다. Springdoc을 사용하지 않는 상태를 실패로 보지 않는다.

---

## 3. 검증 절차

아래 순서로 검증한다.

1. 저장소 구조를 확인한다.
2. 최근 커밋 목록을 확인한다.
3. 문서와 실제 코드가 일치하는지 확인한다.
4. Docker/env 설정을 확인한다.
5. Backend 실행 가능 여부를 확인한다.
6. Frontend 실행 또는 빌드 가능 여부를 확인한다.
7. 테스트 또는 smoke test를 실행한다.
8. Spring REST Docs 생성 여부를 확인한다.
9. Spring REST Docs와 실제 Backend Controller/API가 일치하는지 확인한다.
10. 핵심 기능 체크리스트를 하나씩 검증한다.
11. 실패한 항목은 원인과 수정 방향을 기록한다.
12. 테스트 하네스가 부족하면 최소 smoke test를 추가한다.
13. 수정 가능한 문제는 직접 수정한다.
14. 수정 후 다시 검증한다.
15. 최종 결과를 `docs/final-verification.md`에 기록한다.

---

## 4. 핵심 기능 체크리스트

각 항목을 PASS / PARTIAL / FAIL / UNKNOWN 중 하나로 분류한다.

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
- Spring REST Docs 생성 및 실제 API와의 일치 여부

---

## 5. 판정 기준

### PASS

아래 조건을 만족해야 한다.

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

다음 중 하나라도 해당하면 PARTIAL이다.

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

다음 중 하나라도 해당하면 FAIL이다.

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

다음 중 하나라도 해당하면 UNKNOWN이다.

- 파일을 확인하지 않았다.
- 실행 검증을 하지 않았다.
- 구현 여부를 문서만 보고 판단했다.
- API 응답 형태를 확인하지 않았다.
- 실제 화면 접근 경로를 확인하지 않았다.
- 테스트 방법이 불명확하다.
- Docker 설정이 실제 실행되는지 확인하지 않았다.
- Spring REST Docs와 실제 Controller 일치 여부를 확인하지 않았다.

UNKNOWN은 완료가 아니다.

---

## 6. Spring REST Docs 검증

### 6.1 목표

- 실제 Backend API와 일치하는 Spring REST Docs 문서를 생성하거나 갱신한다.
- 문서만 보고 완료 처리하지 말고, 실제 Controller/DTO/테스트와 REST Docs가 일치하는지 확인한다.

### 6.2 검증 대상

- `backend/src/test/java/**/docs/**/*RestDocsTest.java`
- `backend/src/docs/asciidoc/index.adoc`
- `backend/target/classes/static/docs/api/index.html`
- `docs/spring-rest-docs.md`
- `docs/api-summary.md`
- Backend URL: `http://localhost:8080/docs/api/index.html`
- Nginx URL: `http://localhost/docs/api/index.html`

### 6.3 우선순위

1. Spring REST Docs 테스트가 실제 Controller를 호출하는지 확인한다.
2. `mvn -B prepare-package` 또는 동일한 빌드 명령으로 snippet/HTML이 생성되는지 확인한다.
3. 생성 HTML이 backend static resource로 포함되는지 확인한다.
4. Nginx `/docs/api/index.html` 경로가 backend 문서로 proxy되는지 확인한다.
5. API가 미완성이라면 미완성 항목을 `docs/remaining-work.md`에 기록한다.
6. `docs/api-summary.md`와 REST Docs 대상 API가 일치하는지 확인한다.

### 6.4 금지

- 실제 코드에 없는 API를 문서에 넣지 않는다.
- 구현되지 않은 API를 PASS로 처리하지 않는다.
- REST Docs 파일만 존재한다고 API 구현 완료로 판단하지 않는다.
- REST Docs와 실제 Controller가 불일치하면 PASS가 아니다.
- Swagger UI 또는 `/v3/api-docs` 부재를 실패로 처리하지 않는다.

### 6.5 검증 명령 예시

```bash
bash scripts/dev/backend-test.sh
```

```bash
cd backend && mvn -B prepare-package
```

```bash
test -f backend/target/classes/static/docs/api/index.html
```

```bash
python3 scripts/dev/smoke-lite.py
```

Docker/Nginx가 실행 중이면 다음도 확인한다.

```bash
docker compose -f compose.yml --profile app exec -T nginx nginx -t
```

```bash
docker compose -f compose.yml --profile app exec -T nginx wget -S -O - http://127.0.0.1/docs/api/index.html
```

### 6.6 REST Docs 판정 기준

- PASS: 실제 Controller 기반 REST Docs 테스트와 생성 HTML이 존재하고, backend/nginx 경로 제공 근거가 있다.
- PARTIAL: 일부 API만 문서화되었거나 live serving 근거가 부족하다.
- FAIL: REST Docs 생성이 실패하거나 문서 경로가 깨져 있다.
- UNKNOWN: REST Docs 생성/경로/Controller 일치 여부를 확인하지 않았다.

---

## 7. Docker / 실행환경 검증

확인 대상:

- compose.yml
- compose.mysql.yml
- compose.observability.yml
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
docker compose -f compose.yml config
```

가능한 경우:

```bash
docker compose -f compose.yml --profile app ps
```

```bash
docker compose -f compose.yml --profile app logs --tail=100
```

---

## 8. Backend 검증

확인 대상:

- pom.xml
- application.yml/properties
- MySQL 연결 설정
- Redis 연결 설정
- RabbitMQ 연결 설정
- Controller/Service/Repository 구조
- 인증/인가 설정
- Spring REST Docs 테스트
- smoke test

검증 명령은 저장소 구조에 맞게 선택한다.

---

## 9. Frontend 검증

확인 대상:

- package.json
- React routing
- Tailwind CSS 설정
- API client
- 인증 상태 처리
- 로딩/빈 화면/에러 상태 처리
- 주요 화면 접근 가능성
- build 가능성

검증 명령 예시:

```bash
cd frontend && npm exec -- tsc --noEmit -p tsconfig.app.json
```

```bash
cd frontend && npm run build
```

---

## 10. 최종 산출물

최종 검증 결과는 `docs/final-verification.md` 파일을 작성하거나 갱신한다.

반드시 포함한다.

1. 최종 검증 요약
2. 실행한 명령어
3. 테스트 결과
4. 기능별 PASS / PARTIAL / FAIL / UNKNOWN 표
5. Spring REST Docs 생성/서빙 결과
6. REST Docs와 실제 구현의 일치 여부
7. 발견한 문제
8. 즉시 수정한 내용
9. 남은 작업
10. 최종 판단

---

## 11. 완료 선언 금지 조건

다음 중 하나라도 해당하면 완료라고 하지 않는다.

- FAIL 항목이 있다.
- UNKNOWN 항목이 있다.
- 핵심 기능에 PARTIAL 항목이 남아 있다.
- Backend가 실행되지 않는다.
- Frontend가 빌드되지 않는다.
- Docker compose 설정이 깨져 있다.
- 테스트 또는 smoke test가 없다.
- Spring REST Docs가 없다.
- REST Docs와 실제 API가 불일치한다.
- README가 실제 실행 방법과 다르다.
- `docs/remaining-work.md`에 필수 작업이 남아 있다.
- 커밋되지 않은 변경사항이 있다.
