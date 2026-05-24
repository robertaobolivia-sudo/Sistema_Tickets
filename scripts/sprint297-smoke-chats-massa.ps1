# Sprint 297 — massa para smoke Chats (status + indevido)
$ErrorActionPreference = "Stop"
$base = "http://localhost:8080"
$email = "robertaobolivia@gmail.com"
$senha = "@Hipcom123789"
$clienteId = 89
$matrizId = 5
$suffix = [DateTimeOffset]::UtcNow.ToUnixTimeSeconds()
$telA = "5511963979" + ($suffix % 1000).ToString("000")
$telB = "5511963980" + ($suffix % 1000).ToString("000")

function Invoke-Json($Method, $Uri, $Headers, $Body) {
    $params = @{ Method = $Method; Uri = $Uri; Headers = $Headers; ContentType = "application/json" }
    if ($Body) { $params.Body = ($Body | ConvertTo-Json -Compress) }
    return Invoke-RestMethod @params
}

$login = Invoke-Json POST "$base/api/analistas/login" @{} @{ email = $email; senha = $senha }
$h = @{ "X-Analista-Id" = [string]$login.id; "X-Analista-Token" = $login.authToken }

$criarBase = @{
    clienteContratanteId = $clienteId
    cliente              = "Bruno Fast"
    canal                = "WHATSAPP"
    whatsappMatrizId     = $matrizId
}

$tA = Invoke-Json POST "$base/api/tickets" $h ($criarBase + @{
    telefone    = $telA
    nomeContato = "Smoke297 Fluxo"
    mensagem    = "Smoke 297 fluxo status $suffix"
})
if ($tA.status -ne "ABERTO") { throw "Ticket A: esperado ABERTO, veio $($tA.status)" }

$tB = Invoke-Json POST "$base/api/tickets" $h ($criarBase + @{
    telefone    = $telB
    nomeContato = "Smoke297 Indevido"
    mensagem    = "Smoke 297 classificar indevido $suffix"
})
if ($tB.status -ne "ABERTO") { throw "Ticket B: esperado ABERTO, veio $($tB.status)" }

Write-Output "SPRINT297_TICKET_FLUXO=$($tA.numeroTicket)"
Write-Output "SPRINT297_TICKET_INDEVIDO=$($tB.numeroTicket)"
Write-Output "SPRINT297_CONTATO_A=$($tA.contatoId)"
