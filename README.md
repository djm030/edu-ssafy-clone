# eduSSAFY Clone Coding

SSAFY 교육 플랫폼을 기준으로 구현 중인 full-stack clone입니다. 현재 로컬 Docker Compose app profile을 통해 frontend, backend, MySQL, Redis, RabbitMQ, Nginx를 한 번에 띄워 브라우저에서 확인할 수 있습니다.

## Current project status (2026-04-26)

- Overall readiness: **PASS for the implemented local full-stack clone surface; production launch still requires live environment verification**.
- Passing verification checks: Dockerized backend Maven tests on Java 21, frontend `npm run lint`, frontend `npm run build`, Docker Compose config rendering, Nginx/backend smoke contracts, Spring REST Docs snippets, and the `/ops/readiness` frontend smoke runner.
- Blocked/unknown checks: browser E2E/visual fidelity, live CI evidence, and a latest-image rebuild against Docker Hub can still be environment-dependent.
- Major remaining production gaps: real deployment secret management, browser E2E/visual verification, external observability wiring, and final live all-PASS evidence.
- Source of truth for readiness: `docs/final-verification.md` and `docs/remaining-work.md`.

## Quick Start: localhost 전체 확인

PowerShell에서 저장소 루트로 이동한 뒤 실행합니다.

```powershell
cd C:\Users\kwanyeol\Desktop\eduSSAFY-clone-coding
Copy-Item .env.example .env
# Edit .env and replace every change-me value.
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\dev\localhost.ps1 -Smoke -Open
```

macOS/Linux에서 빠르게 설정을 확인하려면:

```bash
cp .env.example .env
$EDITOR .env # replace every change-me value
docker compose config
docker compose --profile app up -d --build
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
- Production readiness smoke screen: http://localhost/ops/readiness
- Nginx health: http://localhost/nginx-health
- RabbitMQ management: http://localhost:15672
  - user/password: `.env`의 `RABBITMQ_DEFAULT_USER` / `RABBITMQ_DEFAULT_PASS`

Demo login:

- email: `student@ssafy.com`
- password: `password`

Session/cookie hardening defaults are controlled through `.env`:


Production profile notes:

- Keep `SPRING_PROFILES_ACTIVE=docker` for the bundled local Compose demo.
- For production-like deployments set `SPRING_PROFILES_ACTIVE=prod` and provide `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, `SPRING_RABBITMQ_USERNAME`, and `SPRING_RABBITMQ_PASSWORD`; the prod profile intentionally has no secret fallbacks.
- The prod profile defaults session cookies to `Secure` and `SameSite=Strict`; override only when a trusted HTTPS edge/proxy policy requires a different value.

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
- http://localhost/ops/readiness
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
