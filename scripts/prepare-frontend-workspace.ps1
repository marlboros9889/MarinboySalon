[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [string]$SourceRoot,
    [Parameter(Mandatory = $true)]
    [string]$BuildRoot
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

$resolvedSourceRoot = [System.IO.Path]::GetFullPath($SourceRoot)
$resolvedBuildRoot = [System.IO.Path]::GetFullPath($BuildRoot)
$userProfilePath = [Environment]::GetFolderPath([Environment+SpecialFolder]::UserProfile)
$allowedBuildRoot = [System.IO.Path]::GetFullPath((Join-Path $userProfilePath '.marinboy-salon\build'))
$allowedPrefix = $allowedBuildRoot.TrimEnd('\') + '\'

# 생성 폴더를 사용자 전용 .marinboy-salon/build 아래로 제한해 잘못된 재귀 삭제를 막습니다.
if (-not $resolvedBuildRoot.StartsWith($allowedPrefix, [System.StringComparison]::OrdinalIgnoreCase)) {
    throw "프론트 빌드 폴더는 $allowedBuildRoot 아래여야 합니다."
}
if (-not (Test-Path -LiteralPath (Join-Path $resolvedSourceRoot 'package.json') -PathType Leaf)) {
    throw '프론트 소스 폴더에서 package.json을 찾을 수 없습니다.'
}

New-Item -ItemType Directory -Path $resolvedBuildRoot -Force | Out-Null

# 이전 source와 .next만 비우고 큰 node_modules 복사본은 다음 빌드에서 재사용합니다.
foreach ($generatedName in '.next', 'features', 'pages', 'public', 'styles') {
    $generatedPath = Join-Path $resolvedBuildRoot $generatedName
    if (Test-Path -LiteralPath $generatedPath) {
        Remove-Item -LiteralPath $generatedPath -Recurse -Force
    }
}

# Next 빌드에 필요한 소스만 사용자 홈의 독립된 production 작업공간으로 복사합니다.
foreach ($directoryName in 'features', 'pages', 'public', 'styles') {
    $sourceDirectory = Join-Path $resolvedSourceRoot $directoryName
    if (Test-Path -LiteralPath $sourceDirectory -PathType Container) {
        Copy-Item -LiteralPath $sourceDirectory -Destination $resolvedBuildRoot -Recurse -Force
    }
}
foreach ($fileName in 'next.config.js', 'package.json', 'package-lock.json') {
    Copy-Item -LiteralPath (Join-Path $resolvedSourceRoot $fileName) -Destination $resolvedBuildRoot -Force
}

$sourceNodeModules = Join-Path $resolvedSourceRoot 'node_modules'
if (-not (Test-Path -LiteralPath $sourceNodeModules -PathType Container)) {
    throw 'frontend/node_modules가 없습니다. 먼저 npm ci를 실행해 주세요.'
}
$targetNodeModules = Join-Path $resolvedBuildRoot 'node_modules'
$targetNodeModulesItem = Get-Item -LiteralPath $targetNodeModules -Force -ErrorAction SilentlyContinue
if ($null -ne $targetNodeModulesItem -and
        ($targetNodeModulesItem.Attributes -band [System.IO.FileAttributes]::ReparsePoint)) {
    # 이전 버전이 만든 Junction만 제거하며 원본 frontend/node_modules는 건드리지 않습니다.
    Remove-Item -LiteralPath $targetNodeModules -Force
}
$robocopyCommand = Get-Command 'robocopy.exe' -ErrorAction Stop
# Turbopack은 프로젝트 밖 Junction을 거부하므로 실제 파일을 증분 동기화합니다.
& $robocopyCommand.Source $sourceNodeModules $targetNodeModules /MIR /COPY:DAT /DCOPY:DAT /NFL /NDL /NJH /NJS /NP | Out-Null
if ($LASTEXITCODE -ge 8) {
    throw "node_modules 복사에 실패했습니다. robocopy exit=$LASTEXITCODE"
}

Write-Output $resolvedBuildRoot
