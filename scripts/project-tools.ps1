Set-StrictMode -Version Latest

function Import-EnvironmentFile {
    param([string]$FilePath)

    if (-not (Test-Path -LiteralPath $FilePath)) {
        return
    }

    # 단순 KEY=VALUE만 읽고 기존 프로세스 환경변수와 비밀 값은 덮어쓰거나 출력하지 않습니다.
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

        $existingValue = [Environment]::GetEnvironmentVariable($key, 'Process')
        if ($value.Length -gt 0 -and [string]::IsNullOrWhiteSpace($existingValue)) {
            [Environment]::SetEnvironmentVariable($key, $value, 'Process')
        }
    }
}

function Test-TcpPort {
    param([string]$ComputerName, [int]$Port)

    $client = [System.Net.Sockets.TcpClient]::new()
    try {
        $connectTask = $client.ConnectAsync($ComputerName, $Port)
        if (-not $connectTask.Wait(2000)) {
            return $false
        }
        return $client.Connected
    } catch {
        return $false
    } finally {
        $client.Dispose()
    }
}
