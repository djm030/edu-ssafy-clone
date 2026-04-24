# Worker 3 Failure Recovery

## Task 73 — 실패 처리

- Status: failure-handling procedure applied after Task 50 failed truthfully.
- Failed task: Task 50, authorization checks for auth-sensitive features.
- Handling performed: stopped short of a false completion claim, captured the failure evidence, and identified concrete follow-up implementation work.

## Failure recovery sequence

1. Read the failure evidence from the task lifecycle and source scan.
2. Preserve the audit artifact (`docs/collaboration/WORKER_3_AUTHZ_CHECK_AUDIT.md`) so the failure is actionable.
3. Re-run a focused verification scan for backend authorization markers before deciding whether the task can be completed.
4. Continue to adjacent feasible tasks instead of stopping the worker session.

## Recommended follow-up

Implement R7 RBAC/session work in a scoped task: backend session/token validation, role guards for protected endpoints, frontend 401/403 states, and API/smoke tests for unauthorized/forbidden/success cases.
