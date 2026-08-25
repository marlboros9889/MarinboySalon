[CmdletBinding(SupportsShouldProcess)]
param(
    [switch]$IncludeDependencies
)

$ErrorActionPreference = "Stop"
$projectRoot = [System.IO.Path]::GetFullPath((Split-Path -Parent $PSScriptRoot))
$allowedRelativePaths = @(
    "MarinboySalon_v1\target",
    "MarinboySalon_v2\target",
    "MarinboySalon_v3\back\build",
    "MarinboySalon_v3\back\.gradle",
    "MarinboySalon_v3\front\.next",
    "MarinboySalon_v3\front\coverage"
)

if ($IncludeDependencies) {
    $allowedRelativePaths += "MarinboySalon_v3\front\node_modules"
}

foreach ($relativePath in $allowedRelativePaths) {
    $targetPath = [System.IO.Path]::GetFullPath((Join-Path $projectRoot $relativePath))
    $rootPrefix = $projectRoot.TrimEnd('\') + '\'

    # 계산된 경로가 저장소 밖을 가리키면 삭제를 즉시 중단합니다.
    if (-not $targetPath.StartsWith($rootPrefix, [System.StringComparison]::OrdinalIgnoreCase)) {
        throw "저장소 밖의 경로는 정리할 수 없습니다: $targetPath"
    }

    if ((Test-Path -LiteralPath $targetPath) -and $PSCmdlet.ShouldProcess($targetPath, "생성물 삭제")) {
        Remove-Item -LiteralPath $targetPath -Recurse -Force
    }
}

Write-Host "생성물 정리가 완료되었습니다. 소스와 복구 백업은 유지했습니다." -ForegroundColor Green
