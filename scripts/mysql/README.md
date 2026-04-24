# MySQL Schema Verification Harness

This harness verifies that `docs/revised_schema_mysql8.sql` executes on MySQL 8 and that the Priority 1/2 seed data exists.

## Prerequisites
- Docker Desktop or Docker Engine with Compose support
- PowerShell

## Run
```powershell
Copy-Item .env.example .env
powershell -ExecutionPolicy Bypass -File scripts/mysql/verify-schema.ps1
```

To remove the MySQL container and volume after verification:

```powershell
powershell -ExecutionPolicy Bypass -File scripts/mysql/verify-schema.ps1 -Down
```

## Scope
The script validates the SQL baseline and seed coverage for dashboard, attendance appeals, weekly curriculum, lecture replays, learning material resources, quest submissions, survey questions/options/responses, notifications, classmates, profile/password-check data, support tickets, and shared board APIs (`notice`, `free`, `faq`, `qna`). It uses `compose.mysql.yml` so it does not rebuild or start application images.

For safe local checks that do not require a running database, use:

```powershell
powershell -ExecutionPolicy Bypass -File scripts/dev/smoke.ps1 -SkipHttp
```
