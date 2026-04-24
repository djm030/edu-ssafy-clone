[CmdletBinding()]
param()

$ErrorActionPreference = "Stop"
. (Join-Path $PSScriptRoot "docker-env.ps1")

function Write-Section {
  param([string]$Title)
  Write-Host ""
  Write-Host "== $Title =="
}

function Invoke-NativeDiagnostic {
  param(
    [string]$Label,
    [string]$FilePath,
    [string[]]$ArgumentList
  )

  Write-Section $Label
  $id = [System.Guid]::NewGuid().ToString("N")
  $stdout = Join-Path ([System.IO.Path]::GetTempPath()) "omx-docker-$id.out"
  $stderr = Join-Path ([System.IO.Path]::GetTempPath()) "omx-docker-$id.err"
  try {
    $process = Start-Process -FilePath $FilePath `
      -ArgumentList $ArgumentList `
      -NoNewWindow `
      -Wait `
      -PassThru `
      -RedirectStandardOutput $stdout `
      -RedirectStandardError $stderr
    Get-Content -LiteralPath $stdout, $stderr -ErrorAction SilentlyContinue |
      ForEach-Object { Write-Host $_ }
    Write-Host "exit=$($process.ExitCode)"
  }
  catch {
    Write-Host "error=$($_.Exception.Message)"
  }
  finally {
    Remove-Item -LiteralPath $stdout, $stderr -Force -ErrorAction SilentlyContinue
  }
}

Initialize-DockerCliConfig

$identity = [System.Security.Principal.WindowsIdentity]::GetCurrent()
Write-Section "Identity"
Write-Host "name=$($identity.Name)"
Write-Host "sid=$($identity.User.Value)"
Write-Host "docker_config=$env:DOCKER_CONFIG"

Write-Section "docker-users"
try {
  Get-LocalGroupMember -Group "docker-users" |
    Select-Object Name, ObjectClass, SID |
    Format-Table -AutoSize
}
catch {
  Write-Host "error=$($_.Exception.Message)"
}

Invoke-NativeDiagnostic "Docker context" "docker" @("context", "ls")
Invoke-NativeDiagnostic "Docker compose config" "docker" @("compose", "-f", "compose.yml", "config", "--services")
Invoke-NativeDiagnostic "Docker engine" "docker" @("version")

Write-Section "Diagnosis"
if ($identity.Groups.Value -notcontains "S-1-5-32-544") {
  Write-Host "current_user_is_not_local_admin=true"
}
Write-Host "If Docker engine reports pipe access denied, add this identity or CodexSandboxUsers to docker-users, then restart this session."
