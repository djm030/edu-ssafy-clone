[CmdletBinding()]
param(
  [switch]$Observability,
  [switch]$Follow,
  [string[]]$Service = @()
)

$ErrorActionPreference = "Stop"
$repoRoot = Resolve-Path (Join-Path $PSScriptRoot "..\..")
. (Join-Path $PSScriptRoot "docker-env.ps1")
Push-Location $repoRoot

try {
  Initialize-DockerCliConfig
  Assert-DockerEngineAccess

  $composeArgs = @("-f", "compose.yml")
  if ($Observability) {
    $composeArgs += @("-f", "compose.observability.yml")
  }

  $logArgs = @("logs", "--tail", "200")
  if ($Follow) {
    $logArgs += "-f"
  }
  $logArgs += $Service

  docker compose @composeArgs @logArgs
  if ($LASTEXITCODE -ne 0) {
    throw "docker compose logs failed."
  }
}
finally {
  Pop-Location
}
