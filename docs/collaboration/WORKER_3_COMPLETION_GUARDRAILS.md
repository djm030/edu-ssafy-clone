# Worker 3 Completion Guardrails

## Task 25 — 절대 금지 / 다음은 금지한다

Worker-3 records the following hard guardrails before continuing implementation or verification work:

- Do not complete a task with only analysis when the task requires implementation, verification, or repository evidence.
- Do not mark mock-only UI/data as a completed full-clone feature.
- Do not declare the project complete while `docs/remaining-work.md` still contains partial/gap rows.
- Do not treat Docker configuration or compose syntax alone as feature implementation completion.
- Do not delete or replace existing Docker settings unless a later scoped task explicitly authorizes that edit.
- Do not ignore failed verification; read logs, fix if in scope, and rerun the relevant check.

## Current enforcement evidence

- Task 3 documented completed/partial feature evidence in `docs/collaboration/WORKER_3_COMPLETED_FEATURES_AUDIT.md`.
- Task 8 documented executable verification in `docs/collaboration/WORKER_3_VERIFICATION_REPORT.md`.
- Tasks 15 and 16 documented Docker inspection and non-replacement in `docs/collaboration/WORKER_3_DOCKER_CONFIG_AUDIT.md`.

## Task 29 — 계획만 작성하고 종료

- Status: this anti-pattern is explicitly rejected.
- Worker-3 must not stop at a plan-only artifact when assigned implementation, verification, or guardrail work.
- Evidence from this run: worker-3 created committed verification/guardrail artifacts and completed lifecycle transitions before moving to the next feasible task.

## Task 33 — mock-only 구현을 완료로 처리

- Status: this anti-pattern is explicitly rejected.
- Mock/demo data may support local UX continuity, but a feature cannot be marked complete unless the relevant backend contract, persistence/authorization requirements, and verification evidence are present or the remaining gap is documented.
- Current evidence: `docs/remaining-work.md` still contains partial/gap rows; therefore mock-only or fallback-only flows must remain partial.

## Task 39 — `docs/remaining-work.md`에 필수 작업이 있는데 완료 선언

- Status: this anti-pattern is explicitly rejected.
- `docs/remaining-work.md` is a blocking completion gate: if it still lists required partial/gap work, worker-3 must report the project as incomplete and continue planning/executing follow-up work instead of declaring final completion.
- Current evidence from this run: the remaining-work guard check counted 16 partial/gap rows, so the repository can pass local verification gates while still remaining non-final.

## Task 43 — Docker 설정만 있는 상태를 구현 완료로 판단

- Status: this anti-pattern is explicitly rejected.
- Docker/compose files prove runtime wiring only; they do not prove application feature behavior, authorization, persistence, UI states, or end-to-end acceptance.
- Completion claims must pair runtime configuration with source implementation and verification evidence.
