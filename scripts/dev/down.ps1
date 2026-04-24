[CmdletBinding()]
param(
  [switch]$Observability,
  [switch]$Volumes
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

  $downArgs = @("down")
  if ($Volumes) {
    $downArgs += "-v"
  }

  docker compose @composeArgs @downArgs
  if ($LASTEXITCODE -ne 0) {
    throw "docker compose down failed."
  }
}
finally {
  Pop-Location
}
