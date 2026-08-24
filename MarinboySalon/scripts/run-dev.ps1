[CmdletBinding()]
param(
    [ValidateSet('Start', 'Stop', 'Restart', 'Status')]
    [string]$Action = 'Restart',
    [switch]$InstallDependencies,
    [switch]$StartDependencies,
    [switch]$Production
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
. (Join-Path $PSScriptRoot 'project-tools.ps1')

# 어느 폴더에서 실행해도 스크립트 위치를 기준으로 프로젝트 경로를 찾습니다.
$projectRoot = [System.IO.Path]::GetFullPath((Split-Path -Parent $PSScriptRoot))
$backendRoot = Join-Path $projectRoot 'backend'
$frontendRoot = Join-Path $projectRoot 'frontend'
$runtimeRoot = Join-Path $projectRoot '.runtime'
$environmentFile = Join-Path $projectRoot '.env.local'
$localPropertiesFile = Join-Path $projectRoot 'config\application-local.properties'

function Get-ListeningProcessIds {
    param([int]$Port)

    return @(Get-NetTCPConnection -State Listen -LocalPort $Port -ErrorAction SilentlyContinue |
            Select-Object -ExpandProperty OwningProcess -Unique)
}

function Get-SavedProcessIds {
    param([string]$PidFile)

    if (-not (Test-Path -LiteralPath $PidFile)) {
        return @()
    }
    return @(Get-Content -LiteralPath $PidFile | Where-Object { $_ -match '^\d+$' } | ForEach-Object { [int]$_ })
}

function Stop-ProjectPort {
    param(
        [int]$Port,
        [string]$PidFile
    )

    $savedProcessIds = Get-SavedProcessIds -PidFile $PidFile
    foreach ($processId in Get-ListeningProcessIds -Port $Port) {
        $processInfo = Get-CimInstance Win32_Process -Filter "ProcessId = $processId"
        $commandLine = [string]$processInfo.CommandLine
        $isSavedProcess = $savedProcessIds -contains $processId
        $isProjectPath = $commandLine.IndexOf($projectRoot, [System.StringComparison]::OrdinalIgnoreCase) -ge 0
        $isBackendJar = $Port -eq 8082 -and $commandLine.Contains('marinboy-salon-0.0.1-SNAPSHOT.jar')
        $isFrontendBuild = $Port -eq 3000 -and
                $commandLine.IndexOf($frontendBuildRoot, [System.StringComparison]::OrdinalIgnoreCase) -ge 0

        if (-not ($isSavedProcess -or $isProjectPath -or $isBackendJar -or $isFrontendBuild)) {
            throw "포트 $Port 프로세스는 Marinboy 소유임을 확인할 수 없어 종료하지 않았습니다. PID=$processId"
        }

        Stop-Process -Id $processId -Force
    }

    if (Test-Path -LiteralPath $PidFile) {
        Remove-Item -LiteralPath $PidFile -Force
    }
}

function Wait-ForPort {
    param(
        [int]$Port,
        [int]$TimeoutSeconds = 60
    )

    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    while ((Get-Date) -lt $deadline) {
        if (@(Get-ListeningProcessIds -Port $Port).Count -gt 0) {
            return
        }
        Start-Sleep -Milliseconds 500
    }
    throw "포트 $Port 서버가 ${TimeoutSeconds}초 안에 시작되지 않았습니다. .runtime 로그를 확인하세요."
}

function Show-Status {
    foreach ($port in 3000, 8082) {
        $owners = @(Get-ListeningProcessIds -Port $port)
        if ($owners.Count -eq 0) {
            Write-Host "포트 $port : 중지"
        } else {
            Write-Host "포트 $port : 실행 중 (PID $($owners -join ', '))"
        }
    }
}

Import-EnvironmentFile -FilePath $environmentFile
$userHome = [Environment]::GetFolderPath([Environment+SpecialFolder]::UserProfile)
$backendBuildRoot = if ($env:MARINBOY_BUILD_DIRECTORY) {
    [System.IO.Path]::GetFullPath($env:MARINBOY_BUILD_DIRECTORY)
} else {
    Join-Path $userHome '.marinboy-salon\build\backend'
}
$frontendBuildRoot = if ($env:MARINBOY_FRONTEND_BUILD_DIRECTORY) {
    [System.IO.Path]::GetFullPath($env:MARINBOY_FRONTEND_BUILD_DIRECTORY)
} else {
    Join-Path $userHome '.marinboy-salon\build\frontend'
}

function Wait-ForPortToStop {
    param(
        [int]$Port,
        [int]$TimeoutSeconds = 15
    )

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
New-Item -ItemType Directory -Path $runtimeRoot -Force | Out-Null

$backendPidFile = Join-Path $runtimeRoot 'backend.pid'
$frontendPidFile = Join-Path $runtimeRoot 'frontend.pid'

if ($Action -eq 'Status') {
    Show-Status
    exit 0
}

if ($Action -in @('Stop', 'Restart')) {
    # 우리가 시작한 Marinboy 포트만 종료해 다른 프로젝트 서버를 보호합니다.
    Stop-ProjectPort -Port 3000 -PidFile $frontendPidFile
    Stop-ProjectPort -Port 8082 -PidFile $backendPidFile
    Wait-ForPortToStop -Port 3000
    Wait-ForPortToStop -Port 8082
    if ($Action -eq 'Stop') {
        Show-Status
        exit 0
    }
}

if ($Action -in @('Start', 'Restart')) {
    if (@(Get-ListeningProcessIds -Port 8082).Count -gt 0 -or
            @(Get-ListeningProcessIds -Port 3000).Count -gt 0) {
        throw '3000 또는 8082 포트가 이미 사용 중입니다. 먼저 Status 또는 Stop을 실행하세요.'
    }

    $mavenCommand = Get-Command 'mvn.cmd' -ErrorAction Stop
    $npmCommand = Get-Command 'npm.cmd' -ErrorAction Stop
    $javaCommand = Get-Command 'java.exe' -ErrorAction Stop
    New-Item -ItemType Directory -Path $backendBuildRoot -Force | Out-Null
    $mavenBuildArgument = '-Dmarinboy.build.directory=' + $backendBuildRoot.Replace('\', '/')

    # 외부 설정 파일을 사용할 때만 Spring에 절대 경로를 전달합니다.
    if ($env:MARINBOY_CONFIG_FILE) {
        $resolvedConfigFile = [System.IO.Path]::GetFullPath($env:MARINBOY_CONFIG_FILE)
        $env:SPRING_CONFIG_ADDITIONAL_LOCATION = 'optional:file:' + $resolvedConfigFile.Replace('\', '/')
    } elseif (Test-Path -LiteralPath $localPropertiesFile) {
        $resolvedConfigFile = [System.IO.Path]::GetFullPath($localPropertiesFile)
        $env:SPRING_CONFIG_ADDITIONAL_LOCATION = 'optional:file:' + $resolvedConfigFile.Replace('\', '/')
    }

    $hasExternalConfig = [bool]$env:SPRING_CONFIG_ADDITIONAL_LOCATION
    $hasOracleEnvironment = [bool]$env:ORACLE_URL -and [bool]$env:ORACLE_USERNAME -and
            [bool]$env:ORACLE_PASSWORD -and $env:ORACLE_PASSWORD -ne 'change-me'
    if (-not ($hasExternalConfig -or $hasOracleEnvironment)) {
        throw 'Oracle 설정이 없습니다. setup-local.ps1 실행 후 .env.local의 ORACLE_* 실제 값을 입력하세요.'
    }

    # 현재 프로젝트는 JWT와 Redis가 모두 있어야 인증이 성립합니다.
    if (-not $env:JWT_SECRET -or $env:JWT_SECRET -eq 'replace-with-base64-encoded-32-byte-secret') {
        throw 'JWT_SECRET이 없습니다. 32바이트 이상 값을 Base64로 인코딩해 입력하세요.'
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
    if (-not (Test-TcpPort -ComputerName $redisHost -Port $redisPort) -and $StartDependencies) {
        $localRedis = $redisHost -in @('127.0.0.1', 'localhost') -and $redisPort -eq 6379
        if (-not $localRedis) {
            throw '-StartDependencies는 저장소의 127.0.0.1:6379 Redis만 시작할 수 있습니다.'
        }
        $dockerCommand = Get-Command 'docker.exe' -ErrorAction SilentlyContinue
        if ($null -eq $dockerCommand) {
            throw 'Docker를 찾을 수 없습니다. Docker Desktop을 설치하거나 Redis를 직접 실행하세요.'
        }
        # 새 PC에서는 저장소의 compose 파일로 JWT 로그아웃용 Redis를 먼저 준비합니다.
        & $dockerCommand.Source compose -f (Join-Path $projectRoot 'compose.yaml') up -d redis
        if ($LASTEXITCODE -ne 0) {
            throw 'Redis 컨테이너 시작에 실패했습니다.'
        }
        $redisDeadline = (Get-Date).AddSeconds(60)
        while ((Get-Date) -lt $redisDeadline -and -not (Test-TcpPort -ComputerName $redisHost -Port $redisPort)) {
            Start-Sleep -Milliseconds 500
        }
    }
    if (-not (Test-TcpPort -ComputerName $redisHost -Port $redisPort)) {
        throw "Redis에 연결할 수 없습니다: ${redisHost}:${redisPort}. 직접 실행하거나 -StartDependencies를 사용하세요."
    }

    if ($InstallDependencies -or -not (Test-Path -LiteralPath (Join-Path $frontendRoot 'node_modules'))) {
        Push-Location $frontendRoot
        try {
            & $npmCommand.Source ci
            if ($LASTEXITCODE -ne 0) {
                throw '프론트엔드 npm ci에 실패했습니다.'
            }
        } finally {
            Pop-Location
        }
    }

    # 한글이 포함된 Windows 경로에서 Maven의 개발용 클래스패스가 깨지는 문제를 피하도록 JAR로 실행합니다.
    Push-Location $backendRoot
    try {
        & $mavenCommand.Source $mavenBuildArgument clean -DskipTests package
        if ($LASTEXITCODE -ne 0) {
            throw '백엔드 JAR 패키징에 실패했습니다.'
        }
    } finally {
        Pop-Location
    }

    $backendJar = Join-Path $backendBuildRoot 'marinboy-salon-0.0.1-SNAPSHOT.jar'
    if (-not (Test-Path -LiteralPath $backendJar)) {
        throw '실행할 백엔드 JAR을 찾지 못했습니다.'
    }

    $backendProcess = Start-Process -FilePath $javaCommand.Source -ArgumentList '-jar', ('"' + $backendJar + '"') -WorkingDirectory $backendRoot -RedirectStandardOutput (Join-Path $runtimeRoot 'backend.out.log') -RedirectStandardError (Join-Path $runtimeRoot 'backend.err.log') -WindowStyle Hidden -PassThru
    Set-Content -LiteralPath $backendPidFile -Value $backendProcess.Id
    Wait-ForPort -Port 8082

    $frontendArguments = @('run', 'dev')
    $frontendRuntimeRoot = $frontendRoot
    if ($Production) {
        # OneDrive가 .next를 잠그지 않도록 사용자 홈의 빌드 전용 복사본에서 production을 실행합니다.
        $frontendRuntimeRoot = & (Join-Path $PSScriptRoot 'prepare-frontend-workspace.ps1') `
                -SourceRoot $frontendRoot -BuildRoot $frontendBuildRoot
        Push-Location $frontendRuntimeRoot
        try {
            & $npmCommand.Source run build
            if ($LASTEXITCODE -ne 0) {
                throw '프론트엔드 production 빌드에 실패했습니다.'
            }
        } finally {
            Pop-Location
        }
        $frontendArguments = @('run', 'start')
    }

    $frontendProcess = Start-Process -FilePath $npmCommand.Source -ArgumentList $frontendArguments -WorkingDirectory $frontendRuntimeRoot -RedirectStandardOutput (Join-Path $runtimeRoot 'frontend.out.log') -RedirectStandardError (Join-Path $runtimeRoot 'frontend.err.log') -WindowStyle Hidden -PassThru
    Set-Content -LiteralPath $frontendPidFile -Value $frontendProcess.Id
    Wait-ForPort -Port 3000

    $backendResponse = Invoke-WebRequest -Uri 'http://127.0.0.1:8082/api/services' -UseBasicParsing
    $frontendResponse = Invoke-WebRequest -Uri 'http://127.0.0.1:3000/' -UseBasicParsing
    if ($backendResponse.StatusCode -ne 200 -or $frontendResponse.StatusCode -ne 200) {
        throw '서버는 실행됐지만 HTTP 기능 점검에 실패했습니다.'
    }

    Show-Status
}
