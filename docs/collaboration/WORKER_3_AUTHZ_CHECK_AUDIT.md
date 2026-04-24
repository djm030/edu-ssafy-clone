# Worker 3 AuthZ Check Audit

## Task 50 — 인증/인가가 필요한 기능은 권한 검사가 있다

- Status: permission-check audit performed from worker-3 worktree on 2026-04-24.
- Scope inspected: backend source, frontend source, and remaining-work acceptance notes.
- Result: authorization checks are **not yet complete** for all protected functionality; this must remain a blocking gap rather than a completed feature claim.

## Evidence observed

- Backend exposes auth-related endpoints (`/api/auth/login`, `/api/me`, `/api/auth/roles/current`, `/api/auth/logout`) through `AuthController`.
- Frontend API client preserves 401/403 by refusing demo fallback for those statuses and returns Korean unauthorized/error messages.
- No Spring Security configuration, `@PreAuthorize`, or equivalent backend method/controller authorization guard was found in the current backend source scan.
- `docs/remaining-work.md` already records `Access control` as a gap and calls out frontend unauthorized states as still needed.

## Required follow-up before completion

- Add real backend session/token validation and role/permission checks for mutation and protected read endpoints.
- Add frontend unauthorized states for 401/403 flows without falling back to mock data.
- Extend smoke/API tests to cover unauthorized, forbidden, and authorized success paths.

## Guardrail

Task 50 cannot be considered fully implemented in this worktree yet. This audit records the current state and prevents a false completion claim until actual authorization enforcement and tests are added.
