[CmdletBinding()]
param(
    [switch]$SkipRuntime
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

$projectRoot = [System.IO.Path]::GetFullPath((Split-Path -Parent $PSScriptRoot))
$backendRoot = Join-Path $projectRoot 'backend'
$frontendRoot = Join-Path $projectRoot 'frontend'

function Import-EnvironmentFile {
    param([string]$FilePath)

    if (-not (Test-Path -LiteralPath $FilePath)) {
        return
    }

    foreach ($line in Get-Content -LiteralPath $FilePath) {
        $trimmedLine = $line.Trim()
        if ($trimmedLine.Length -eq 0 -or $trimmedLine.StartsWith('#')) {
            continue
        }

        $separatorIndex = $trimmedLine.IndexOf('=')
        if ($separatorIndex -lt 1) {
            throw ".env.local 형식이 잘못되었습니다: $trimmedLine"
        }

        $key = $trimmedLine.Substring(0, $separatorIndex).Trim()
        $value = $trimmedLine.Substring($separatorIndex + 1).Trim()
        if ($value.StartsWith('"') -and $value.EndsWith('"')) {
            $value = $value.Substring(1, $value.Length - 2)
        }
        if ($value.Length -gt 0) {
            [Environment]::SetEnvironmentVariable($key, $value, 'Process')
        }
    }
}

# 재실행과 검증이 같은 로컬 설정을 사용해야 DB 연결 결과가 달라지지 않습니다.
Import-EnvironmentFile -FilePath (Join-Path $projectRoot '.env.local')
$defaultLocalProperties = Join-Path $projectRoot 'config\application-local.properties'
if ($env:MARINBOY_CONFIG_FILE) {
    $resolvedConfigFile = [System.IO.Path]::GetFullPath($env:MARINBOY_CONFIG_FILE)
    $env:SPRING_CONFIG_ADDITIONAL_LOCATION = 'optional:file:' + $resolvedConfigFile.Replace('\', '/')
} elseif (Test-Path -LiteralPath $defaultLocalProperties) {
    $resolvedConfigFile = [System.IO.Path]::GetFullPath($defaultLocalProperties)
    $env:SPRING_CONFIG_ADDITIONAL_LOCATION = 'optional:file:' + $resolvedConfigFile.Replace('\', '/')
}

function Invoke-CheckedCommand {
    param(
        [string]$WorkingDirectory,
        [scriptblock]$Command,
        [string]$FailureMessage
    )

    Push-Location $WorkingDirectory
    try {
        & $Command
        if ($LASTEXITCODE -ne 0) {
            throw $FailureMessage
        }
    } finally {
        Pop-Location
    }
}

# Controller → Service → Mapper와 실제 Oracle 연결을 포함한 백엔드 테스트를 실행합니다.
Invoke-CheckedCommand -WorkingDirectory $backendRoot -Command { mvn clean test } -FailureMessage '백엔드 테스트에 실패했습니다.'
Invoke-CheckedCommand -WorkingDirectory $backendRoot -Command { mvn -DskipTests package } -FailureMessage '백엔드 패키징에 실패했습니다.'

$jarFile = Get-ChildItem -LiteralPath (Join-Path $backendRoot 'target') -Filter 'marinboy-v3-*.jar' -File |
        Where-Object { $_.Name -notlike '*.original' } |
        Select-Object -First 1
if ($null -eq $jarFile) {
    throw '검증할 Spring Boot JAR을 찾지 못했습니다.'
}

# JAR 내부에 로컬 설정이나 서비스 계정 키가 들어가면 즉시 실패시킵니다.
$unsafeEntries = & jar tf $jarFile.FullName |
        Where-Object { $_ -match 'application-local\.properties|service-account|\.p12$|\.pem$|\.key$' }
if ($unsafeEntries) {
    throw "JAR에 비밀설정 후보가 포함되어 있습니다: $($unsafeEntries -join ', ')"
}

# Redux/Saga 테스트, 린트, 프로덕션 빌드를 각각 확인합니다.
Invoke-CheckedCommand -WorkingDirectory $frontendRoot -Command { npm.cmd test } -FailureMessage '프론트엔드 테스트에 실패했습니다.'
Invoke-CheckedCommand -WorkingDirectory $frontendRoot -Command { npm.cmd run lint } -FailureMessage '프론트엔드 린트에 실패했습니다.'
Invoke-CheckedCommand -WorkingDirectory $frontendRoot -Command { npm.cmd run build } -FailureMessage '프론트엔드 빌드에 실패했습니다.'

if (-not $SkipRuntime) {
    $backendResponse = Invoke-WebRequest -Uri 'http://127.0.0.1:8082/api/services' -UseBasicParsing
    $frontendResponse = Invoke-WebRequest -Uri 'http://127.0.0.1:3000/' -UseBasicParsing
    $frontendApiResponse = Invoke-WebRequest -Uri 'http://127.0.0.1:3000/api/services' -UseBasicParsing

    if ($backendResponse.StatusCode -ne 200 -or
            $frontendResponse.StatusCode -ne 200 -or
            $frontendApiResponse.StatusCode -ne 200) {
        throw '백엔드·프론트엔드 HTTP 연결 검증에 실패했습니다.'
    }
    if (-not $frontendResponse.Content.Contains('웨이브 펌')) {
        throw 'SSR 화면에서 실제 서비스 데이터를 찾지 못했습니다.'
    }
}

Push-Location (Split-Path -Parent $projectRoot)
try {
    git diff --check
    if ($LASTEXITCODE -ne 0) {
        throw 'Git diff 공백·충돌 검증에 실패했습니다.'
    }
} finally {
    Pop-Location
}

Write-Host 'Marinboy v3 전체 검증을 통과했습니다.'
