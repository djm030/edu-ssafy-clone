# Worker 3 Test Rerun

## Task 114 — 가능한 테스트 재실행

- Status: feasible tests rerun from worker-3 worktree on 2026-04-25.

## Rerun results

- Frontend dependency bootstrap: `cd frontend && npm ci` PASS (installed dependencies; only engine warning for Node v23.6.0 vs eslint-visitor-keys preferred range).
| Check | Command | Result |
|---|---|---|
| Frontend lint/build | `cd frontend && npm run lint && npm run build` | PASS — ESLint passed; `tsc -b && vite build` passed; 68 modules transformed (vite v7.3.2). |
| Backend tests | `docker run --rm -v "$PWD/backend:/workspace" -w /workspace maven:3.9.9-eclipse-temurin-21 mvn -q test` | PASS — command exited 0; Spring controller/service test suite completed without failures. |

- Task 114 sync: this rerun evidence was reused to close both verification tasks 8 and 114 on 2026-04-25.

## Not rerun locally

- PowerShell smoke scripts: `pwsh`/`powershell` is unavailable in this macOS worker.
- Live app profile smoke: not started in this pane; should be rerun in host/CI after RBAC/session work lands.
