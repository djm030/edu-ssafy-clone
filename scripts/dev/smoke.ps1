[CmdletBinding()]
param(
  [string]$BaseUrl = "http://localhost",
  [string]$BackendUrl = "http://localhost:8080",
  [switch]$SkipHttp
)

$ErrorActionPreference = "Stop"
$repoRoot = Resolve-Path (Join-Path $PSScriptRoot "..\..")

function Assert-LineLimit {
  param(
    [string]$Path,
    [int]$MaxLines = 500
  )
  $lineCount = (Get-Content $Path | Measure-Object -Line).Lines
  if ($lineCount -gt $MaxLines) {
    throw "$Path has $lineCount lines; limit is $MaxLines."
  }
}

function Assert-SeedContains {
  param([string]$Needle)
  if (-not (Select-String -Path "scripts/mysql/20-board-list-seed.sql" -SimpleMatch $Needle -Quiet)) {
    throw "Priority seed coverage is missing: $Needle"
  }
}

function Assert-VerifyContains {
  param([string]$Needle)
  if (-not (Select-String -Path "scripts/mysql/verify-schema.sql" -SimpleMatch $Needle -Quiet)) {
    throw "Priority verification coverage is missing: $Needle"
  }
}

function Assert-FileContains {
  param(
    [string]$Path,
    [string]$Needle
  )
  if (-not (Select-String -Path $Path -SimpleMatch $Needle -Quiet)) {
    throw "Expected static path is missing in ${Path}: $Needle"
  }
}

function Invoke-SmokeRequest {
  param(
    [string]$Url,
    [string]$Method = "GET",
    [string]$Body = $null
  )
  try {
    $args = @{
      Uri = $Url
      Method = $Method
      TimeoutSec = 5
      UseBasicParsing = $true
    }
    if ($Body) {
      $args.ContentType = "application/json"
      $args.Body = $Body
    }
    $response = Invoke-WebRequest @args
    Write-Host "$Method $Url -> HTTP $($response.StatusCode)"
    return $response
  }
  catch {
    throw "$Method $Url failed ($($_.Exception.Message))"
  }
}

function Test-HttpEndpoint {
  param(
    [string]$Url,
    [string]$Method = "GET",
    [string]$Body = $null
  )
  [void](Invoke-SmokeRequest -Url $Url -Method $Method -Body $Body)
}

function Get-FirstItemId {
  param(
    [string]$Url,
    [string]$Label
  )
  $response = Invoke-SmokeRequest -Url $Url
  $json = $response.Content | ConvertFrom-Json
  if (-not $json.items -or $json.items.Count -lt 1 -or -not $json.items[0].id) {
    throw "No seeded $Label id returned from $Url."
  }
  return $json.items[0].id
}

function Test-OptionalHttpEndpoint {
  param(
    [string]$Label,
    [string]$Url,
    [string]$Method = "GET",
    [string]$Body = $null
  )
  try {
    Test-HttpEndpoint -Url $Url -Method $Method -Body $Body
  }
  catch {
    Write-Warning "Optional smoke '$Label' skipped/failed: $($_.Exception.Message)"
  }
}

Push-Location $repoRoot

try {
  $handWrittenFiles = @(
    "README.md",
    "compose.yml",
    "compose.mysql.yml",
    "compose.observability.yml",
    ".env.example",
    "infra/nginx/conf.d/default.conf",
    "infra/logstash/pipeline/logstash.conf",
    "infra/filebeat/filebeat.yml",
    "scripts/dev/README.md",
    "scripts/dev/diagnose-git.ps1",
    "scripts/dev/localhost.ps1",
    "scripts/dev/verify-compose.ps1",
    "scripts/dev/smoke.ps1",
    "scripts/mysql/20-board-list-seed.sql",
    "scripts/mysql/verify-schema.sql",
    "scripts/mysql/verify-schema.ps1",
    "scripts/mysql/README.md"
  )

  foreach ($file in $handWrittenFiles) {
    if (Test-Path $file) {
      Assert-LineLimit $file
    }
  }

  foreach ($needle in @(
    "user_level_statuses",
    "attendance_records",
    "learning_materials",
    "quest_evaluations",
    "surveys",
    "notification_recipients",
    "lecture_replays",
    "support_tickets",
    "support_ticket_messages",
    "classmate@ssafy.com",
    "Java Collections Review",
    "MySQL Schema Practice",
    "REST API study notes",
    "Seed QNA support ticket message.",
    "How was this week?",
    "Good",
    "Seed survey response for submit smoke checks.",
    "'faq'",
    "'qna'"
  )) {
    Assert-SeedContains $needle
  }

  foreach ($needle in @(
    "seeded_profile_password_read_path",
    "seeded_classmates",
    "seeded_weekly_curriculum",
    "seeded_lecture_replays",
    "seeded_support_tickets",
    "seeded_board_detail_posts",
    "seeded_learning_material_detail_resources",
    "seeded_quest_detail_submission",
    "seeded_survey_detail_options",
    "seeded_attendance_appeal_submit_ready",
    "seeded_survey_response_submission",
    "seeded_board_write_relations"
  )) {
    Assert-VerifyContains $needle
  }

  foreach ($route in @(
    "/mycampus/attendance",
    "/profile/check",
    "/community/free",
    "/community/classmates",
    "/help/qna",
    "/quest",
    "/survey"
  )) {
    Assert-FileContains "frontend/src/App.tsx" $route
  }

  foreach ($apiPath in @(
    "/api/auth/login",
    "/api/dashboard/summary",
    "/api/attendance/records",
    "/api/community/classmates",
    "/api/boards/qna/posts",
    "/api/profile/password-check",
    "/api/quests/",
    "/api/surveys/"
  )) {
    Assert-FileContains "frontend/src/api/app.ts" $apiPath
  }

  Assert-FileContains "backend/src/main/java/com/edussafy/backend/priority/api/CommunityController.java" "/classmates/{userId}/notifications"

  & (Join-Path $PSScriptRoot "verify-compose.ps1")

  if ($SkipHttp) {
    Write-Host "HTTP smoke checks skipped."
    return
  }

  Test-HttpEndpoint "$BaseUrl/nginx-health"
  Test-HttpEndpoint "$BackendUrl/actuator/health"
  Test-HttpEndpoint "$BackendUrl/api/health"
  Test-HttpEndpoint "$BackendUrl/api/auth/login" "POST" '{"email":"student@ssafy.com","password":"password"}'
  Test-HttpEndpoint "$BackendUrl/api/me"
  Test-HttpEndpoint "$BackendUrl/api/profile"
  Test-HttpEndpoint "$BackendUrl/api/profile/password-check" "POST" '{"password":"password"}'
  Test-HttpEndpoint "$BackendUrl/api/dashboard/summary"
  Test-HttpEndpoint "$BackendUrl/api/attendance/records"
  Test-HttpEndpoint "$BackendUrl/api/notifications?page=1&size=5"
  Test-HttpEndpoint "$BackendUrl/api/community/classmates"
  Test-OptionalHttpEndpoint "R6 classmate notification API (requires rebuilt backend image)" "$BackendUrl/api/community/classmates/1/notifications" "POST" '{"type":"contact_request","message":"Smoke classmate notification."}'
  Test-HttpEndpoint "$BackendUrl/api/learning/curriculum"
  Test-HttpEndpoint "$BackendUrl/api/learning/replays"
  Test-HttpEndpoint "$BackendUrl/api/learning/materials?keyword=REST%20API&page=1&size=5"
  Test-HttpEndpoint "$BackendUrl/api/learning/materials?type=file&page=1&size=5"
  Test-HttpEndpoint "$BackendUrl/api/learning/materials/1"
  Test-HttpEndpoint "$BackendUrl/api/learning/materials/1/resources"
  Test-HttpEndpoint "$BackendUrl/api/quests?page=1&size=5"
  Test-HttpEndpoint "$BackendUrl/api/quests/1"
  Test-HttpEndpoint "$BackendUrl/api/surveys?page=1&size=5"
  Test-HttpEndpoint "$BackendUrl/api/surveys/1"
  Test-HttpEndpoint "$BackendUrl/api/boards/notice/categories"
  Test-HttpEndpoint "$BackendUrl/api/boards/notice/posts?keyword=Welcome&page=1&size=5"
  $noticePostId = Get-FirstItemId "$BackendUrl/api/boards/notice/posts?keyword=Welcome&page=1&size=1" "notice post"
  Test-HttpEndpoint "$BackendUrl/api/boards/notice/posts/$noticePostId"
  Test-HttpEndpoint "$BackendUrl/api/boards/free/posts?page=1&size=5"
  Test-HttpEndpoint "$BackendUrl/api/boards/free/posts?keyword=REST%20API&page=1&size=5"
  $freePostId = Get-FirstItemId "$BackendUrl/api/boards/free/posts?keyword=REST%20API&page=1&size=1" "free post"
  Test-HttpEndpoint "$BackendUrl/api/boards/free/posts/$freePostId"
  Test-HttpEndpoint "$BackendUrl/api/boards/faq/categories"
  Test-HttpEndpoint "$BackendUrl/api/boards/faq/posts?page=1&size=5"
  Test-HttpEndpoint "$BackendUrl/api/boards/qna/categories"
  Test-HttpEndpoint "$BackendUrl/api/boards/qna/posts?page=1&size=5"
  Test-HttpEndpoint "$BackendUrl/api/support/tickets?page=1&size=5"
  Test-HttpEndpoint "$BackendUrl/api/support/tickets" "POST" '{"title":"Smoke support ticket","content":"Created by smoke check."}'
  Test-HttpEndpoint "$BackendUrl/api/attendance/appeals" "POST" '{"type":"status_change","requestedStatus":"present","reason":"Smoke attendance appeal."}'
  Test-HttpEndpoint "$BackendUrl/api/profile" "PUT" '{"name":"Demo Learner","mobilePhone":"010-1234-5678","addressLine1":"Smoke address"}'
  Test-HttpEndpoint "$BackendUrl/api/boards/free/posts" "POST" '{"title":"Smoke free board post","content":"Created by smoke check."}'
  Test-HttpEndpoint "$BackendUrl/api/boards/qna/posts" "POST" '{"title":"Smoke QNA board post","content":"Created by smoke check."}'
  Test-HttpEndpoint "$BackendUrl/api/boards/free/posts/$freePostId/comments" "POST" '{"content":"Smoke board comment."}'
  Test-HttpEndpoint "$BackendUrl/api/boards/free/posts/$freePostId/reactions" "POST" '{"type":"like"}'
  Test-HttpEndpoint "$BackendUrl/api/quests/1/submissions" "POST" '{"content":"Smoke quest submission."}'
  Test-HttpEndpoint "$BackendUrl/api/surveys/1/responses" "POST" '{"answers":[{"questionId":1,"optionIds":[1],"answerText":"Smoke survey response."}]}'

  Test-OptionalHttpEndpoint "learning material reaction API (not implemented in R5/R6 backend surface)" "$BackendUrl/api/learning/materials/1/reactions" "POST" '{"type":"like"}'
}
finally {
  Pop-Location
}
