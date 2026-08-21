[CmdletBinding()]
param([switch]$SkipRuntime)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

$repositoryRoot = [System.IO.Path]::GetFullPath((Split-Path -Parent $PSScriptRoot))
$v2Verifier = Join-Path $repositoryRoot 'marinboy_v2\scripts\verify-project.ps1'
$v3Verifier = Join-Path $repositoryRoot 'marinboy_v3\scripts\verify-project.ps1'

Write-Host '[1/3] v2 HttpSession 프로젝트 검증'
& $v2Verifier
if ($LASTEXITCODE -ne 0) {
    throw 'marinboy_v2 검증에 실패했습니다.'
}

Write-Host '[2/3] v3 JWT + Redis 프로젝트 검증'
if ($SkipRuntime) {
    & $v3Verifier -SkipRuntime
} else {
    & $v3Verifier
}
if ($LASTEXITCODE -ne 0) {
    throw 'marinboy_v3 검증에 실패했습니다.'
}

Write-Host '[3/3] 저장소 충돌·공백 검증'
Push-Location $repositoryRoot
try {
    git diff --check
    if ($LASTEXITCODE -ne 0) {
        throw 'Git diff 공백·충돌 검증에 실패했습니다.'
    }
} finally {
    Pop-Location
}

Write-Host 'v2·v3 전체 검증을 통과했습니다. 모바일은 검증 범위에서 제외됩니다.'
