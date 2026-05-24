# Para processo escutando na porta 8080 (Windows).
# Uso: .\scripts\stop-java-8080.ps1
# Não falha se nenhum processo estiver na porta.

param([int]$Port = 8080)

$ErrorActionPreference = 'Continue'
$stopped = @()

$conns = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue
foreach ($c in $conns) {
    $pid = $c.OwningProcess
    if ($pid -and $pid -gt 0 -and $stopped -notcontains $pid) {
        Write-Host "Parando PID $pid (porta $Port)..." -ForegroundColor Yellow
        Stop-Process -Id $pid -Force -ErrorAction SilentlyContinue
        $stopped += $pid
    }
}

if ($stopped.Count -eq 0) {
    Write-Host "Nenhum processo na porta $Port." -ForegroundColor Gray
} else {
    Write-Host "PIDs parados: $($stopped -join ', ')" -ForegroundColor Green
    Start-Sleep -Seconds 2
}

exit 0
