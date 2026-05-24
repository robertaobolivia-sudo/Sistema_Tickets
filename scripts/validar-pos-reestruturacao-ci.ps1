# Gate F48 — modo CI/local sem janela Java (chama validar-pos-reestruturacao.ps1).
# Uso (raiz): .\scripts\validar-pos-reestruturacao-ci.ps1

param([switch]$SkipPlaywright)

$ErrorActionPreference = 'Stop'
$ProjectRoot = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
$GateScript = Join-Path $ProjectRoot 'scripts\validar-pos-reestruturacao.ps1'
$StopScript = Join-Path $ProjectRoot 'scripts\stop-java-8080.ps1'

$args = @('-BackgroundJava')
if ($SkipPlaywright) {
    $args += '-SkipPlaywright'
}

try {
    & $GateScript @args
    exit $LASTEXITCODE
} finally {
    & $StopScript | Out-Null
}
