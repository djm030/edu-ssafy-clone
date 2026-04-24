# Test and Smoke Availability Check

Date: 2026-04-24
Worker: worker-2
Task: 55 - 가능한 테스트 또는 smoke test가 있다.

## Result

The repository has executable verification paths for both application code and smoke coverage.

## Available Checks

| Area | Command / file | Current result |
| --- | --- | --- |
| Frontend lint | `npm --prefix frontend run lint` | PASS |
| Frontend build/typecheck | `npm --prefix frontend run build` | PASS in prior worker-2 runs for this session |
| Backend tests | `docker run --rm -v "$PWD/backend:/workspace" -w /workspace maven:3.9.9-eclipse-temurin-21 mvn test` | PASS, 32 tests, 0 failures, 0 errors, 0 skipped |
| Docker compose render | `docker compose -f compose.yml --profile app config` | PASS |
| Static/local smoke harness | `scripts/dev/smoke.ps1 -SkipHttp` | Present, but not executable in this macOS worker because `pwsh`/`powershell` is unavailable |
| OpenAPI drift smoke | `scripts/dev/verify-openapi.ps1` | Present, but not executable in this macOS worker because `pwsh`/`powershell` is unavailable |

## Smoke Harness Evidence

- `scripts/dev/smoke.ps1` exists and includes static checks plus optional live HTTP checks for local app URLs.
- `scripts/dev/verify-openapi.ps1` exists and checks OpenAPI/smoke marker drift for core auth/profile/board contracts.
- `scripts/dev/verify-compose.ps1` exists for compose validation in PowerShell-capable environments.

## Backend Test Evidence

Dockerized Maven test execution completed successfully:

```text
Tests run: 32, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## Limitation

PowerShell smoke scripts could not be run directly in this worker because neither `pwsh` nor `powershell` is installed. The scripts are present and remain executable from a PowerShell-capable host/CI environment.
