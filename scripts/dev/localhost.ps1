[CmdletBinding()]
param(
  [switch]$NoStart,
  [switch]$Smoke,
  [switch]$Open,
  [int]$HttpPort = 80,
  [int]$BackendPort = 8080,
  [int]$FrontendDevPort = 5173
)

$ErrorActionPreference = "Stop"
$repoRoot = Resolve-Path (Join-Path $PSScriptRoot "..\..")

function Format-LocalUrl {
  param([int]$Port)
  if ($Port -eq 80) { return "http://localhost" }
  return "http://localhost:$Port"
}

function Write-Urls {
  param(
    [string]$BaseUrl,
    [string]$BackendUrl,
    [string]$FrontendDevUrl
  )

  $screenRoutes = @(
    "/login",
    "/",
    "/mycampus/attendance",
    "/mycampus/attendance/appeals/new",
    "/mycampus/level",
    "/mycampus/notifications",
    "/learning/curriculum",
    "/learning/replays",
    "/learning/materials",
    "/learning/materials/1",
    "/learning/materials/1/viewer",
    "/quest",
    "/quest/1",
    "/quest/1/submit",
    "/survey",
    "/survey/1",
    "/survey/1/respond",
    "/community/free",
    "/community/free/1",
    "/community/free/write",
    "/community/classmates",
    "/help/notice",
    "/help/notice/1",
    "/help/faq",
    "/help/faq/1",
    "/help/qna",
    "/help/qna/new",
    "/profile/check",
    "/profile/edit"
  )

  $apiRoutes = @(
    "/nginx-health",
    "/api/health",
    "/actuator/health",
    "/api/me",
    "/api/profile",
    "/api/dashboard/summary",
    "/api/attendance/records",
    "/api/notifications?page=1&size=5",
    "/api/community/classmates",
    "/api/learning/curriculum",
    "/api/learning/replays",
    "/api/learning/materials?page=1&size=5",
    "/api/learning/materials/1",
    "/api/learning/materials/1/resources",
    "/api/quests?page=1&size=5",
    "/api/quests/1",
    "/api/surveys?page=1&size=5",
    "/api/surveys/1",
    "/api/boards/notice/categories",
    "/api/boards/notice/posts?page=1&size=5",
    "/api/boards/free/posts?page=1&size=5",
    "/api/support/tickets?page=1&size=5"
  )

  Write-Host ""
  Write-Host "== Main localhost entrypoints =="
  Write-Host "App via nginx:      $BaseUrl"
  Write-Host "Backend direct:     $BackendUrl"
  Write-Host "Frontend dev mode:  $FrontendDevUrl"
  Write-Host "Login demo:         student@ssafy.com / password"

  Write-Host ""
  Write-Host "== Browser screens =="
  foreach ($route in $screenRoutes) {
    Write-Host ($BaseUrl + $route)
  }

  Write-Host ""
  Write-Host "== API smoke URLs =="
  foreach ($route in $apiRoutes) {
    Write-Host ($BaseUrl + $route)
  }

  if ($Open) {
    Start-Process ($BaseUrl + "/login")
    Start-Process ($BaseUrl + "/")
    Start-Process ($BaseUrl + "/learning/materials")
    Start-Process ($BaseUrl + "/community/free")
    Start-Process ($BaseUrl + "/help/qna")
  }
}

Push-Location $repoRoot
try {
  $baseUrl = Format-LocalUrl -Port $HttpPort
  $backendUrl = "http://localhost:$BackendPort"
  $frontendDevUrl = "http://localhost:$FrontendDevPort"

  if (-not $NoStart) {
    Write-Host "Starting app profile with Docker Compose..."
    & (Join-Path $PSScriptRoot "up.ps1") -App
  }

  if ($Smoke) {
    Write-Host "Running smoke checks..."
    & (Join-Path $PSScriptRoot "smoke.ps1") -BaseUrl $baseUrl -BackendUrl $backendUrl
  }

  Write-Urls -BaseUrl $baseUrl -BackendUrl $backendUrl -FrontendDevUrl $frontendDevUrl
}
finally {
  Pop-Location
}
