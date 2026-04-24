# Dev Harness Runbook

This folder contains the local execution and recovery harness for the SSAFY clone. Use these commands from the repository root unless noted.

## Previously Successful Git Path

Evidence: `git log --oneline -5` shows `c63fec7 (HEAD -> main, origin/main) Stabilize the clone enough for phase verification`, which was committed and pushed after Git ownership/safe-directory handling was fixed.

Use this host-shell path when Codex is blocked from writing `.git` metadata:

```powershell
cd C:\Users\kwanyeol\Desktop\eduSSAFY-clone-coding
$repo = 'C:/Users/kwanyeol/Desktop/eduSSAFY-clone-coding'

git -c safe.directory=$repo status --short --branch
git -c safe.directory=$repo add <coherent-file-list>
git -c safe.directory=$repo commit -m "feat(scope): concise change summary"
git -c safe.directory=$repo push origin main
```

If Codex must commit from its sandbox user, grant `.git` metadata write access from an elevated or repository-owner PowerShell, then restart the Codex/OMX session so the token picks up the ACL:

```powershell
$repo = 'C:\Users\kwanyeol\Desktop\eduSSAFY-clone-coding'
icacls "$repo\.git" /grant "DESKTOP-KPGHMRC\CodexSandboxOffline:(OI)(CI)M" /T
```

Diagnostic harness:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\dev\diagnose-git.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\dev\diagnose-git.ps1 -Remote
```

`-Remote` also checks `origin/main`; use it only when network/auth should be available.

## Previously Successful Docker / Runtime Path

Evidence from R5 tracker: the app profile was rebuilt and smoke-tested with:

```powershell
docker compose -f compose.yml --profile app up -d --build
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\dev\smoke.ps1
```

For browser-based local testing, use the localhost harness. It starts the app profile, optionally runs smoke checks, prints every screen/API URL, and can open a few representative pages:

```powershell
cd C:\Users\kwanyeol\Desktop\eduSSAFY-clone-coding
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\dev\localhost.ps1 -Smoke -Open
```

Backend tests passed using Dockerized Maven because local `mvn` was not on PATH:

```powershell
cd C:\Users\kwanyeol\Desktop\eduSSAFY-clone-coding\backend
docker run --rm -v "${PWD}:/workspace" -w /workspace maven:3.9.9-eclipse-temurin-21 mvn -B test
```

Frontend gates:

```powershell
cd C:\Users\kwanyeol\Desktop\eduSSAFY-clone-coding\frontend
npm run lint
npm run build
```

## Docker Access Recovery

If Docker works from the normal `kwanyeol` shell but not from Codex, add the sandbox identity to `docker-users` from Administrator PowerShell, then restart Docker Desktop and the Codex/OMX session:

```powershell
net localgroup docker-users "DESKTOP-KPGHMRC\CodexSandboxOffline" /add
net localgroup docker-users "DESKTOP-KPGHMRC\CodexSandboxUsers" /add
```

Use the repo helpers instead of raw Docker commands when possible; they set a temp `DOCKER_CONFIG` to avoid host Docker config ACL issues:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\dev\diagnose-docker.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\dev\verify-compose.ps1 -App
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\dev\up.ps1 -App
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\dev\smoke.ps1
```

## R6 Retest Sequence After ACL Fix

```powershell
cd C:\Users\kwanyeol\Desktop\eduSSAFY-clone-coding
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\dev\diagnose-git.ps1 -Remote
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\dev\diagnose-docker.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\dev\up.ps1 -App
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\dev\smoke.ps1
cd backend
docker run --rm -v "${PWD}:/workspace" -w /workspace maven:3.9.9-eclipse-temurin-21 mvn -B test
cd ..\frontend
npm run lint
npm run build
cd ..
```

Then commit by coherent unit with the Git path above.
