function Initialize-DockerCliConfig {
  if ($env:DOCKER_CONFIG) {
    return
  }

  $configRoot = Join-Path ([System.IO.Path]::GetTempPath()) "omx-docker-config"
  New-Item -ItemType Directory -Force -Path $configRoot | Out-Null

  $configPath = Join-Path $configRoot "config.json"
  if (-not (Test-Path -LiteralPath $configPath)) {
    Set-Content -LiteralPath $configPath -Value "{}" -Encoding ascii
  }

  $env:DOCKER_CONFIG = $configRoot
}

function Assert-DockerEngineAccess {
  Initialize-DockerCliConfig

  $identity = [System.Security.Principal.WindowsIdentity]::GetCurrent()
  $id = [System.Guid]::NewGuid().ToString("N")
  $stdout = Join-Path ([System.IO.Path]::GetTempPath()) "omx-docker-$id.out"
  $stderr = Join-Path ([System.IO.Path]::GetTempPath()) "omx-docker-$id.err"
  $process = Start-Process -FilePath "docker" `
    -ArgumentList @("version", "--format", "{{.Server.Version}}") `
    -NoNewWindow `
    -Wait `
    -PassThru `
    -RedirectStandardOutput $stdout `
    -RedirectStandardError $stderr

  if ($process.ExitCode -eq 0) {
    Remove-Item -LiteralPath $stdout, $stderr -Force -ErrorAction SilentlyContinue
    return
  }

  $details = ((Get-Content -LiteralPath $stderr, $stdout -ErrorAction SilentlyContinue) -join "`n").Trim()
  Remove-Item -LiteralPath $stdout, $stderr -Force -ErrorAction SilentlyContinue

  throw @"
Docker engine is not reachable for $($identity.Name).
Docker error: $details
Expected fix: run from a Docker-authorized user shell, or add this user/group to the local docker-users group and restart the shell/session.
"@
}
