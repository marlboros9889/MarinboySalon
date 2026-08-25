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

    Write-Host "`n[$Name] 시작" -ForegroundColor Cyan
    Push-Location $Path
    try {
        & $Command
        if ($LASTEXITCODE -ne 0) {
            throw "$Name 검증이 종료 코드 $LASTEXITCODE 로 실패했습니다."
        }
    }
    finally {
        Pop-Location
    }
    Write-Host "[$Name] 통과" -ForegroundColor Green
}

# 각 버전을 독립적으로 빌드해 숨은 버전 간 의존성을 막습니다.
Invoke-ProjectStep "v1 Maven 테스트" (Join-Path $projectRoot "MarinboySalon_v1") {
    mvn clean test
}

Invoke-ProjectStep "v2 Maven 테스트" (Join-Path $projectRoot "MarinboySalon_v2") {
    mvn clean test
}

Invoke-ProjectStep "v3 백엔드 테스트와 패키징" (Join-Path $projectRoot "MarinboySalon_v3\back") {
    .\gradlew.bat clean test bootJar
}

Invoke-ProjectStep "v3 프론트엔드 테스트와 빌드" (Join-Path $projectRoot "MarinboySalon_v3\front") {
    if (-not $SkipFrontendInstall) {
        npm ci
        if ($LASTEXITCODE -ne 0) {
            throw "v3 프론트엔드 의존성 설치가 실패했습니다."
        }
    }
    npm audit --audit-level=high
    if ($LASTEXITCODE -ne 0) {
        throw "v3 프론트엔드 의존성 보안 점검이 실패했습니다."
    }
    npm test -- --runInBand --passWithNoTests
    if ($LASTEXITCODE -ne 0) {
        throw "v3 프론트엔드 테스트가 실패했습니다."
    }
    npm run build
}

Write-Host "`n모든 프로젝트 검증을 통과했습니다." -ForegroundColor Green
