# Worker 3 RBAC Verification

## Task 131 — R7-rbac 검증: 401/403 및 역할별 접근 테스트 추가

- Status: RBAC verification added through backend controller tests and rerun after Task 118 implementation.
- Unauthorized check: `GET /api/me` with `X-Demo-Auth: false` returns 401 and `UNAUTHORIZED` JSON error.
- Forbidden check: learner/default role posting `/api/community/classmates/{id}/notifications` returns 403 and `FORBIDDEN` JSON error.
- Authorized success check: coach role posting `/api/community/classmates/{id}/notifications` returns 201 and classmate notification payload.

## Verification commands

- `docker run --rm -v "$PWD/backend:/workspace" -w /workspace maven:3.9.9-eclipse-temurin-21 mvn -q test` -> PASS.
- `cd frontend && npm run lint && npm run build` -> PASS.

## Remaining verification gap

PowerShell smoke scripts were not run locally because this worker lacks `pwsh`/`powershell`. Host/CI should add or rerun live smoke coverage for the same 401/403/coach-success cases.
