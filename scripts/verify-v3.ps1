[CmdletBinding()]
param(
    [switch]$SkipFrontendInstall
)

$ErrorActionPreference = "Stop"
$projectRoot = Split-Path -Parent $PSScriptRoot
$v3Root = Join-Path $projectRoot "MarinboySalon_v3"

function Invoke-V3Step {
    param(
        [string]$Name,
        [string]$Path,
        [scriptblock]$Command
    )

    Write-Host "`n[$Name] START" -ForegroundColor Cyan
    Push-Location $Path
    try {
        & $Command
        if ($LASTEXITCODE -ne 0) {
            throw "$Name failed with exit code $LASTEXITCODE."
        }
    }
    finally {
        Pop-Location
    }
    Write-Host "[$Name] PASS" -ForegroundColor Green
}

# 포트폴리오 제출본은 V1·V2가 아닌 V3만 빌드해 실행 대상 혼동을 막습니다.
Invoke-V3Step "V3 backend test and package" (Join-Path $v3Root "back") {
    .\gradlew.bat clean test bootJar
}

Invoke-V3Step "V3 frontend test and build" (Join-Path $v3Root "front") {
    if (-not $SkipFrontendInstall) {
        npm ci
        if ($LASTEXITCODE -ne 0) {
            throw "V3 frontend dependency installation failed."
        }
    }
    npm audit --audit-level=high
    if ($LASTEXITCODE -ne 0) {
        throw "V3 frontend dependency security audit failed."
    }
    npm test -- --runInBand --passWithNoTests
    if ($LASTEXITCODE -ne 0) {
        throw "V3 frontend tests failed."
    }
    npm run build
}

Write-Host "`nV3 PORTFOLIO CHECKS PASSED." -ForegroundColor Green
