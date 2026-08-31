[CmdletBinding(SupportsShouldProcess)]
param(
    [switch]$IncludeDependencies
)

$ErrorActionPreference = "Stop"
$projectRoot = [System.IO.Path]::GetFullPath((Split-Path -Parent $PSScriptRoot))
$generatedPaths = @(
    "MarinboySalon_v1\target",
    "MarinboySalon_v2\target",
    "MarinboySalon_v3\back\build",
    "MarinboySalon_v3\back\.gradle",
    "MarinboySalon_v3\front\.next"
)

if ($IncludeDependencies) {
    $generatedPaths += "MarinboySalon_v3\front\node_modules"
}

foreach ($relativePath in $generatedPaths) {
    $targetPath = [System.IO.Path]::GetFullPath((Join-Path $projectRoot $relativePath))
    $expectedPrefix = $projectRoot.TrimEnd('\') + '\'

    # 저장소 밖의 경로는 어떤 경우에도 지우지 않습니다.
    if (-not $targetPath.StartsWith($expectedPrefix, [System.StringComparison]::OrdinalIgnoreCase)) {
        throw "저장소 밖의 경로는 삭제할 수 없습니다: $targetPath"
    }

    if (Test-Path -LiteralPath $targetPath) {
        if ($PSCmdlet.ShouldProcess($targetPath, "생성 결과 폴더 삭제")) {
            Remove-Item -LiteralPath $targetPath -Recurse -Force
        }
    }
}
