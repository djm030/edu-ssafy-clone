[CmdletBinding()]
param()

$ErrorActionPreference = "Stop"
$repoRoot = Resolve-Path (Join-Path $PSScriptRoot "..\..")
$specPath = Join-Path $repoRoot "docs/openapi.yaml"
$smokePath = Join-Path $repoRoot "scripts/dev/smoke.ps1"

function Assert-FileContains {
  param(
    [string]$Path,
    [string]$Needle
  )
  if (-not (Select-String -Path $Path -SimpleMatch $Needle -Quiet)) {
    throw "Expected contract marker is missing in ${Path}: $Needle"
  }
}

foreach ($path in @(
  "/api/auth/login:",
  "/api/me:",
  "/api/profile:",
  "/api/boards/{boardCode}/posts:",
  "/api/boards/{boardCode}/posts/{postId}:",
  "BoardPostDetailResponse:",
  "BoardPostCreateResponse:",
  "required: [user]",
  "required: [profile]",
  "required: [post]",
  "required: [item]"
)) {
  Assert-FileContains $specPath $path
}

foreach ($shape in @(
  "Test-AuthJsonShape",
  "Test-ProfileJsonShape",
  "Test-BoardJsonShape",
  "post.engagement.commentCount",
  "item.createdAt"
)) {
  Assert-FileContains $smokePath $shape
}

Write-Host "OpenAPI bootstrap/drift markers verified."
