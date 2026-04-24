[CmdletBinding()]
param(
  [string]$ComposeFile = "compose.mysql.yml",
  [string]$Service = "mysql",
  [string]$SchemaPath = "/work/docs/revised_schema_mysql8.sql",
  [string]$SeedPath = "/work/scripts/mysql/20-board-list-seed.sql",
  [string]$VerifyPath = "/work/scripts/mysql/verify-schema.sql",
  [int]$WaitSeconds = 120,
  [switch]$Down
)

$ErrorActionPreference = "Stop"

$repoRoot = Resolve-Path (Join-Path $PSScriptRoot "..\..")
Push-Location $repoRoot

try {
  $pingCommand = 'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysql -N -uroot -e "SELECT @@port" | grep -qx 3306'
  $schemaCommand = 'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysql -uroot "$MYSQL_DATABASE" < ' + $SchemaPath
  $seedCommand = 'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysql -uroot "$MYSQL_DATABASE" < ' + $SeedPath
  $verifyCommand = 'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysql -uroot "$MYSQL_DATABASE" < ' + $VerifyPath

  Write-Host "Starting MySQL 8 verification container..."
  docker compose -f $ComposeFile up -d $Service
  if ($LASTEXITCODE -ne 0) {
    throw "docker compose up failed."
  }

  Write-Host "Waiting for MySQL readiness..."
  $deadline = (Get-Date).AddSeconds($WaitSeconds)
  $ready = $false
  while ((Get-Date) -lt $deadline) {
    docker compose -f $ComposeFile exec -T $Service sh -lc $pingCommand *> $null
    if ($LASTEXITCODE -eq 0) {
      $ready = $true
      break
    }
    Start-Sleep -Seconds 2
  }

  if (-not $ready) {
    throw "MySQL did not become ready within $WaitSeconds seconds."
  }

  Write-Host "Applying schema: $SchemaPath"
  docker compose -f $ComposeFile exec -T $Service sh -lc $schemaCommand
  if ($LASTEXITCODE -ne 0) {
    throw "Schema execution failed."
  }

  Write-Host "Applying seed: $SeedPath"
  docker compose -f $ComposeFile exec -T $Service sh -lc $seedCommand
  if ($LASTEXITCODE -ne 0) {
    throw "Seed execution failed."
  }

  Write-Host "Running verification queries: $VerifyPath"
  docker compose -f $ComposeFile exec -T $Service sh -lc $verifyCommand
  if ($LASTEXITCODE -ne 0) {
    throw "Verification queries failed."
  }

  Write-Host "MySQL schema verification completed."
}
finally {
  if ($Down) {
    Write-Host "Stopping MySQL verification container and removing volume..."
    docker compose -f $ComposeFile down -v
  }
  Pop-Location
}
