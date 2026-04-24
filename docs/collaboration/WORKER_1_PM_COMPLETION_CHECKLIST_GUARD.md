# Worker 1 PM Completion Checklist Guard

Date: 2026-04-24
Worker: worker-1
Task: 46 - PM 완료 착각 방지 체크리스트

## PASS Criteria

A feature row may be marked PASS only when all of the following are true:

1. Backend/API behavior exists or the feature is explicitly frontend-only.
2. Frontend screen or call site exists when user-facing behavior is required.
3. Auth/RBAC is enforced when the feature requires role-specific access.
4. Loading, empty, normal, and error states are handled for user-facing flows.
5. Relevant tests/smoke/build checks pass with fresh evidence.
6. API/screen/test documentation is updated.
7. There is no TODO-only, mock-only, or infrastructure-only substitute for the feature.

If any criterion is missing, the row must be PARTIAL, FAIL, or UNKNOWN. Final completion requires every core row to be PASS.

## Current Checklist Evidence

`docs/final-verification.md` already contains a feature table with PASS/PARTIAL/FAIL/UNKNOWN classifications. Current non-PASS examples include:

- PARTIAL: 인증/인가, 사용자 프로필, 캠퍼스/기수/반/트랙, 출석, 알림, 커리큘럼, 퀘스트, 설문, 게시판, 1:1 문의, 권한별 접근 제어, 테스트.
- FAIL: 첨부파일, 학습자료 반응, 게시글 첨부파일, 문의 첨부파일.
- UNKNOWN gates: PowerShell smoke harness and browser E2E / visual fidelity.

## Decision

The PM completion checklist is active and prevents mistaken completion. The project must remain NOT COMPLETE until all core feature rows satisfy the PASS criteria above.
