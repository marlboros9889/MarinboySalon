[CmdletBinding(SupportsShouldProcess = $true)]
param(
    [switch]$IncludeDependencies
)

# 프로젝트 소스는 보존하고, 실행 과정에서 다시 만들어지는 파일만 정리합니다.
$projectRoot = Split-Path -Parent $PSScriptRoot
$projectRootPath = [System.IO.Path]::GetFullPath($projectRoot)
$projectRootPrefix = $projectRootPath.TrimEnd('\') + '\'

$generatedTargets = @(
    '.metadata',
    '.playwright-cli',
    'bin',
    'src',
    'target',
    'backend\.settings',
    'backend\target',
    'frontend\.next',
    'frontend\.playwright-cli',
    'frontend\dist',
    'frontend\logs',
    'frontend\src'
)

# 의존성 폴더는 다시 npm install 하면 복구되므로, 필요할 때만 함께 삭제합니다.
if ($IncludeDependencies) {
    $generatedTargets += 'frontend\node_modules'
}

foreach ($relativePath in $generatedTargets) {
    $targetPath = Join-Path $projectRootPath $relativePath

    if (-not (Test-Path -LiteralPath $targetPath)) {
        continue
    }

    $resolvedPath = [System.IO.Path]::GetFullPath($targetPath)
    $isInsideProject = $resolvedPath.StartsWith($projectRootPrefix, [System.StringComparison]::OrdinalIgnoreCase)

    if (-not $isInsideProject) {
        throw "프로젝트 밖의 경로는 삭제할 수 없습니다: $resolvedPath"
    }

    if ($PSCmdlet.ShouldProcess($resolvedPath, '재생성 파일 삭제')) {
        Remove-Item -LiteralPath $resolvedPath -Recurse -Force
    }
}
