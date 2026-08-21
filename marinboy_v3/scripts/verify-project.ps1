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
        # CI나 다른 PC에서 미리 지정한 환경변수를 .env.local이 덮어쓰지 않게 합니다.
        $existingValue = [Environment]::GetEnvironmentVariable($key, 'Process')
        if ($value.Length -gt 0 -and [string]::IsNullOrWhiteSpace($existingValue)) {
            [Environment]::SetEnvironmentVariable($key, $value, 'Process')
        }
    }
}

function Test-TcpPort {
    param([string]$ComputerName, [int]$Port)

    $client = [System.Net.Sockets.TcpClient]::new()
    try {
        $connectTask = $client.ConnectAsync($ComputerName, $Port)
        if (-not $connectTask.Wait(2000)) {
            return $false
        }
        return $client.Connected
    } catch {
        return $false
    } finally {
        $client.Dispose()
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

$userHome = [Environment]::GetFolderPath([Environment+SpecialFolder]::UserProfile)
$backendBuildRoot = if ($env:MARINBOY_VERIFY_BUILD_DIRECTORY) {
    [System.IO.Path]::GetFullPath($env:MARINBOY_VERIFY_BUILD_DIRECTORY)
} else {
    Join-Path $userHome '.marinboy\build\verify\v3-backend'
}
$frontendBuildRoot = Join-Path $userHome '.marinboy\build\verify\v3-frontend'
New-Item -ItemType Directory -Path $backendBuildRoot -Force | Out-Null
$mavenBuildArgument = '-Dmarinboy.build.directory=' + $backendBuildRoot.Replace('\', '/')

# v3는 HttpSession/CSRF 보조 엔드포인트 없이 JWT와 Redis만 사용해야 합니다.
$javaSourceFiles = Get-ChildItem -LiteralPath (Join-Path $backendRoot 'src\main\java') -Recurse -Filter '*.java' -File
$javaSource = ($javaSourceFiles | ForEach-Object { Get-Content -LiteralPath $_.FullName -Raw }) -join "`n"
if ($javaSource -match '\bHttpSession\b|\.getSession\s*\(') {
    throw 'v3 Java 소스에 HttpSession 기반 인증 코드가 남아 있습니다.'
}
if (Test-Path -LiteralPath (Join-Path $backendRoot 'src\main\java\com\marinboy\controller\CsrfController.java')) {
    throw 'CSRF를 비활성화한 v3에 사용하지 않는 CsrfController가 남아 있습니다.'
}

$securityConfig = Get-Content -LiteralPath (Join-Path $backendRoot 'src\main\java\com\marinboy\security\SecurityConfig.java') -Raw
if ($securityConfig -notmatch 'SessionCreationPolicy\.STATELESS' -or
        $securityConfig -notmatch 'JwtAuthenticationFilter') {
    throw 'v3 SecurityConfig의 Stateless JWT 설정이 불완전합니다.'
}

$backendPom = Get-Content -LiteralPath (Join-Path $backendRoot 'pom.xml') -Raw
if ($backendPom -notmatch 'spring-boot-starter-data-redis' -or $backendPom -notmatch 'jjwt-api') {
    throw 'v3 JWT 또는 Redis 의존성이 누락되었습니다.'
}

$mobilePatterns = 'MB_DEVICE_TOKEN|MobileAdminController|MobilePushService|DeviceTokenMapper|FirebaseConfig|firebase-admin'
$runtimeSourceFiles = Get-ChildItem -LiteralPath (Join-Path $backendRoot 'src\main') -Recurse -File |
        Where-Object { $_.Extension -in @('.java', '.xml', '.sql', '.properties') }
$mobileMatches = $runtimeSourceFiles | Select-String -Pattern $mobilePatterns
if ($mobileMatches) {
    throw "모바일 제외 범위의 코드 또는 DB 항목이 남아 있습니다: $($mobileMatches[0].Path)"
}

if (-not $env:JWT_SECRET -or $env:JWT_SECRET -eq 'replace-with-base64-encoded-32-byte-secret') {
    throw 'JWT_SECRET이 없습니다. scripts/setup-local.ps1로 .env.local을 준비하세요.'
}
try {
    $decodedJwtSecret = [Convert]::FromBase64String($env:JWT_SECRET)
} catch {
    throw 'JWT_SECRET은 올바른 Base64 문자열이어야 합니다.'
}
if ($decodedJwtSecret.Length -lt 32) {
    throw 'JWT_SECRET을 디코딩한 길이는 32바이트 이상이어야 합니다.'
}

$redisHost = if ($env:REDIS_HOST) { $env:REDIS_HOST } else { '127.0.0.1' }
$redisPort = if ($env:REDIS_PORT) { [int]$env:REDIS_PORT } else { 6379 }
if (-not (Test-TcpPort -ComputerName $redisHost -Port $redisPort)) {
    throw "Redis에 연결할 수 없습니다: ${redisHost}:${redisPort}"
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
Invoke-CheckedCommand -WorkingDirectory $backendRoot -Command { & mvn.cmd $mavenBuildArgument clean test } -FailureMessage '백엔드 테스트에 실패했습니다.'
Invoke-CheckedCommand -WorkingDirectory $backendRoot -Command { & mvn.cmd $mavenBuildArgument -DskipTests package } -FailureMessage '백엔드 패키징에 실패했습니다.'

$jarFile = Get-ChildItem -LiteralPath $backendBuildRoot -Filter 'marinboy-v3-*.jar' -File |
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
$frontendVerifyRoot = & (Join-Path $PSScriptRoot 'prepare-frontend-workspace.ps1') `
        -SourceRoot $frontendRoot -BuildRoot $frontendBuildRoot
Invoke-CheckedCommand -WorkingDirectory $frontendVerifyRoot -Command { npm.cmd run build } -FailureMessage '프론트엔드 빌드에 실패했습니다.'

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
