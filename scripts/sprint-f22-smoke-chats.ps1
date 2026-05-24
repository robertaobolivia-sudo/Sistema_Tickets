# Sprint F22 — smoke API mínimo Chats (sem /api/carteiras no front)
param(
    [string]$BaseUrl = 'http://localhost:8080',
    [string]$Email = $env:SMOKE_EMAIL,
    [string]$Senha = $env:SMOKE_SENHA,
    [string]$Token = $env:SMOKE_AUTH_TOKEN
)

$ErrorActionPreference = 'Stop'

function Get-Token {
    if ($Token) { return $Token }
    if (-not $Email -or -not $Senha) {
        throw 'Informe SMOKE_AUTH_TOKEN ou SMOKE_EMAIL + SMOKE_SENHA'
    }
    $login = Invoke-RestMethod -Uri "$BaseUrl/api/analistas/login" -Method Post `
        -ContentType 'application/json' -Body (@{ email = $Email; senha = $Senha } | ConvertTo-Json)
    return $login.token
}

$root = Invoke-WebRequest -Uri $BaseUrl -UseBasicParsing
if ($root.StatusCode -ne 200) { throw "HTTP root $($root.StatusCode)" }
Write-Host "OK HTTP $($root.StatusCode) $BaseUrl"

$t = Get-Token
$headers = @{ Authorization = "Bearer $t" }

$tickets = Invoke-RestMethod -Uri "$BaseUrl/api/tickets?status=ALL" -Headers $headers
$list = @($tickets)
if ($tickets.content) { $list = @($tickets.content) }
Write-Host "OK tickets listados: $($list.Count)"

if ($list.Count -gt 0) {
    $num = $list[0].numeroTicket
    $detail = Invoke-RestMethod -Uri "$BaseUrl/api/tickets/$num" -Headers $headers
    Write-Host "OK ticket detalhe $num cliente=$($detail.cliente) arte=$($detail.clienteArteHeaderChatsUrl)"
}

Write-Host 'OK smoke API F22 (carteiras nao e chamada por este script; front Chats nao importa carteiraService)'
