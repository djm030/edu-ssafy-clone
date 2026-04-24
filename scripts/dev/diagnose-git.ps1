[CmdletBinding()]
param(
  [switch]$Remote
)

$ErrorActionPreference = "Stop"
$repoRoot = Resolve-Path (Join-Path $PSScriptRoot "..\..")
$safeDirectory = ($repoRoot.Path -replace '\\', '/')

Push-Location $repoRoot
try {
  Write-Host "== Identity =="
  $identity = [System.Security.Principal.WindowsIdentity]::GetCurrent()
  Write-Host "name=$($identity.Name)"
  Write-Host "sid=$($identity.User.Value)"

  Write-Host ""
  Write-Host "== Git status with safe.directory =="
  git -c "safe.directory=$safeDirectory" status --short --branch
  if ($LASTEXITCODE -ne 0) {
    throw "git status failed even with safe.directory=$safeDirectory"
  }

  Write-Host ""
  Write-Host "== .git metadata write check =="
  $probe = Join-Path $repoRoot ".git\omx-write-probe-$([System.Guid]::NewGuid().ToString('N')).tmp"
  try {
    [System.IO.File]::WriteAllText($probe, "probe")
    Remove-Item -LiteralPath $probe -Force
    Write-Host "git_metadata_writable=true"
  }
  catch {
    Write-Host "git_metadata_writable=false"
    Write-Host "git_metadata_error=$($_.Exception.Message)"
    Write-Host "Fix from an elevated/owner host shell, then restart Codex if needed:"
    Write-Host "  icacls `"$($repoRoot.Path)\.git`" /grant `"DESKTOP-KPGHMRC\CodexSandboxOffline:(OI)(CI)M`" /T"
  }

  Write-Host ""
  Write-Host "== Recent commits =="
  git -c "safe.directory=$safeDirectory" log --oneline --decorate -5

  if ($Remote) {
    Write-Host ""
    Write-Host "== Remote check =="
    git -c "safe.directory=$safeDirectory" ls-remote origin main
    if ($LASTEXITCODE -ne 0) {
      throw "git remote check failed. Verify network/auth from the host shell."
    }
  }
}
finally {
  Pop-Location
}
