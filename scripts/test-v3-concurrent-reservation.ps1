[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [string]$Email,
    [Parameter(Mandatory = $true)]
    [string]$Password,
    [Parameter(Mandatory = $true)]
    [long]$ServiceId,
    [Parameter(Mandatory = $true)]
    [datetime]$ReservationStart,
    [string]$ApiUrl = "http://localhost:8082"
)

$ErrorActionPreference = "Stop"
$loginBody = @{ email = $Email; password = $Password } | ConvertTo-Json
$loginResponse = Invoke-RestMethod -Method Post -Uri "$ApiUrl/auth/login" `
    -ContentType "application/json" -Body $loginBody -SessionVariable loginSession
$accessToken = $loginResponse.accessToken

if ([string]::IsNullOrWhiteSpace($accessToken)) {
    throw "로그인 응답에 Access Token이 없습니다."
}

$requestBody = @{
    serviceId = $ServiceId
    reservationStart = $ReservationStart.ToString("yyyy-MM-ddTHH:mm:ss")
    requestMemo = "동시 예약 검증"
} | ConvertTo-Json
$headers = @{ Authorization = "Bearer $accessToken" }

# 같은 시간의 요청 두 개를 별도 작업으로 동시에 시작합니다.
$jobs = 1..2 | ForEach-Object {
    Start-Job -ScriptBlock {
        param($TargetUrl, $TargetHeaders, $TargetBody)
        try {
            $response = Invoke-WebRequest -Method Post -Uri "$TargetUrl/api/reservations" `
                -Headers $TargetHeaders -ContentType "application/json" -Body $TargetBody
            [pscustomobject]@{ StatusCode = [int]$response.StatusCode; Body = $response.Content }
        } catch {
            $statusCode = 0
            if ($_.Exception.Response) {
                $statusCode = [int]$_.Exception.Response.StatusCode
            }
            [pscustomobject]@{ StatusCode = $statusCode; Body = $_.ErrorDetails.Message }
        }
    } -ArgumentList $ApiUrl, $headers, $requestBody
}

$results = $jobs | Wait-Job | Receive-Job
$jobs | Remove-Job
$results | Format-Table -AutoSize

$successCount = @($results | Where-Object { $_.StatusCode -ge 200 -and $_.StatusCode -lt 300 }).Count
if ($successCount -ne 1) {
    throw "동시 요청 2건 중 정확히 1건만 성공해야 합니다. 실제 성공: $successCount"
}

Write-Host "동시성 검증 통과: 동일 시간 요청 2건 중 1건만 저장되었습니다." -ForegroundColor Green
