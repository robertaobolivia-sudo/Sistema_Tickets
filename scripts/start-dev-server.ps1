# Build + sobe JAR em janela dedicada (não use o mesmo terminal do Playwright).
# Uso (raiz do projeto): .\scripts\start-dev-server.ps1
# Opcional: .\scripts\start-dev-server.ps1 -SkipPackage

param(
    [switch]$SkipPackage,
    [int]$HttpWaitSeconds = 180
)

$ErrorActionPreference = 'Stop'
$ProjectRoot = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
if (-not (Test-Path (Join-Path $ProjectRoot 'pom.xml'))) {
    $ProjectRoot = (Get-Location).Path
}

$JarPath = Join-Path $ProjectRoot 'target\suporte-tickets-1.0.0.jar'
$BaseUrl = if ($env:E2E_BASE_URL) { $env:E2E_BASE_URL } else { 'http://localhost:8080' }
$StopScript = Join-Path $ProjectRoot 'scripts\stop-java-8080.ps1'

function Test-HttpOk([string]$url) {
    try {
        $r = Invoke-WebRequest -Uri $url -UseBasicParsing -TimeoutSec 5
        return ($r.StatusCode -eq 200)
    } catch {
        return $false
    }
}

Write-Host "`n==> Liberando porta 8080" -ForegroundColor Cyan
& $StopScript

if (-not $SkipPackage) {
    Write-Host "`n==> mvn package -DskipTests" -ForegroundColor Cyan
    Push-Location $ProjectRoot
    try {
        & mvn -q package -DskipTests
        if ($LASTEXITCODE -ne 0) {
            Write-Host "FALHA: mvn package (exit $LASTEXITCODE). Pare o Java na 8080 e tente de novo." -ForegroundColor Red
            exit $LASTEXITCODE
        }
    } finally {
        Pop-Location
    }
}

if (-not (Test-Path $JarPath)) {
    Write-Host "FALHA: JAR ausente: $JarPath" -ForegroundColor Red
    exit 1
}

if (Test-HttpOk "$BaseUrl/") {
    Write-Host "OK: App já responde em $BaseUrl/" -ForegroundColor Green
    exit 0
}

Write-Host "`n==> Iniciando java -jar (janela separada)" -ForegroundColor Cyan
$proc = Start-Process -FilePath 'java' -ArgumentList @('-jar', $JarPath) `
    -WorkingDirectory $ProjectRoot -PassThru -WindowStyle Normal
Write-Host "PID $($proc.Id) - mantenha a janela Java aberta durante o E2E." -ForegroundColor Yellow

$deadline = (Get-Date).AddSeconds($HttpWaitSeconds)
while ((Get-Date) -lt $deadline) {
    if (Test-HttpOk "$BaseUrl/") {
        Write-Host "OK: HTTP 200 em $BaseUrl/" -ForegroundColor Green
        Write-Host "Em outro terminal: cd e2e; `$env:E2E_SKIP_WEB_SERVER='1'; npm run test:pos-reestruturacao" -ForegroundColor Gray
        exit 0
    }
    Start-Sleep -Seconds 3
}

Write-Host 'FALHA: timeout aguardando HTTP 200. Verifique MySQL e a janela do Java.' -ForegroundColor Red
exit 1
