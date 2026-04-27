# Test Spec

## 목적
현재 저장소에서 구현 완료를 판단할 검증 기준을 정의한다.

## 현재 전제
- Runtime은 Docker Compose 기준이다.
- Backend는 Spring Boot, Frontend는 React/Vite 기준이다.
- MySQL 8, Redis, RabbitMQ, Nginx, ELK는 Compose 서비스로 실행한다.
- 직접 작성 코드 파일은 500줄 이하로 유지한다.

## 검증 레벨
### Level 1: 문서 계약 검증

체크:
- `docs/REQUIREMENTS.md`와 `docs/FUNCTIONAL_SPEC.md`에 대상 요구사항이 있다.
- `docs/api-summary.md` 또는 `docs/openapi.json`에 API surface가 반영되어 있다.
- `docs/next_plan.md`에 현재 UI/UX parity 작업이 있으면 해당 항목과 연결되어 있다.
- `TEST_SPEC.md`에 검증 방법이 있다.
- 새 코드 파일 중 직접 작성 파일은 500줄을 넘지 않는다.

명령 후보:
```powershell
$files = rg --files backend frontend infra scripts docs/collaboration compose.yml compose.mysql.yml compose.observability.yml .env.example -g '!frontend/node_modules/**' -g '!frontend/dist/**' -g '!backend/target/**' -g '!frontend/package-lock.json'
foreach ($f in $files) {
  $lines=(Get-Content -LiteralPath $f | Measure-Object -Line).Lines
  if ($lines -gt 500) { "$lines $f" }
}
```

### Level 2: DB 검증
목표:
- `docs/revised_schema_mysql8.sql`이 MySQL 8에서 실행되는지 확인한다.
- 게시판 목록 API에 필요한 seed가 적용되는지 확인한다.

명령 후보:
```powershell
powershell -ExecutionPolicy Bypass -File scripts\mysql\verify-schema.ps1
```

완료 조건:
- schema 실행 성공
- board group, access scope, board, category, post smoke query 성공
- 또는 실패 SQL line, error code, 원인 후보가 기록됨

### Level 3: API 검증
대상:
```text
POST /api/auth/login
GET /api/me
GET /api/dashboard/summary
GET /api/attendance/records
GET /api/notifications
GET /api/learning/materials
GET /api/quests
GET /api/surveys
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
- notice/free가 같은 contract 사용

명령 후보:
```bash
curl -i "http://localhost:8080/api/boards/notice/posts?page=1&size=20"
curl -i "http://localhost:8080/api/boards/free/posts?keyword=test"
```

완료 조건:
- `docs/api-summary.md`와 `docs/openapi.json`의 route/method shape와 일치
- 오류 응답이 `{ "error": { "code": "...", "message": "..." } }` 형태 유지

### Level 4: Frontend Smoke
대상 route:
```text
/login
/
/mycampus/attendance
/mycampus/level
/help/notice
/community/free
/learning/materials
/quest
/survey
```

검증:
- route 직접 진입
- 공통 navigation 표시
- 목록 렌더링
- 로딩 상태
- 빈 상태
- API 오류 상태
- 카테고리 선택
- 검색어 입력
- 페이지 변경

명령 후보:
```bash
npm run lint
npm run build
```

완료 조건:
- 화면이 API contract와 연결됨
- notice/free route에 직접 진입 가능
- 빈 상태와 오류 상태가 깨지지 않음

### Level 5: Runtime Compose 검증
명령 후보:
```powershell
powershell -ExecutionPolicy Bypass -File scripts\dev\verify-compose.ps1 -App
powershell -ExecutionPolicy Bypass -File scripts\dev\up.ps1
```

완료 조건:
- Compose config validation 성공
- Docker 권한이 있으면 app profile 기동 성공
- Docker 권한이 없으면 권한 실패 메시지를 증거로 기록

### Level 6: 사람 리뷰

리뷰 자료:
- 변경 파일 목록
- 실행한 테스트 명령
- 테스트 결과
- 스크린샷 또는 smoke evidence
- 남은 리스크

리뷰 기준:
- SSAFY 화면 흐름과 우선순위에 맞는가
- 과도한 추론이 들어가지 않았는가
- 문서, API, UI, DB가 서로 모순되지 않는가

## 실패 처리
- DB 실패: DevOps-QA가 error evidence를 기록하고 Backend가 SQL 수정 후보를 낸다.
- API 실패: Backend가 수정하고 DevOps-QA가 재검증한다.
- UI 실패: Frontend가 수정하고 DevOps-QA가 smoke를 재검증한다.
- 계약 충돌: `docs/api-summary.md`, `docs/openapi.json`, `docs/next_plan.md`, 이 문서를 먼저 맞춘 뒤 작업을 재개한다.
- 500줄 초과: 해당 lane이 파일을 분리한 뒤 재검증한다.

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
- 현재 sandbox는 Docker 엔진 pipe 접근 권한이 없어 실제 컨테이너 기동 검증이 실패할 수 있다.
- 로컬 Maven이 없으면 Backend 테스트는 Docker 또는 CI에서 실행해야 한다.
- 기존 ERD/SQL 문서와 캡처 도구 일부는 500줄을 넘는다. 새 구현 코드 제한과 별도 정리 대상으로 본다.
