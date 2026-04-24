# eduSSAFY Clone Coding

## Worker-4 status sync (2026-04-25)
- Team backlog has been re-synced with explicit follow-up tasks 125-130 for unresolved auth, attachment/reaction, survey/support depth, and E2E/CI closure.
- Project remains **NOT READY** until those tasks and remaining-work gates are closed with fresh verification evidence.


SSAFY 교육 플랫폼을 기준으로 구현 중인 full-stack clone입니다. 현재 로컬 Docker Compose app profile을 통해 frontend, backend, MySQL, Redis, RabbitMQ, Nginx를 한 번에 띄워 브라우저에서 확인할 수 있습니다.

## Current project status (2026-04-24)

- Overall readiness: **NOT READY / PARTIAL**. The repository is a runnable full-stack clone scaffold, but it is not final full-clone complete.
- Passing verification checks: Dockerized backend `mvn -B test` on Java 21, frontend `npm run lint`, frontend `npm run build`, Docker Compose config render/build, app-profile startup with healthy containers, and basic local HTTP smoke.
- Blocked/unknown checks: PowerShell smoke because `pwsh`/`powershell` is unavailable on this macOS verification host; browser E2E/visual fidelity and live CI run evidence are still missing.
- Major remaining product gaps: production auth/session/RBAC, durable notification/support/survey/material workflows, attachments, board edit/delete/permissions, browser E2E/visual verification, and final all-PASS verification.
- Source of truth for readiness: `docs/final-verification.md` and `docs/remaining-work.md`.

## Quick Start: localhost 전체 확인

PowerShell에서 저장소 루트로 이동한 뒤 실행합니다.

```powershell
cd C:\Users\kwanyeol\Desktop\eduSSAFY-clone-coding
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\dev\localhost.ps1 -Smoke -Open
```

Docker 권한 문제가 있으면 먼저 아래를 관리자 PowerShell에서 실행하고 Docker Desktop/Codex/터미널을 재시작하세요.

```powershell
net localgroup docker-users "DESKTOP-KPGHMRC\CodexSandboxOffline" /add
net localgroup docker-users "DESKTOP-KPGHMRC\CodexSandboxUsers" /add
```

## Main URLs

- App via Nginx: http://localhost:18000
- Login: http://localhost:18000/login
- Backend health: http://localhost:18080/actuator/health
- Backend API through Nginx: http://localhost:18000/api/me
- Nginx health: http://localhost:18000/nginx-health
- RabbitMQ management: http://localhost:25673
  - user: `ssafy`
  - password: `ssafy_dev_password`

Demo login:

- email: `student@ssafy.com`
- password: `password`

## Browser screen checklist

- http://localhost:18000/
- http://localhost:18000/login
- http://localhost:18000/mycampus/attendance
- http://localhost:18000/mycampus/attendance/appeals/new
- http://localhost:18000/mycampus/level
- http://localhost:18000/mycampus/notifications
- http://localhost:18000/learning/curriculum
- http://localhost:18000/learning/replays
- http://localhost:18000/learning/materials
- http://localhost:18000/learning/materials/1
- http://localhost:18000/learning/materials/1/viewer
- http://localhost:18000/quest
- http://localhost:18000/quest/1
- http://localhost:18000/quest/1/submit
- http://localhost:18000/survey
- http://localhost:18000/survey/1
- http://localhost:18000/survey/1/respond
- http://localhost:18000/community/free
- http://localhost:18000/community/free/1
- http://localhost:18000/community/free/write
- http://localhost:18000/community/classmates
- http://localhost:18000/help/notice
- http://localhost:18000/help/notice/1
- http://localhost:18000/help/faq
- http://localhost:18000/help/faq/1
- http://localhost:18000/help/qna
- http://localhost:18000/help/qna/new
- http://localhost:18000/profile/check
- http://localhost:18000/profile/edit

## Verification commands

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\dev\smoke.ps1
cd frontend
npm run lint
npm run build
cd ..\backend
docker run --rm -v "${PWD}:/workspace" -w /workspace maven:3.9.9-eclipse-temurin-21 mvn -B test
```

If `mvn` is installed locally, `mvn -B test` from `backend/` is also valid.


## Documentation status

Required project documents are maintained under `docs/`:

- `docs/progress.md` — current rounds, worker progress, commits, and changed-file summaries.
- `docs/architecture.md` — stack, runtime boundaries, domain modules, and architectural gaps.
- `docs/api-summary.md` — implemented API surface and contract guardrails.
- `docs/test-report.md` — verification evidence, blocked checks, and retest commands.
- `docs/remaining-work.md` — required work that prevents final PASS.
- `docs/final-verification.md` — final readiness gate; currently **NOT READY** until remaining implementation and verification blockers close.

## Known current caveats

- R6 source includes `POST /api/community/classmates/{userId}/notifications`; if live smoke returns 404 for that optional check, rebuild the backend image with `scripts\dev\localhost.ps1 -Smoke` or `scripts\dev\up.ps1 -App`.
- Material reaction API is intentionally still future work and is optional in smoke.
- Git commit from Codex may require `.git` ACL recovery; see `scripts/dev/README.md`.
