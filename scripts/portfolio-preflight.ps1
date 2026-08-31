[CmdletBinding()]
param(
    [switch]$CheckRunningServices
)

$ErrorActionPreference = "Stop"
$projectRoot = Split-Path -Parent $PSScriptRoot
$applicationPath = Join-Path $projectRoot "MarinboySalon_v3\back\src\main\resources\application.yml"
$frontendEnvironmentPath = Join-Path $projectRoot "MarinboySalon_v3\front\.env.example"
$apiConfigPath = Join-Path $projectRoot "MarinboySalon_v3\front\api\apiConfig.js"

function Assert-FileContains {
    param(
        [string]$Path,
        [string]$ExpectedText,
        [string]$Message
    )

    if ((Get-Content -LiteralPath $Path -Raw) -notmatch [regex]::Escape($ExpectedText)) {
        throw $Message
    }
}

# 제출 시 V3 API 주소가 8082로 통일됐는지 먼저 확인합니다.
Assert-FileContains $applicationPath "port: 8082" "V3 백엔드 포트가 8082로 고정되어 있지 않습니다."
Assert-FileContains $frontendEnvironmentPath "http://localhost:8082" "프런트 예제 API 주소가 8082가 아닙니다."
Assert-FileContains $apiConfigPath "http://localhost:8082" "공통 API 기본 주소가 8082가 아닙니다."

if ($CheckRunningServices) {
    # 실제 시연 전에 백엔드와 프런트 응답을 확인해 포트 충돌을 발견합니다.
    Invoke-WebRequest "http://localhost:8082/actuator/health" -UseBasicParsing | Out-Null
    Invoke-WebRequest "http://localhost:3000/" -UseBasicParsing | Out-Null
}

Write-Host "포트폴리오 사전 점검 통과: V3 프런트 3000 / 백엔드 8082 기준입니다." -ForegroundColor Green
