$ErrorActionPreference = 'Stop'
$body = '{"email":"robertaobolivia@gmail.com","senha":"@Hipcom123789"}'
$login = Invoke-RestMethod -Uri 'http://localhost:8080/api/analistas/login' -Method Post -Body $body -ContentType 'application/json; charset=utf-8'
$h = @{ 'X-Analista-Id' = [string]$login.id; 'X-Analista-Token' = [string]$login.authToken }
$tickets = Invoke-RestMethod -Uri 'http://localhost:8080/api/tickets?status=ALL' -Headers $h
Write-Output "TICKETS_ALL count=$($tickets.Count)"
$tickets | Group-Object status | ForEach-Object { Write-Output "  status $($_.Name) = $($_.Count)" }
if ($tickets.Count -gt 0) {
    $n = $tickets[0].numeroTicket
    Write-Output "FIRST=$n status=$($tickets[0].status)"
    try {
        $det = Invoke-RestMethod -Uri "http://localhost:8080/api/tickets/$n" -Headers $h
        Write-Output "DETAIL_OK=$n"
    } catch { Write-Output "DETAIL_FAIL $($_.Exception.Message)" }
}
