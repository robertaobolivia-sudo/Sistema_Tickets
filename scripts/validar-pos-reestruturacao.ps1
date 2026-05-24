# Validação oficial pós-reestruturação (F46/F48 GATE): Maven + Vitest + package + HTTP + Playwright F45.
# Uso (raiz): .\scripts\validar-pos-reestruturacao.ps1
# Dois terminais (recomendado se AutoStart falhar):
#   Terminal 1: .\scripts\start-dev-server.ps1
#   Terminal 2: .\scripts\validar-pos-reestruturacao.ps1 -NoAutoStartServer
#
# Parâmetros:
#   -NoAutoStartServer  não sobe JAR; exige app já em :8080
#   -SkipPackage       pula mvn package
#   -SkipPlaywright     pula E2E F45
#   -KeepServer         não para Java ao final (só com AutoStart)
#   -SkipInitialStop    não mata :8080 no início (uso com validar-pos-reestruturacao-ci.ps1)
#   -BackgroundJava      sobe JAR em background (log em logs/gate-java.log) em vez de janela

param(
    [switch]$NoAutoStartServer,
    [switch]$SkipPackage,
    [switch]$SkipPlaywright,
    [switch]$KeepServer,
    [switch]$SkipInitialStop,
    [switch]$BackgroundJava,
    [int]$HttpWaitSeconds = 300
)

$ErrorActionPreference = 'Stop'
$ProjectRoot = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
if (-not (Test-Path (Join-Path $ProjectRoot 'pom.xml'))) {
    $ProjectRoot = (Get-Location).Path
}

$JarPath = Join-Path $ProjectRoot 'target\suporte-tickets-1.0.0.jar'
$JsDir = Join-Path $ProjectRoot 'src\main\resources\static\js'
$E2eDir = Join-Path $ProjectRoot 'e2e'
$StopScript = Join-Path $ProjectRoot 'scripts\stop-java-8080.ps1'
$LogDir = Join-Path $ProjectRoot 'logs'
$GateJavaLog = Join-Path $LogDir 'gate-java.log'
$BaseUrl = if ($env:E2E_BASE_URL) { $env:E2E_BASE_URL } else { 'http://localhost:8080' }

$results = [ordered]@{
    MavenTest = 'pendente'
    Vitest = 'pendente'
    MavenPackage = 'pendente'
    Http200 = 'pendente'
    PlaywrightF45 = 'pendente'
}

$javaProc = $null
$startedJava = $false

function Write-Step([string]$msg) { Write-Host "`n==> $msg" -ForegroundColor Cyan }
function Write-Ok([string]$msg) { Write-Host "OK: $msg" -ForegroundColor Green }
function Write-Fail([string]$msg) { Write-Host "FALHA: $msg" -ForegroundColor Red }

function Test-HttpOk([string]$url) {
    try {
        $r = Invoke-WebRequest -Uri $url -UseBasicParsing -TimeoutSec 5
        return ($r.StatusCode -eq 200)
    } catch {
        return $false
    }
}

function Wait-Http200([string]$url, [int]$maxSeconds) {
    $deadline = (Get-Date).AddSeconds($maxSeconds)
    while ((Get-Date) -lt $deadline) {
        if (Test-HttpOk $url) { return $true }
        Start-Sleep -Seconds 3
    }
    return $false
}

function Show-Summary {
    Write-Host "`n========== GATE F48 (pos-reestruturacao) ==========" -ForegroundColor Cyan
    foreach ($k in $results.Keys) {
        $color = if ($results[$k] -eq 'OK') { 'Green' } elseif ($results[$k] -match 'SKIP') { 'Yellow' } else { 'Red' }
        Write-Host ("{0,-16} {1}" -f $k, $results[$k]) -ForegroundColor $color
    }
    Write-Host "================================`n" -ForegroundColor Cyan
}

try {
    if (-not $SkipInitialStop) {
        Write-Step 'Parar Java na porta 8080 (evita lock do JAR)'
        & $StopScript | Out-Null
    }

    Write-Step 'Maven test'
    Push-Location $ProjectRoot
    try {
        & mvn -q test
        if ($LASTEXITCODE -ne 0) { throw "mvn test exit $LASTEXITCODE" }
        $results.MavenTest = 'OK'
        Write-Ok 'mvn test'
    } finally {
        Pop-Location
    }

    Write-Step 'Vitest (static/js)'
    Push-Location $JsDir
    try {
        if (-not (Test-Path 'node_modules')) {
            npm install --silent
        }
        npm test
        if ($LASTEXITCODE -ne 0) { throw "Vitest exit $LASTEXITCODE" }
        $results.Vitest = 'OK'
        Write-Ok 'Vitest'
    } finally {
        Pop-Location
    }

    if (-not $SkipPackage) {
        Write-Step 'Maven package -DskipTests'
        Push-Location $ProjectRoot
        try {
            & mvn -q package -DskipTests
            if ($LASTEXITCODE -ne 0) {
                Write-Host 'Tentando de novo após stop-java-8080...' -ForegroundColor Yellow
                & $StopScript | Out-Null
                Start-Sleep -Seconds 2
                & mvn -q package -DskipTests
                if ($LASTEXITCODE -ne 0) { throw "mvn package exit $LASTEXITCODE (JAR em uso?)" }
            }
            $results.MavenPackage = 'OK'
            Write-Ok 'mvn package'
        } finally {
            Pop-Location
        }
    } else {
        $results.MavenPackage = 'SKIP'
    }

    if (-not (Test-Path $JarPath)) {
        throw "JAR não encontrado: $JarPath"
    }

    if ($NoAutoStartServer) {
        Write-Step 'HTTP 200 (app deve estar no ar — start-dev-server.ps1)'
        if (-not (Test-HttpOk "$BaseUrl/")) {
            throw "Sem HTTP 200 em $BaseUrl/. Rode: .\scripts\start-dev-server.ps1"
        }
    } else {
        Write-Step 'Subir app (Java)'
        New-Item -ItemType Directory -Force -Path $LogDir | Out-Null
        if ($BackgroundJava) {
            $javaProc = Start-Process -FilePath 'java' -ArgumentList @('-jar', $JarPath) `
                -WorkingDirectory $ProjectRoot -PassThru -NoNewWindow `
                -RedirectStandardOutput $GateJavaLog -RedirectStandardError $GateJavaLog
            Write-Ok "Java PID $($javaProc.Id) (log: $GateJavaLog)"
        } else {
            $javaProc = Start-Process -FilePath 'java' -ArgumentList @('-jar', $JarPath) `
                -WorkingDirectory $ProjectRoot -PassThru -WindowStyle Normal
            Write-Ok "Java PID $($javaProc.Id)"
        }
        $startedJava = $true
        if (-not (Wait-Http200 "$BaseUrl/" $HttpWaitSeconds)) {
            throw 'Timeout HTTP 200'
        }
    }
    $results.Http200 = 'OK'
    Write-Ok "HTTP 200 $BaseUrl/"

    if ($SkipPlaywright) {
        $results.PlaywrightF45 = 'SKIP'
    } else {
        Write-Step 'Playwright F45 (test:pos-reestruturacao)'
        $env:E2E_SKIP_WEB_SERVER = '1'
        Push-Location $E2eDir
        try {
            if (-not (Test-Path 'node_modules')) {
                npm install --silent
            }
            npm run test:pos-reestruturacao
            if ($LASTEXITCODE -ne 0) { throw "Playwright exit $LASTEXITCODE" }
            $results.PlaywrightF45 = 'OK'
            Write-Ok 'Playwright F45'
        } finally {
            Pop-Location
        }
    }

    if (-not (Test-HttpOk "$BaseUrl/")) {
        throw 'HTTP 200 final falhou'
    }

    Show-Summary
    exit 0
} catch {
    Write-Fail $_.Exception.Message
    Show-Summary
    exit 1
} finally {
    if ($startedJava -and -not $KeepServer -and $null -ne $javaProc -and -not $javaProc.HasExited) {
        Write-Step 'Encerrando Java iniciado por validar-pos-reestruturacao.ps1'
        Stop-Process -Id $javaProc.Id -Force -ErrorAction SilentlyContinue
        Write-Ok "PID $($javaProc.Id) encerrado (use -KeepServer para manter)"
    }
}
