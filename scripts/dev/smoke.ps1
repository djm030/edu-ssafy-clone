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

function Test-HttpEndpoint {
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
  }
  catch {
    throw "$Method $Url failed ($($_.Exception.Message))"
  }
}

Push-Location $repoRoot

try {
  $handWrittenFiles = @(
    "compose.yml",
    "compose.mysql.yml",
    "compose.observability.yml",
    ".env.example",
    "infra/nginx/conf.d/default.conf",
    "infra/logstash/pipeline/logstash.conf",
    "infra/filebeat/filebeat.yml",
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
    "/help/qna",
    "/quest",
    "/survey"
  )) {
    Assert-FileContains "frontend/src/App.tsx" $route
  }

  foreach ($apiPath in @(
    "/api/boards/qna/posts",
    "/api/profile/password-check",
    "/api/quests/",
    "/api/surveys/"
  )) {
    Assert-FileContains "frontend/src/api/app.ts" $apiPath
  }

  & (Join-Path $PSScriptRoot "verify-compose.ps1")

  if ($SkipHttp) {
    Write-Host "HTTP smoke checks skipped."
    return
  }

  Test-HttpEndpoint "$BaseUrl/nginx-health"
  Test-HttpEndpoint "$BackendUrl/actuator/health"
  Test-HttpEndpoint "$BackendUrl/api/me"
  Test-HttpEndpoint "$BackendUrl/api/profile/password-check" "POST" '{"password":"password"}'
  Test-HttpEndpoint "$BackendUrl/api/notifications?page=1&size=5"
  Test-HttpEndpoint "$BackendUrl/api/learning/curriculum"
  Test-HttpEndpoint "$BackendUrl/api/learning/replays"
  Test-HttpEndpoint "$BackendUrl/api/learning/materials?keyword=REST%20API&page=1&size=5"
  Test-HttpEndpoint "$BackendUrl/api/learning/materials?type=file&page=1&size=5"
  Test-HttpEndpoint "$BackendUrl/api/quests?page=1&size=5"
  Test-HttpEndpoint "$BackendUrl/api/surveys?page=1&size=5"
  Test-HttpEndpoint "$BackendUrl/api/boards/notice/categories"
  Test-HttpEndpoint "$BackendUrl/api/boards/notice/posts?keyword=Welcome&page=1&size=5"
  Test-HttpEndpoint "$BackendUrl/api/boards/free/posts?page=1&size=5"
  Test-HttpEndpoint "$BackendUrl/api/boards/free/posts?keyword=REST%20API&page=1&size=5"
  Test-HttpEndpoint "$BackendUrl/api/boards/faq/categories"
  Test-HttpEndpoint "$BackendUrl/api/boards/faq/posts?page=1&size=5"
  Test-HttpEndpoint "$BackendUrl/api/boards/qna/categories"
  Test-HttpEndpoint "$BackendUrl/api/boards/qna/posts?page=1&size=5"
  Test-HttpEndpoint "$BackendUrl/api/support/tickets?page=1&size=5"
  Test-HttpEndpoint "$BackendUrl/api/support/tickets" "POST" '{"title":"Smoke support ticket","content":"Created by optional smoke check."}'
  Test-HttpEndpoint "$BackendUrl/api/attendance/appeals" "POST" '{"type":"status_change","requestedStatus":"present","reason":"Smoke attendance appeal."}'
  Test-HttpEndpoint "$BackendUrl/api/profile" "PUT" '{"name":"Demo Learner","mobilePhone":"010-1234-5678","addressLine1":"Smoke address"}'
  Test-HttpEndpoint "$BackendUrl/api/boards/free/posts" "POST" '{"title":"Smoke free board post","content":"Created by optional smoke check.","categoryId":1}'
  Test-HttpEndpoint "$BackendUrl/api/boards/free/posts/1/comments" "POST" '{"content":"Smoke board comment."}'
  Test-HttpEndpoint "$BackendUrl/api/boards/free/posts/1/reactions" "POST" '{"type":"like"}'
  Test-HttpEndpoint "$BackendUrl/api/quests/1/submissions" "POST" '{"content":"Smoke quest submission.","submitStatus":"submitted"}'
  Test-HttpEndpoint "$BackendUrl/api/surveys/1/responses" "POST" '{"answers":[{"questionId":1,"optionIds":[1],"answerText":"Smoke survey response."}]}'
}
finally {
  Pop-Location
}
