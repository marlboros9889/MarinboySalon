[CmdletBinding()]
param([switch]$Force)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

$projectRoot = [System.IO.Path]::GetFullPath((Split-Path -Parent $PSScriptRoot))
$exampleFile = Join-Path $projectRoot '.env.example'
$environmentFile = Join-Path $projectRoot '.env.local'

if (-not (Test-Path -LiteralPath $exampleFile -PathType Leaf)) {
    throw '.env.example 파일을 찾을 수 없습니다.'
}
if ((Test-Path -LiteralPath $environmentFile) -and -not $Force) {
    throw '.env.local이 이미 있습니다. 기존 설정을 보호하기 위해 중단했습니다. 다시 만들려면 -Force를 명시하세요.'
}

# v2는 JWT나 Redis 없이 Oracle과 HttpSession 설정만 별도 파일로 준비합니다.
$environmentContent = Get-Content -LiteralPath $exampleFile -Raw
Set-Content -LiteralPath $environmentFile -Value $environmentContent -Encoding utf8

Write-Host '.env.local 생성 완료: V2_ORACLE_* 실제 값을 입력하세요.'
Write-Host 'v2에는 JWT_SECRET과 REDIS_* 값을 추가하지 않습니다.'
