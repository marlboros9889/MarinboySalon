param(
    [switch]$SkipTests
)

$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$projectEnvironmentFile = Join-Path $root '.env.local'
$failures = [System.Collections.Generic.List[string]]::new()

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

Import-EnvironmentFile -FilePath $projectEnvironmentFile
$userHome = [Environment]::GetFolderPath([Environment+SpecialFolder]::UserProfile)
$buildDirectory = if ($env:V2_VERIFY_BUILD_DIRECTORY) {
    [System.IO.Path]::GetFullPath($env:V2_VERIFY_BUILD_DIRECTORY)
} else {
    Join-Path $userHome '.marinboy\build\verify\v2'
}
New-Item -ItemType Directory -Path $buildDirectory -Force | Out-Null
$mavenBuildArgument = '-Dmarinboy.build.directory=' + $buildDirectory.Replace('\', '/')

Write-Host '[1/5] MyBatis XML validation'
Get-ChildItem (Join-Path $root 'src\main\resources\mybatis\mapper') -Filter '*.xml' | ForEach-Object {
    try {
        [xml](Get-Content -LiteralPath $_.FullName -Raw) | Out-Null
    } catch {
        $failures.Add("Invalid XML: $($_.Name) - $($_.Exception.Message)")
    }
}

Write-Host '[2/5] Reservation SQL validation'
$reservationMapper = Get-Content -LiteralPath (Join-Path $root 'src\main\resources\mybatis\mapper\salon-reservation-mapper.xml') -Raw
if ($reservationMapper -notmatch 'countOverlappingReservation') {
    $failures.Add('Missing overlapping reservation query.')
}
if ($reservationMapper -match '(?m)^\s*[<>]\s*#\{reservationDateTime\}') {
    $failures.Add('Unescaped XML comparison operator in reservation query.')
}

Write-Host '[3/5] DAO and admin API validation'
$reservationDao = Get-Content -LiteralPath (Join-Path $root 'src\main\java\com\marinboy\dao\SalonReservationDao.java') -Raw
if ($reservationDao -match 'countActiveReservationAt') {
    $failures.Add('Obsolete countActiveReservationAt DAO method remains.')
}
$adminController = Get-Content -LiteralPath (Join-Path $root 'src\main\java\com\marinboy\controller\AdminController.java') -Raw
if ($adminController -notmatch 'updateReservationStatus') {
    $failures.Add('Missing admin reservation status update API.')
}
$securityConfig = Get-Content -LiteralPath (Join-Path $root 'src\main\java\com\marinboy\security\SecurityConfig.java') -Raw
if ($securityConfig -notmatch 'requestMatchers\([^\)]*"/api/db/\*\*"[^\)]*\)\s*\.access\(') {
    $failures.Add('/api/db/** administrator restriction is missing.')
}

Write-Host '[4/5] v2 session boundary and production configuration validation'
$properties = Get-Content -LiteralPath (Join-Path $root 'src\main\resources\application.properties') -Raw
if ($properties -match '(?im)^logging\.level\.com\.marinboy\s*=\s*debug\s*$') {
    $failures.Add('com.marinboy DEBUG logging may expose personal data.')
}
if ($properties -notmatch 'server\.servlet\.session\.cookie\.name=MARINBOY_V2_SESSION') {
    $failures.Add('v2 전용 세션 쿠키 이름이 없습니다.')
}
if ($properties -match '(?im)^spring\.data\.redis\.' -or $properties -match '(?im)^app\.jwt\.') {
    $failures.Add('v2 설정에 Redis 또는 JWT 설정이 섞여 있습니다.')
}
if ($securityConfig -notmatch '\bHttpSession\b|\.getSession\s*\(') {
    $failures.Add('v2 SecurityConfig에 HttpSession 인증 경계가 없습니다.')
}
if ($securityConfig -match 'SessionCreationPolicy\.STATELESS|JwtAuthenticationFilter') {
    $failures.Add('v2 SecurityConfig에 v3 Stateless JWT 설정이 섞여 있습니다.')
}
$pom = Get-Content -LiteralPath (Join-Path $root 'pom.xml') -Raw
if ($pom -match 'spring-boot-starter-data-redis|jjwt-api|spring-session-data-redis') {
    $failures.Add('v2 의존성에 Redis, JWT 또는 Spring Session Redis가 섞여 있습니다.')
}
$oraclePassword = if ($env:V2_ORACLE_PASSWORD) { $env:V2_ORACLE_PASSWORD } else { $env:ORACLE_PASSWORD }
if (-not $oraclePassword -or $oraclePassword -eq 'change-me') {
    $failures.Add('v2 Oracle 실제 설정이 없습니다. scripts/setup-local.ps1로 .env.local을 준비하세요.')
}
$uploadService = Get-Content -LiteralPath (Join-Path $root 'src\main\java\com\marinboy\service\SalonServiceService.java') -Raw
if ($uploadService -match 'Paths\.get\("uploads"') {
    $failures.Add('v2 업로드 저장 경로가 실행 폴더 상대경로로 남아 있습니다.')
}

Write-Host '[5/5] Build and test'
Push-Location $root
try {
    if ($SkipTests) {
        & mvn.cmd $mavenBuildArgument -q -DskipTests compile
    } else {
        & mvn.cmd $mavenBuildArgument -q clean test
    }
    if ($LASTEXITCODE -ne 0) { $failures.Add('Maven build or test failed.') }
} finally {
    Pop-Location
}

if ($failures.Count -gt 0) {
    $failures | ForEach-Object { Write-Host "FAIL: $_" }
    exit 1
}

Write-Host 'Verification passed: no defects found.'
