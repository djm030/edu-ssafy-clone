# Test Spec

## 목적
PM + 3-agent 협업에서 구현 완료를 판단할 검증 기준을 정의한다.

## 현재 전제
- 저장소는 아직 앱 프레임워크가 확정되지 않았다.
- 따라서 일부 명령은 future harness placeholder다.
- 실제 명령은 Backend/Frontend/DevOps-QA가 스택 확정 후 이 문서에 반영한다.

## 검증 레벨
### Level 1: 문서 계약 검증
담당: PM

체크:
- `DESIGN_NOTES.md`에 대상 FR/FS가 있다.
- `IMPLEMENTATION_PLAN.md`에 담당 레인과 완료 조건이 있다.
- `API_SPEC_DRAFT.md`에 response shape가 있다.
- `SCREEN_LIST.md`에 화면 상태가 있다.
- `TEST_SPEC.md`에 검증 방법이 있다.

### Level 2: DB 검증
담당: DevOps-QA, Backend 지원

목표:
- `docs/revised_schema_mysql8.sql`이 MySQL 8에서 실행되는지 확인한다.

명령 후보:
```bash
mysql --version
mysql -h 127.0.0.1 -P 3306 -u root -p ssafy_clone < docs/revised_schema_mysql8.sql
```

Docker 후보:
```bash
docker compose up -d mysql
docker compose exec -T mysql mysql -u root -p ssafy_clone < docs/revised_schema_mysql8.sql
```

완료 조건:
- schema 실행 성공
- 또는 실패 SQL line, error code, 원인 후보가 기록됨

### Level 3: API 검증
담당: Backend, DevOps-QA

대상:
```text
GET /api/boards/notice/categories
GET /api/boards/notice/posts
GET /api/boards/free/categories
GET /api/boards/free/posts
```

검증:
- 200 success shape
- empty response shape
- invalid boardCode 404
- invalid page/size 400
- unauthenticated 401 또는 프로젝트 인증 정책에 맞는 응답

명령 후보:
```bash
curl -i "http://localhost:8080/api/boards/notice/posts?page=1&size=20"
curl -i "http://localhost:8080/api/boards/free/posts?keyword=test"
```

완료 조건:
- `API_SPEC_DRAFT.md`의 JSON shape와 일치
- notice/free가 같은 contract를 사용

### Level 4: Frontend Smoke
담당: Frontend, DevOps-QA

대상 route 후보:
```text
/help/notice
/community/free
```

검증:
- 목록 렌더링
- 로딩 상태
- 빈 상태
- API 오류 상태
- 카테고리 선택
- 검색어 입력
- 페이지 변경

명령 후보:
```bash
npm test
npm run test:e2e
npm run lint
```

완료 조건:
- 화면이 API contract와 연결됨
- notice/free route가 독립 진입 가능
- 빈 상태와 오류 상태가 깨지지 않음

### Level 5: 사람 리뷰
담당: Human Reviewer, PM 지원

리뷰 자료:
- 변경 파일 목록
- 실행한 테스트 명령
- 테스트 결과
- 스크린샷 또는 smoke evidence
- 남은 리스크

리뷰 기준:
- SSAFY 화면 흐름과 우선순위에 맞는가
- 과도한 추론이 들어가지 않았는가
- 문서-API-UI-DB가 서로 모순되지 않는가

## 실패 처리
- DB 실패: DevOps-QA가 error evidence를 기록하고 Backend가 SQL 수정 후보를 낸다.
- API 실패: Backend가 수정하고 DevOps-QA가 재검증한다.
- UI 실패: Frontend가 수정하고 DevOps-QA가 smoke를 재검증한다.
- 계약 충돌: PM이 API/SCREEN/TEST 문서를 먼저 수정한 뒤 레인 작업을 재개한다.

## Evidence Template
```text
Date:
Commit/Worktree:
Command:
Result:
Evidence file:
Failure owner:
Next action:
```

## Known Risks
- MySQL 8 실행 검증이 아직 완료되지 않았다.
- 앱 프레임워크가 정해지기 전까지 test command는 placeholder다.
- 긴 파일명 캡처 자료는 일부 Windows 도구에서 경로 길이 문제가 날 수 있다.
