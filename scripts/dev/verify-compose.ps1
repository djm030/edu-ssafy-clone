[CmdletBinding()]
param(
  [switch]$App,
  [switch]$SkipDocker
)

$ErrorActionPreference = "Stop"
$repoRoot = Resolve-Path (Join-Path $PSScriptRoot "..\..")
. (Join-Path $PSScriptRoot "docker-env.ps1")

function Assert-FileExists {
  param([string]$Path)
  if (-not (Test-Path $Path)) {
    throw "Required file is missing: $Path"
  }
}

function Assert-ServiceDeclared {
  param(
    [string]$ComposeFile,
    [string]$Service
  )
  $pattern = "^\s{2}$([regex]::Escape($Service)):"
  if (-not (Select-String -Path $ComposeFile -Pattern $pattern -Quiet)) {
    throw "Service '$Service' is not declared in $ComposeFile."
  }
}

function Assert-SeedMounted {
  param([string]$ComposeFile)
  $seedPath = "./scripts/mysql/20-board-list-seed.sql"
  if (-not (Select-String -Path $ComposeFile -SimpleMatch $seedPath -Quiet)) {
    throw "Priority seed is not mounted in $ComposeFile."
  }
}

function Invoke-ComposeConfig {
  param([string[]]$Args, [string]$Label)
  Write-Host "Validating $Label..."
  docker compose @Args config --quiet
  if ($LASTEXITCODE -ne 0) {
    throw "$Label compose config is invalid."
  }
}

Push-Location $repoRoot

try {
  Assert-FileExists "compose.yml"
  Assert-FileExists "compose.mysql.yml"
  Assert-FileExists "compose.observability.yml"
  Assert-FileExists "scripts/mysql/20-board-list-seed.sql"

  foreach ($service in @("nginx", "mysql", "redis", "rabbitmq", "backend", "frontend")) {
    Assert-ServiceDeclared "compose.yml" $service
  }

  foreach ($service in @("elasticsearch", "logstash", "kibana", "filebeat")) {
    Assert-ServiceDeclared "compose.observability.yml" $service
  }

  Assert-SeedMounted "compose.yml"
  Assert-SeedMounted "compose.mysql.yml"

  $docker = Get-Command docker -ErrorAction SilentlyContinue
  if ($SkipDocker -or -not $docker) {
    Write-Host "Docker CLI unavailable or skipped; static compose checks completed."
    return
  }

  Initialize-DockerCliConfig

  Invoke-ComposeConfig -Args @("-f", "compose.mysql.yml") -Label "MySQL verification stack"
  Invoke-ComposeConfig -Args @("-f", "compose.yml") -Label "base runtime stack"
  Invoke-ComposeConfig -Args @("-f", "compose.yml", "-f", "compose.observability.yml") -Label "runtime plus observability stack"

  if ($App) {
    Assert-FileExists "backend"
    Assert-FileExists "frontend"
    Invoke-ComposeConfig -Args @("-f", "compose.yml", "--profile", "app") -Label "app profile stack"
  }

  Write-Host "Compose validation completed."
}
finally {
  Pop-Location
}
