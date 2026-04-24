# eduSSAFY Clone Coding

SSAFY 교육 플랫폼을 기준으로 구현 중인 full-stack clone입니다. 현재 로컬 Docker Compose app profile을 통해 frontend, backend, MySQL, Redis, RabbitMQ, Nginx를 한 번에 띄워 브라우저에서 확인할 수 있습니다.

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

## Known current caveats

- R6 source includes `POST /api/community/classmates/{userId}/notifications`; if live smoke returns 404 for that optional check, rebuild the backend image with `scripts\dev\localhost.ps1 -Smoke` or `scripts\dev\up.ps1 -App`.
- Material reaction API is intentionally still future work and is optional in smoke.
- Git commit from Codex may require `.git` ACL recovery; see `scripts/dev/README.md`.
