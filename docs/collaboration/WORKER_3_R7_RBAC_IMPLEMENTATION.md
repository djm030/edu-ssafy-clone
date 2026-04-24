# Worker 3 R7 RBAC Implementation

## Task 118 — R7-rbac: 역할 기반 권한 검사와 401/403 UI 상태 구현

- Status: minimal RBAC guard implemented in backend and verified with controller tests.
- Backend change: `RoleAccessInterceptor` protects `/api/**` except public health/login routes, supports demo unauthorized simulation through `X-Demo-Auth: false`, and enforces coach/admin-only access for classmate notification sends.
- Backend config: `RoleAccessWebConfig` registers the interceptor for API paths.
- Frontend state: existing `fetchJson` behavior already refuses fallback for 401/403 and surfaces Korean unauthorized/forbidden messages through page `DataState` error rendering.

## Verification

- `docker run --rm -v "$PWD/backend:/workspace" -w /workspace maven:3.9.9-eclipse-temurin-21 mvn -q test` -> PASS.
- `cd frontend && npm run lint && npm run build` -> PASS.

## Follow-up

This is a minimal demo RBAC layer. Production-grade session/token storage and broader per-endpoint permission matrices should continue under the remaining R7/RBAC follow-up tasks.
