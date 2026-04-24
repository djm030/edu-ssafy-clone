[CmdletBinding()]
param(
  [switch]$App,
  [switch]$Observability
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

  if ($App) {
    docker compose @composeArgs --profile app up -d
  }
  else {
    docker compose @composeArgs up -d mysql redis rabbitmq
  }

  if ($LASTEXITCODE -ne 0) {
    throw "docker compose up failed."
  }
}
finally {
  Pop-Location
}
