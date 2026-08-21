[CmdletBinding()]
param(
    [ValidateSet('Start', 'Stop', 'Restart', 'Status')]
    [string]$Action = 'Restart'
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

$projectRoot = [System.IO.Path]::GetFullPath((Split-Path -Parent $PSScriptRoot))
$runtimeRoot = Join-Path $projectRoot '.runtime'
$environmentFile = Join-Path $projectRoot '.env.local'
$pidFile = Join-Path $runtimeRoot 'server.pid'

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

function Get-ListeningProcessIds {
    param([int]$Port)
    return @(Get-NetTCPConnection -State Listen -LocalPort $Port -ErrorAction SilentlyContinue |
            Select-Object -ExpandProperty OwningProcess -Unique)
}

function Wait-ForPort {
    param([int]$Port, [int]$TimeoutSeconds = 60)

    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    while ((Get-Date) -lt $deadline) {
        if (@(Get-ListeningProcessIds -Port $Port).Count -gt 0) {
            return
        }
        Start-Sleep -Milliseconds 500
    }
    throw "포트 $Port 서버가 ${TimeoutSeconds}초 안에 시작되지 않았습니다. .runtime 로그를 확인하세요."
}

function Wait-ForPortToStop {
    param([int]$Port, [int]$TimeoutSeconds = 15)

    # Windows가 종료된 프로세스의 포트를 해제할 때까지 기다려 재시작 충돌을 막습니다.
    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    while ((Get-Date) -lt $deadline) {
        if (@(Get-ListeningProcessIds -Port $Port).Count -eq 0) {
            return
        }
        Start-Sleep -Milliseconds 300
    }
    throw "포트 $Port 프로세스가 ${TimeoutSeconds}초 안에 종료되지 않았습니다."
}

function Stop-ProjectServer {
    param([int]$Port)

    $savedProcessIds = @()
    if (Test-Path -LiteralPath $pidFile) {
        $savedProcessIds = @(Get-Content -LiteralPath $pidFile |
                Where-Object { $_ -match '^\d+$' } | ForEach-Object { [int]$_ })
    }
    foreach ($processId in Get-ListeningProcessIds -Port $Port) {
        $processInfo = Get-CimInstance Win32_Process -Filter "ProcessId = $processId"
        $commandLine = [string]$processInfo.CommandLine
        $isProjectProcess = $savedProcessIds -contains $processId -or
                $commandLine.IndexOf($projectRoot, [StringComparison]::OrdinalIgnoreCase) -ge 0 -or
                $commandLine.Contains('marinboy-v2-0.0.1-SNAPSHOT.jar')
        if (-not $isProjectProcess) {
            throw "포트 $Port 프로세스는 Marinboy v2 소유임을 확인할 수 없어 종료하지 않았습니다. PID=$processId"
        }
        Stop-Process -Id $processId -Force
    }
    if (Test-Path -LiteralPath $pidFile) {
        Remove-Item -LiteralPath $pidFile -Force
    }
}

function Show-Status {
    param([int]$Port)
    $owners = @(Get-ListeningProcessIds -Port $Port)
    if ($owners.Count -eq 0) {
        Write-Host "v2 포트 $Port : 중지"
    } else {
        Write-Host "v2 포트 $Port : 실행 중 (PID $($owners -join ', '))"
    }
}

Import-EnvironmentFile -FilePath $environmentFile
New-Item -ItemType Directory -Path $runtimeRoot -Force | Out-Null
$serverPort = if ($env:V2_SERVER_PORT) { [int]$env:V2_SERVER_PORT } else { 8081 }

if ($Action -eq 'Status') {
    Show-Status -Port $serverPort
    exit 0
}
if ($Action -in @('Stop', 'Restart')) {
    Stop-ProjectServer -Port $serverPort
    Wait-ForPortToStop -Port $serverPort
    if ($Action -eq 'Stop') {
        Show-Status -Port $serverPort
        exit 0
    }
}

if (@(Get-ListeningProcessIds -Port $serverPort).Count -gt 0) {
    throw "포트 $serverPort가 이미 사용 중입니다. 먼저 Status 또는 Stop을 실행하세요."
}

$oracleUrl = if ($env:V2_ORACLE_URL) { $env:V2_ORACLE_URL } else { $env:ORACLE_URL }
$oracleUsername = if ($env:V2_ORACLE_USERNAME) { $env:V2_ORACLE_USERNAME } else { $env:ORACLE_USERNAME }
$oraclePassword = if ($env:V2_ORACLE_PASSWORD) { $env:V2_ORACLE_PASSWORD } else { $env:ORACLE_PASSWORD }
if (-not $oracleUrl -or -not $oracleUsername -or -not $oraclePassword -or $oraclePassword -eq 'change-me') {
    throw 'v2 Oracle 설정이 없습니다. setup-local.ps1 실행 후 .env.local의 V2_ORACLE_* 실제 값을 입력하세요.'
}

$mavenCommand = Get-Command 'mvn.cmd' -ErrorAction Stop
$javaCommand = Get-Command 'java.exe' -ErrorAction Stop
$userHome = [Environment]::GetFolderPath([Environment+SpecialFolder]::UserProfile)
$buildDirectory = if ($env:V2_BUILD_DIRECTORY) {
    [System.IO.Path]::GetFullPath($env:V2_BUILD_DIRECTORY)
} else {
    Join-Path $userHome '.marinboy\build\v2'
}
New-Item -ItemType Directory -Path $buildDirectory -Force | Out-Null
$mavenBuildArgument = '-Dmarinboy.build.directory=' + $buildDirectory.Replace('\', '/')

# 한글 Windows 경로에서도 클래스패스가 흔들리지 않도록 clean JAR로 실행합니다.
Push-Location $projectRoot
try {
    & $mavenCommand.Source $mavenBuildArgument clean -DskipTests package
    if ($LASTEXITCODE -ne 0) {
        throw 'v2 백엔드 JAR 패키징에 실패했습니다.'
    }
} finally {
    Pop-Location
}

$serverJar = Join-Path $buildDirectory 'marinboy-v2-0.0.1-SNAPSHOT.jar'
if (-not (Test-Path -LiteralPath $serverJar -PathType Leaf)) {
    throw '실행할 v2 JAR을 찾지 못했습니다.'
}
$processOptions = @{
    FilePath = $javaCommand.Source
    ArgumentList = @('-jar', ('"' + $serverJar + '"'))
    WorkingDirectory = $projectRoot
    RedirectStandardOutput = Join-Path $runtimeRoot 'server.out.log'
    RedirectStandardError = Join-Path $runtimeRoot 'server.err.log'
    WindowStyle = 'Hidden'
    PassThru = $true
}
$serverProcess = Start-Process @processOptions
Set-Content -LiteralPath $pidFile -Value $serverProcess.Id
Wait-ForPort -Port $serverPort

$homeResponse = Invoke-WebRequest -Uri "http://127.0.0.1:$serverPort/" -UseBasicParsing
if ($homeResponse.StatusCode -ne 200) {
    throw 'v2 서버는 실행됐지만 고객 화면 HTTP 점검에 실패했습니다.'
}
Show-Status -Port $serverPort
