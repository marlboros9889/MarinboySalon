[CmdletBinding()]
param(
    [switch]$Force,
    [switch]$StartRedis
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

$projectRoot = [System.IO.Path]::GetFullPath((Split-Path -Parent $PSScriptRoot))
$exampleFile = Join-Path $projectRoot '.env.example'
$environmentFile = Join-Path $projectRoot '.env.local'
$composeFile = Join-Path $projectRoot 'compose.yaml'
$credentialDirectory = Join-Path ([Environment]::GetFolderPath([Environment+SpecialFolder]::UserProfile)) '.marinboy\credentials'
$credentialFile = Join-Path $credentialDirectory 'google-calendar-service-account.json'

if (-not (Test-Path -LiteralPath $exampleFile -PathType Leaf)) {
    throw '.env.example 파일을 찾을 수 없습니다.'
}
if ((Test-Path -LiteralPath $environmentFile) -and -not $Force) {
    throw '.env.local이 이미 있습니다. 기존 설정을 보호하기 위해 중단했습니다. 다시 만들려면 -Force를 명시하세요.'
}

# 다른 PC에서도 안전한 JWT 서명키를 바로 사용할 수 있도록 32바이트 난수를 생성합니다.
$secretBytes = New-Object byte[] 32
[Security.Cryptography.RandomNumberGenerator]::Fill($secretBytes)
$jwtSecret = [Convert]::ToBase64String($secretBytes)

$environmentContent = Get-Content -LiteralPath $exampleFile -Raw
$environmentContent = $environmentContent.Replace('replace-with-base64-encoded-32-byte-secret', $jwtSecret)
$portableCredentialPath = $credentialFile.Replace('\', '/')
$environmentContent = $environmentContent.Replace(
        'GOOGLE_CALENDAR_CREDENTIALS_PATH=',
        "GOOGLE_CALENDAR_CREDENTIALS_PATH=$portableCredentialPath")

New-Item -ItemType Directory -Path $credentialDirectory -Force | Out-Null
Set-Content -LiteralPath $environmentFile -Value $environmentContent -Encoding utf8

if ($StartRedis) {
    $dockerCommand = Get-Command 'docker.exe' -ErrorAction SilentlyContinue
    if ($null -eq $dockerCommand) {
        throw 'Docker를 찾을 수 없습니다. Docker Desktop을 설치하거나 Redis를 직접 실행하세요.'
    }
    # 저장소의 compose 설정으로 v3 JWT 로그아웃용 Redis만 시작합니다.
    & $dockerCommand.Source compose -f $composeFile up -d redis
    if ($LASTEXITCODE -ne 0) {
        throw 'Redis 컨테이너 시작에 실패했습니다.'
    }
}

Write-Host '.env.local 생성 완료: JWT_SECRET은 자동 생성했으며 화면에 출력하지 않았습니다.'
Write-Host '다음 단계: .env.local의 ORACLE_* 값을 입력한 뒤 run-dev.ps1을 실행하세요.'
Write-Host "Calendar 키 권장 위치: $credentialFile"
