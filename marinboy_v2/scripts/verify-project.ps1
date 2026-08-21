param(
    [switch]$SkipTests
)

$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$failures = [System.Collections.Generic.List[string]]::new()

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
$uploadService = Get-Content -LiteralPath (Join-Path $root 'src\main\java\com\marinboy\service\SalonServiceService.java') -Raw
if ($uploadService -match 'Paths\.get\("uploads"') {
    $failures.Add('v2 업로드 저장 경로가 실행 폴더 상대경로로 남아 있습니다.')
}

Write-Host '[5/5] Build and test'
Push-Location $root
try {
    if ($SkipTests) { mvn -q -DskipTests compile } else { mvn -q test }
    if ($LASTEXITCODE -ne 0) { $failures.Add('Maven build or test failed.') }
} finally {
    Pop-Location
}

if ($failures.Count -gt 0) {
    $failures | ForEach-Object { Write-Host "FAIL: $_" }
    exit 1
}

Write-Host 'Verification passed: no defects found.'
