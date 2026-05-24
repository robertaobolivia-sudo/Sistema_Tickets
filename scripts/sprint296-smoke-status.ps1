# Sprint 296 — smoke máquina de status do ticket
$ErrorActionPreference = "Stop"
$base = "http://localhost:8080"
$email = "robertaobolivia@gmail.com"
$senha = "@Hipcom123789"
$clienteId = 89
$matrizId = 5
$suffix = [DateTimeOffset]::UtcNow.ToUnixTimeSeconds()
$telSmoke = "5511963978" + ($suffix % 1000).ToString("000")

function Invoke-Json($Method, $Uri, $Headers, $Body) {
    $params = @{ Method = $Method; Uri = $Uri; Headers = $Headers; ContentType = "application/json" }
    if ($Body) { $params.Body = ($Body | ConvertTo-Json -Compress) }
    return Invoke-RestMethod @params
}

function Invoke-ExpectBadRequest($Method, $Uri, $Headers, $Body, $needle) {
    try {
        $params = @{ Method = $Method; Uri = $Uri; Headers = $Headers; ContentType = "application/json"; ErrorAction = "Stop" }
        if ($Body) { $params.Body = ($Body | ConvertTo-Json -Compress) }
        Invoke-WebRequest @params -UseBasicParsing | Out-Null
        throw "Esperava erro 400, mas a requisicao teve sucesso: $Uri"
    } catch [System.Net.WebException] {
        $code = [int]$_.Exception.Response.StatusCode
        if ($code -ne 400) { throw "Esperava 400 em $Uri, recebeu $code" }
        $reader = [IO.StreamReader]::new($_.Exception.Response.GetResponseStream())
        $txt = $reader.ReadToEnd()
        if ($needle -and $txt -notmatch [regex]::Escape($needle)) {
            throw "Corpo 400 sem '$needle': $txt"
        }
        return $txt
    }
}

$login = Invoke-Json POST "$base/api/analistas/login" @{} @{ email = $email; senha = $senha }
$h = @{ "X-Analista-Id" = [string]$login.id; "X-Analista-Token" = $login.authToken }
$analistaId = [long]$login.id

$grupos = Invoke-Json GET "$base/api/grupos-categoria" $h $null
$grupo = ($grupos | Where-Object { $_.ativo -ne $false } | Select-Object -First 1)
$subs = Invoke-Json GET "$base/api/subgrupos-categoria/grupo/$($grupo.id)" $h $null
$sub = ($subs | Where-Object { $_.ativo -ne $false } | Select-Object -First 1)
$motivos = Invoke-Json GET "$base/api/motivos?subgrupoId=$($sub.id)" $h $null
$motivo = ($motivos | Where-Object { $_.ativo -ne $false } | Select-Object -First 1)
$encBody = @{
    grupoId                  = [long]$grupo.id
    subgrupoId               = [long]$sub.id
    motivoId                 = [long]$motivo.id
    comentarioEncerramento   = "Smoke Sprint 296 encerramento"
    enviarPesquisaSatisfacao = $false
}

# --- Ticket principal: fluxo permitido ---
$criar = @{
    clienteContratanteId = $clienteId
    cliente              = "Bruno Fast"
    telefone             = $telSmoke
    nomeContato          = "Smoke296 Status"
    mensagem             = "Abertura smoke 296 $suffix"
    canal                = "WHATSAPP"
    whatsappMatrizId     = $matrizId
}
$t = Invoke-Json POST "$base/api/tickets" $h $criar
$tk = $t.numeroTicket
if ($t.status -ne "ABERTO") { throw "Esperado ABERTO, veio $($t.status)" }

$t = Invoke-Json PUT "$base/api/tickets/$tk/status" $h @{ status = "EM_ATENDIMENTO"; analistaId = $analistaId }
if ($t.status -ne "EM_ATENDIMENTO") { throw "Falha EM_ATENDIMENTO" }

$t = Invoke-Json PUT "$base/api/tickets/$tk/status" $h @{ status = "AGUARDANDO_CLIENTE" }
if ($t.status -ne "AGUARDANDO_CLIENTE") { throw "Falha AGUARDANDO_CLIENTE" }

$t = Invoke-Json PUT "$base/api/tickets/$tk/status" $h @{ status = "EM_ATENDIMENTO"; analistaId = $analistaId }
if ($t.status -ne "EM_ATENDIMENTO") { throw "Falha retorno EM_ATENDIMENTO" }

$t = Invoke-Json PUT "$base/api/tickets/$tk/encerrar" $h $encBody
if ($t.status -ne "RESOLVIDO") { throw "Falha encerramento RESOLVIDO" }

# --- Bloqueios no ticket resolvido ---
Invoke-ExpectBadRequest PUT "$base/api/tickets/$tk/status" $h @{ status = "RESOLVIDO" } "encerramento"
Invoke-ExpectBadRequest PUT "$base/api/tickets/$tk/status" $h @{ status = "INDEVIDO" } "classificacao indevido"
Invoke-ExpectBadRequest PUT "$base/api/tickets/$tk/status" $h @{ status = "EM_ATENDIMENTO"; analistaId = $analistaId } "Transicao"

Invoke-ExpectBadRequest PUT "$base/api/tickets/$tk/encerrar" $h $encBody "encerrado"

# --- Reabertura ---
$t = Invoke-Json PUT "$base/api/tickets/$tk/reabrir" $h $null
if ($t.status -ne "ABERTO") { throw "Reabertura deveria voltar ABERTO" }

# --- Ticket INDEVIDO ---
$criar2 = $criar.Clone()
$criar2.telefone = "5511963979" + ($suffix % 1000).ToString("000")
$criar2.mensagem = "Smoke 296 indevido $suffix"
$t2 = Invoke-Json POST "$base/api/tickets" $h $criar2
$tk2 = $t2.numeroTicket
Invoke-Json PUT "$base/api/tickets/$tk2/status" $h @{ status = "EM_ATENDIMENTO"; analistaId = $analistaId } | Out-Null
$indevBody = @{ confirmacao = $true; motivoOperacional = "INDEVIDO"; comentario = "smoke 296" }
$t2 = Invoke-Json PUT "$base/api/tickets/$tk2/classificar-indevido" $h $indevBody
if ($t2.status -ne "INDEVIDO") { throw "Classificacao INDEVIDO falhou" }

Invoke-ExpectBadRequest PUT "$base/api/tickets/$tk2/reabrir" $h $null "indevido"
Invoke-ExpectBadRequest PUT "$base/api/tickets/$tk2/status" $h @{ status = "EM_ATENDIMENTO"; analistaId = $analistaId } "Transicao"

@{
    status           = "OK"
    ticketFluxo      = $tk
    ticketIndevido   = $tk2
    transicoesOk     = @(
        "ABERTO->EM_ATENDIMENTO",
        "EM_ATENDIMENTO->AGUARDANDO_CLIENTE",
        "AGUARDANDO_CLIENTE->EM_ATENDIMENTO",
        "EM_ATENDIMENTO->RESOLVIDO(encerrar)",
        "RESOLVIDO->ABERTO(reabrir)",
        "EM_ATENDIMENTO->INDEVIDO(classificar)"
    )
    bloqueiosOk      = @(
        "PUT RESOLVIDO",
        "PUT INDEVIDO",
        "PUT terminal->ativo",
        "encerrar ja encerrado",
        "reabrir INDEVIDO"
    )
} | ConvertTo-Json -Depth 4
