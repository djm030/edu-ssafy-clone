# eduSSAFY Clone Coding

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

- App via Nginx: http://localhost
- Login: http://localhost/login
- Backend health: http://localhost:8080/actuator/health
- Backend API through Nginx: http://localhost/api/me
- Nginx health: http://localhost/nginx-health
- RabbitMQ management: http://localhost:15672
  - user: `ssafy`
  - password: `ssafy_dev_password`

Demo login:

- email: `student@ssafy.com`
- password: `password`

Session/cookie hardening defaults are controlled through `.env`:

- `EDUSSAFY_AUTH_ALLOW_NOOP_PASSWORDS=false` keeps legacy plaintext-style demo hashes disabled.
- `SERVER_SERVLET_SESSION_TIMEOUT=30m` aligns the servlet container with the app session TTL.
- Set `SERVER_SERVLET_SESSION_COOKIE_SECURE=true` when serving through HTTPS.
- `SERVER_SERVLET_SESSION_COOKIE_SAME_SITE=lax` is the local default for form/navigation flows.

## Browser screen checklist

- http://localhost/
- http://localhost/login
- http://localhost/mycampus/attendance
- http://localhost/mycampus/attendance/appeals/new
- http://localhost/mycampus/level
- http://localhost/mycampus/notifications
- http://localhost/learning/curriculum
- http://localhost/learning/replays
- http://localhost/learning/materials
- http://localhost/learning/materials/1
- http://localhost/learning/materials/1/viewer
- http://localhost/quest
- http://localhost/quest/1
- http://localhost/quest/1/submit
- http://localhost/survey
- http://localhost/survey/1
- http://localhost/survey/1/respond
- http://localhost/community/free
- http://localhost/community/free/1
- http://localhost/community/free/write
- http://localhost/community/classmates
- http://localhost/help/notice
- http://localhost/help/notice/1
- http://localhost/help/faq
- http://localhost/help/faq/1
- http://localhost/help/qna
- http://localhost/help/qna/new
- http://localhost/profile/check
- http://localhost/profile/edit

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
