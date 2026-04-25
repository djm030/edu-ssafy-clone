# SSAFY Full Clone - TEAM (4 Worker Fixed Roles)

이 팀은 반드시 `omx team 4:executor`로 실행한다.
모든 worker는 아래 역할만 수행하며, 역할 혼합은 금지한다.

## Worker Role Lock (Hard Rule)
- `worker-1` (PM / Architect)
  - API 계약 고정
  - 작업 범위 잠금
  - 완료 기준 관리
  - PASS/PARTIAL/FAIL/UNKNOWN 판정
  - `docs/remaining-work.md`, `docs/progress.md` 관리
- `worker-2` (Backend)
  - Spring Boot API
  - DB/MySQL
  - 인증/RBAC
  - 첨부파일
  - 문의/설문/퀘스트/알림 API
  - Spring REST Docs snippet
- `worker-3` (Frontend)
  - React 화면
  - Tailwind CSS
  - API client
  - 첨부 UI
  - 문의/설문/퀘스트/알림 UX
  - 401/403/빈 상태/로딩/에러 처리
- `worker-4` (DevOps/QA)
  - Docker Compose
  - Nginx
  - smoke test
  - E2E test
  - CI
  - REST Docs 노출 검증
  - `docs/test-report.md` 관리

## Non-Mixing Guardrail
- 각 worker는 자기 lane 외 작업을 하지 않는다.
- 다른 lane 파일을 수정해야 하면 작업을 넘기고 PM(worker-1)이 재할당한다.
- 문서만 수정하고 완료 처리하지 않는다.
- 기능 완료는 구현 + 연동 + 검증 증거가 있어야 한다.

## Execution Loop
1. `worker-1`: 기능 범위/완료조건/API 계약 확정, owner 배정
2. `worker-2`: backend+db+rbac+restdocs 구현
3. `worker-3`: frontend 연동 및 상태/권한 UX 처리
4. `worker-4`: compose/nginx/smoke/e2e/ci/restdocs 노출 검증
5. `worker-1`: PASS/PARTIAL/FAIL/UNKNOWN 판정 및 진행 문서 갱신

## Baseline Verification Commands
- `bash scripts/dev/backend-test.sh`
- `cd backend && mvn -B prepare-package`
- `cd frontend && npm exec -- tsc --noEmit -p tsconfig.app.json`
- `cd frontend && npm run build`
- `docker compose -f compose.yml config`
- `docker compose -f compose.yml --profile app exec -T nginx nginx -t`
- `python3 scripts/dev/smoke-lite.py`

## REST Docs Rule
- 변경 API는 REST Docs 테스트/스니펫/HTML 생성 근거가 있어야 한다.
- `/v3/api-docs` 또는 Swagger UI 유무만으로 완료 판정하지 않는다.

## Launch Command
```bash
OMX_DEFAULT_SPARK_MODEL=gpt-5.3-codex-spark omx team 4:executor "$(cat prompts/ssafy-full-clone-team.md)"
```

## Output Contract
기능 / worker-1 판정 / worker-2 결과 / worker-3 결과 / worker-4 결과 / 검증증거 / 커밋 / 남은 blocker / 다음 기능
