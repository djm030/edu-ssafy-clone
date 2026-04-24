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
