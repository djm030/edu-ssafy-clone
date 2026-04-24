[CmdletBinding()]
param(
  [string]$SpecPath = "docs/openapi.yaml"
)

$ErrorActionPreference = "Stop"
$repoRoot = Resolve-Path (Join-Path $PSScriptRoot "..\..")
$fullPath = Join-Path $repoRoot $SpecPath

if (-not (Test-Path $fullPath)) {
  throw "OpenAPI contract artifact is missing: $SpecPath"
}

$content = Get-Content $fullPath -Raw
foreach ($needle in @(
  "openapi: 3.0.3",
  "/api/auth/login:",
  "/api/me:",
  "/api/profile:",
  "/api/boards/{boardCode}/posts:",
  "/api/boards/{boardCode}/posts/{postId}:",
  "BoardPostDetailResponse:",
  "BoardPostCreateResponse:",
  "ApiErrorResponse:"
)) {
  if (-not $content.Contains($needle)) {
    throw "OpenAPI contract artifact is missing required marker: $needle"
  }
}

Write-Host "OpenAPI contract artifact verified: $SpecPath"
