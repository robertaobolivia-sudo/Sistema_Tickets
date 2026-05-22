# Regressão E2E local — build, app 8080, Playwright (Sprint 224)
# Uso (na raiz do projeto ou via caminho completo):
#   .\scripts\run-e2e-local.ps1
#   .\scripts\run-e2e-local.ps1 -SkipPackage          # JAR já gerado
#   .\scripts\run-e2e-local.ps1 -UsePlaywrightServer  # Playwright sobe o JAR (sem script iniciar Java)
#   .\scripts\run-e2e-local.ps1 -KeepServer           # não encerra o Java ao final
#
# Variáveis opcionais: SMOKE_ADMIN_EMAIL, SMOKE_ADMIN_SENHA, E2E_BASE_URL

param(
    [switch]$SkipPackage,
    [switch]$UsePlaywrightServer,
    [switch]$KeepServer,
    [int]$HttpWaitSeconds = 180,
    [int]$MysqlPort = 3306
)

$ErrorActionPreference = 'Stop'
$ProjectRoot = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
if (-not (Test-Path (Join-Path $ProjectRoot 'pom.xml'))) {
    $ProjectRoot = (Get-Location).Path
}

$JarPath = Join-Path $ProjectRoot 'target\suporte-tickets-1.0.0.jar'
$E2eDir = Join-Path $ProjectRoot 'e2e'
$PidFile = Join-Path $ProjectRoot 'logs\e2e-app.pid'
$BaseUrl = if ($env:E2E_BASE_URL) { $env:E2E_BASE_URL } else { 'http://localhost:8080' }

function Write-Step([string]$msg) { Write-Host "`n==> $msg" -ForegroundColor Cyan }
function Write-Ok([string]$msg) { Write-Host "OK: $msg" -ForegroundColor Green }
function Write-Warn([string]$msg) { Write-Host "AVISO: $msg" -ForegroundColor Yellow }
function Write-Fail([string]$msg) { Write-Host "FALHA: $msg" -ForegroundColor Red }

function Test-HttpOk([string]$url) {
    try {
        $r = Invoke-WebRequest -Uri $url -UseBasicParsing -TimeoutSec 5
        return ($r.StatusCode -eq 200)
    } catch {
        return $false
    }
}

function Stop-AppOnPort8080 {
    $conns = Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue
    foreach ($c in $conns) {
        $pid = $c.OwningProcess
        if ($pid -and $pid -gt 0) {
            Write-Warn "Encerrando processo na porta 8080 (PID $pid)"
            Stop-Process -Id $pid -Force -ErrorAction SilentlyContinue
        }
    }
    Start-Sleep -Seconds 2
}

function Wait-Http200([string]$url, [int]$maxSeconds) {
    $deadline = (Get-Date).AddSeconds($maxSeconds)
    while ((Get-Date) -lt $deadline) {
        if (Test-HttpOk $url) { return $true }
        Start-Sleep -Seconds 3
    }
    return $false
}

$startedJava = $false
$javaProc = $null

try {
    Write-Step 'Verificando MySQL (TCP)'
    $mysqlOk = Test-NetConnection -ComputerName localhost -Port $MysqlPort -WarningAction SilentlyContinue |
        Select-Object -ExpandProperty TcpTestSucceeded
    if (-not $mysqlOk) {
        Write-Fail "MySQL não acessível em localhost:$MysqlPort. Suba o serviço antes do E2E."
        exit 1
    }
    Write-Ok "Porta $MysqlPort aberta"

    if (-not $SkipPackage) {
        Write-Step 'Maven package (sem testes unitários Java)'
        Push-Location $ProjectRoot
        try {
            & mvn -q package -DskipTests
            if ($LASTEXITCODE -ne 0) {
                Write-Fail "mvn package falhou (exit $LASTEXITCODE). Feche o JAR em execução se o arquivo estiver bloqueado."
                exit $LASTEXITCODE
            }
        } finally {
            Pop-Location
        }
        Write-Ok 'JAR gerado'
    } else {
        Write-Warn 'SkipPackage: não executou mvn package'
    }

    if (-not (Test-Path $JarPath)) {
        Write-Fail "JAR não encontrado: $JarPath"
        exit 1
    }

    if ($UsePlaywrightServer) {
        Write-Step 'Modo Playwright webServer (E2E_SKIP_WEB_SERVER não definido)'
        Remove-Item Env:E2E_SKIP_WEB_SERVER -ErrorAction SilentlyContinue
        if (Test-HttpOk "$BaseUrl/") {
            Write-Ok "App já responde em $BaseUrl — Playwright reutiliza (reuseExistingServer)"
        } else {
            Write-Ok 'Playwright iniciará o JAR conforme playwright.config.ts'
        }
    } else {
        Write-Step 'Subindo aplicação (java -jar)'
        if (Test-HttpOk "$BaseUrl/") {
            Write-Ok "App já em $BaseUrl — reutilizando (E2E_SKIP_WEB_SERVER=1)"
        } else {
            Stop-AppOnPort8080
            New-Item -ItemType Directory -Force -Path (Join-Path $ProjectRoot 'logs') | Out-Null
            $javaProc = Start-Process -FilePath 'java' -ArgumentList @('-jar', $JarPath) `
                -WorkingDirectory $ProjectRoot -PassThru -WindowStyle Normal
            $startedJava = $true
            $javaProc.Id | Out-File -FilePath $PidFile -Encoding ascii -NoNewline
            Write-Ok "Java iniciado (PID $($javaProc.Id)). Log no console da janela do processo."

            Write-Step "Aguardando HTTP 200 em $BaseUrl/ (até ${HttpWaitSeconds}s)"
            if (-not (Wait-Http200 "$BaseUrl/" $HttpWaitSeconds)) {
                Write-Fail 'Timeout aguardando aplicação. Verifique MySQL e o console Java.'
                exit 1
            }
        }
        $env:E2E_SKIP_WEB_SERVER = '1'
    }

    Write-Step 'Playwright — npm test (3 specs)'
    Push-Location $E2eDir
    try {
        if (-not (Test-Path 'node_modules')) {
            Write-Warn 'node_modules ausente — executando npm install'
            npm install
        }
        npm test
        if ($LASTEXITCODE -ne 0) {
            Write-Fail "Playwright falhou (exit $LASTEXITCODE). Relatório: npx playwright show-report"
            exit $LASTEXITCODE
        }
    } finally {
        Pop-Location
    }

    Write-Host ''
    Write-Ok 'Regressão E2E concluída — 3 specs esperados (com pesquisa, sem pesquisa, sem contato)'
    Write-Host "HTTP: $BaseUrl/ → 200" -ForegroundColor Green
    Write-Host 'Relatório HTML: cd e2e; npx playwright show-report' -ForegroundColor Gray

    exit 0
} finally {
    if ($startedJava -and -not $KeepServer -and $null -ne $javaProc -and -not $javaProc.HasExited) {
        Write-Step 'Encerrando Java iniciado por este script'
        Stop-Process -Id $javaProc.Id -Force -ErrorAction SilentlyContinue
        Remove-Item $PidFile -ErrorAction SilentlyContinue
        Write-Ok "Processo $($javaProc.Id) finalizado. Use -KeepServer para manter a app no ar."
    } elseif ($KeepServer -and $startedJava) {
        Write-Warn "App mantida em execução (PID $($javaProc.Id)). Para parar: Stop-Process -Id $($javaProc.Id)"
    }
}
