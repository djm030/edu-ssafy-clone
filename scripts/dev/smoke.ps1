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


function Assert-JsonProperty {
  param(
    [Parameter(Mandatory = $true)]$Json,
    [Parameter(Mandatory = $true)][string]$Path,
    [string]$Label = "response"
  )

  $current = $Json
  foreach ($segment in $Path.Split('.')) {
    if ($null -eq $current) {
      throw "$Label JSON shape is missing '$Path'."
    }

    if ($current -is [System.Collections.IEnumerable] -and -not ($current -is [string]) -and -not ($current -is [pscustomobject])) {
      $current = @($current)
      if ($current.Count -lt 1) {
        throw "$Label JSON shape has an empty array before '$Path'."
      }
      $current = $current[0]
    }

    if (-not ($current.PSObject.Properties.Name -contains $segment)) {
      throw "$Label JSON shape is missing '$Path'."
    }
    $current = $current.$segment
  }

  if ($null -eq $current) {
    throw "$Label JSON shape has null '$Path'."
  }

  return $current
}

function Assert-JsonArray {
  param(
    [Parameter(Mandatory = $true)]$Json,
    [Parameter(Mandatory = $true)][string]$Path,
    [string]$Label = "response",
    [switch]$AllowEmpty
  )

  $value = Assert-JsonProperty -Json $Json -Path $Path -Label $Label
  $items = @($value)
  if (-not $AllowEmpty -and $items.Count -lt 1) {
    throw "$Label JSON shape has empty '$Path'."
  }
  return $items
}

function Invoke-SmokeJson {
  param(
    [string]$Url,
    [string]$Method = "GET",
    [string]$Body = $null
  )

  $response = Invoke-SmokeRequest -Url $Url -Method $Method -Body $Body
  try {
    return $response.Content | ConvertFrom-Json
  }
  catch {
    throw "$Method $Url did not return valid JSON ($($_.Exception.Message))"
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


function Assert-JsonPropertyPath {
  param(
    [object]$Json,
    [string]$Path
  )
  $current = $Json
  foreach ($segment in $Path.Split('.')) {
    if ($null -eq $current) {
      throw "JSON path '$Path' is missing at '$segment'."
    }
    $property = $current.PSObject.Properties[$segment]
    if ($null -eq $property) {
      throw "JSON path '$Path' is missing at '$segment'."
    }
    $current = $property.Value
  }
}

function Test-JsonShape {
  param(
    [string]$Label,
    [string]$Url,
    [string[]]$RequiredPaths,
    [string]$Method = "GET",
    [string]$Body = $null
  )
  $response = Invoke-SmokeRequest -Url $Url -Method $Method -Body $Body
  $json = $response.Content | ConvertFrom-Json
  foreach ($path in $RequiredPaths) {
    Assert-JsonPropertyPath -Json $json -Path $path
  }
  Write-Host "JSON shape $Label -> $($RequiredPaths -join ', ')"
  return $json
}

function Get-FirstItemId {
  param(
    [string]$Url,
    [string]$Label
  )
  $json = Invoke-SmokeJson -Url $Url
  $items = Assert-JsonArray -Json $json -Path "items" -Label "$Label list"
  [void](Assert-JsonProperty -Json $items[0] -Path "id" -Label "$Label item")
  return $items[0].id
}

function Test-AuthJsonShape {
  param([string]$BackendUrl)

  $login = Invoke-SmokeJson -Url "$BackendUrl/api/auth/login" -Method "POST" -Body '{"email":"student@ssafy.com","password":"password"}'
  foreach ($path in @("user.id", "user.email", "user.name", "user.role")) {
    [void](Assert-JsonProperty -Json $login -Path $path -Label "login")
  }

  $me = Invoke-SmokeJson -Url "$BackendUrl/api/me"
  foreach ($path in @("user.id", "user.email", "user.name", "user.campusName", "user.cohortName", "user.trackName")) {
    [void](Assert-JsonProperty -Json $me -Path $path -Label "me")
  }
}

function Test-ProfileJsonShape {
  param([string]$BackendUrl)

  $profile = Invoke-SmokeJson -Url "$BackendUrl/api/profile"
  foreach ($path in @("profile.id", "profile.email", "profile.name", "profile.campusName", "profile.cohortName", "profile.trackName")) {
    [void](Assert-JsonProperty -Json $profile -Path $path -Label "profile")
  }
}

function Test-BoardJsonShape {
  param([string]$BackendUrl)

  $list = Invoke-SmokeJson -Url "$BackendUrl/api/boards/free/posts?keyword=REST%20API&page=1&size=1"
  $items = Assert-JsonArray -Json $list -Path "items" -Label "board list"
  foreach ($path in @("id", "boardCode", "title", "authorName", "createdAt")) {
    [void](Assert-JsonProperty -Json $items[0] -Path $path -Label "board list item")
  }
  foreach ($path in @("page.page", "page.size", "page.totalItems", "page.totalPages")) {
    [void](Assert-JsonProperty -Json $list -Path $path -Label "board list")
  }

  $postId = $items[0].id
  $detail = Invoke-SmokeJson -Url "$BackendUrl/api/boards/free/posts/$postId"
  foreach ($path in @("post.id", "post.boardCode", "post.title", "post.content", "post.engagement.commentCount", "post.engagement.reactionCount")) {
    [void](Assert-JsonProperty -Json $detail -Path $path -Label "board detail")
  }

  $created = Invoke-SmokeJson -Url "$BackendUrl/api/boards/free/posts" -Method "POST" -Body '{"title":"Smoke free board post","content":"Created by smoke check."}'
  foreach ($path in @("item.id", "item.boardCode", "item.title", "item.content", "item.authorName", "item.createdAt")) {
    [void](Assert-JsonProperty -Json $created -Path $path -Label "board create")
  }
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
    "scripts/dev/verify-openapi.ps1",
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
  & (Join-Path $PSScriptRoot "verify-openapi.ps1")

  if ($SkipHttp) {
    Write-Host "HTTP smoke checks skipped."
    return
  }

  Test-HttpEndpoint "$BaseUrl/nginx-health"
  Test-HttpEndpoint "$BaseUrl/api/readiness"
  Test-HttpEndpoint "$BackendUrl/actuator/health"
  Test-HttpEndpoint "$BackendUrl/api/health"
  Test-HttpEndpoint "$BackendUrl/api/readiness"
  Test-AuthJsonShape $BackendUrl
  Test-ProfileJsonShape $BackendUrl
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
  Test-JsonShape "notice board detail" "$BackendUrl/api/boards/notice/posts/$noticePostId" @("post.id", "post.title", "post.engagement")
  Test-HttpEndpoint "$BackendUrl/api/boards/free/posts?page=1&size=5"
  Test-HttpEndpoint "$BackendUrl/api/boards/free/posts?keyword=REST%20API&page=1&size=5"
  Test-BoardJsonShape $BackendUrl
  $freePostId = Get-FirstItemId "$BackendUrl/api/boards/free/posts?keyword=REST%20API&page=1&size=1" "free post"
  Test-JsonShape "free board detail" "$BackendUrl/api/boards/free/posts/$freePostId" @("post.id", "post.title", "post.engagement")
  Test-HttpEndpoint "$BackendUrl/api/boards/faq/categories"
  Test-HttpEndpoint "$BackendUrl/api/boards/faq/posts?page=1&size=5"
  Test-HttpEndpoint "$BackendUrl/api/boards/qna/categories"
  Test-HttpEndpoint "$BackendUrl/api/boards/qna/posts?page=1&size=5"
  Test-HttpEndpoint "$BackendUrl/api/support/tickets?page=1&size=5"
  Test-HttpEndpoint "$BackendUrl/api/support/tickets" "POST" '{"title":"Smoke support ticket","content":"Created by smoke check."}'
  Test-HttpEndpoint "$BackendUrl/api/attendance/appeals" "POST" '{"type":"status_change","requestedStatus":"present","reason":"Smoke attendance appeal."}'
  Test-HttpEndpoint "$BackendUrl/api/profile" "PUT" '{"name":"Demo Learner","mobilePhone":"010-1234-5678","addressLine1":"Smoke address"}'
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
