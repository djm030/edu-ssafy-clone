# Worker 3 Follow-up Task Creation

## Task 80 — 남은 작업이 있으면 다음 task를 생성한다

- Status: remaining work exists after Task 50 failed authorization verification.
- Existing related implementation task: Task 118 (`R7-rbac: 역할 기반 권한 검사와 401/403 UI 상태 구현`).
- New follow-up verification task created: `R7-rbac 검증: 401/403 및 역할별 접근 테스트 추가`.
- Purpose: after R7 RBAC/session implementation lands, verify unauthorized, forbidden, and authorized success paths across backend/API smoke/frontend states.

## Why this follow-up exists

Task 50 failed because backend authorization enforcement markers were absent. The next work should not only add RBAC/session implementation, but also prove the 401/403 behavior with tests so the same gap is not reintroduced.
