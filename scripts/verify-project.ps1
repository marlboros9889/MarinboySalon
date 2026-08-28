[CmdletBinding()]
param(
    [switch]$SkipFrontendInstall
)

$ErrorActionPreference = "Stop"
$projectRoot = Split-Path -Parent $PSScriptRoot

function Invoke-ProjectStep {
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

# 각 버전을 독립적으로 빌드해 숨은 버전 간 의존성을 막습니다.
Invoke-ProjectStep "v1 Maven test" (Join-Path $projectRoot "MarinboySalon_v1") {
    mvn clean test
}

Invoke-ProjectStep "v2 Maven test" (Join-Path $projectRoot "MarinboySalon_v2") {
    mvn clean test
}

Invoke-ProjectStep "v3 backend test and package" (Join-Path $projectRoot "MarinboySalon_v3\back") {
    .\gradlew.bat clean test bootJar
}

Invoke-ProjectStep "v3 frontend test and build" (Join-Path $projectRoot "MarinboySalon_v3\front") {
    if (-not $SkipFrontendInstall) {
        npm ci
        if ($LASTEXITCODE -ne 0) {
            throw "v3 frontend dependency installation failed."
        }
    }
    npm audit --audit-level=high
    if ($LASTEXITCODE -ne 0) {
        throw "v3 frontend dependency security audit failed."
    }
    npm test -- --runInBand --passWithNoTests
    if ($LASTEXITCODE -ne 0) {
        throw "v3 frontend tests failed."
    }
    npm run build
}

Write-Host "`nALL PROJECT CHECKS PASSED." -ForegroundColor Green
