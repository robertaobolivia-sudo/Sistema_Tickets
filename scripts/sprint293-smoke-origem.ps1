# Sprint 293 — smoke origem atendimento (API)
$ErrorActionPreference = "Stop"
$base = "http://localhost:8080"
$email = "robertaobolivia@gmail.com"
$senha = "@Hipcom123789"
$contatoId = 69
$clienteId = 89
$telPrincipal = "5511980030111"
$telAdicional = "5512942833853"
$matrizId = 5

function Invoke-Json($Method, $Uri, $Headers, $Body) {
    $params = @{ Method = $Method; Uri = $Uri; Headers = $Headers; ContentType = "application/json" }
    if ($Body) { $params.Body = ($Body | ConvertTo-Json -Compress) }
    return Invoke-RestMethod @params
}

$login = Invoke-Json POST "$base/api/analistas/login" @{} @{ email = $email; senha = $senha }
$h = @{
    "X-Analista-Id"    = [string]$login.id
    "X-Analista-Token" = $login.authToken
}

$grupos = Invoke-Json GET "$base/api/grupos-categoria" $h $null
$grupo = ($grupos | Where-Object { $_.ativo -ne $false } | Select-Object -First 1)
$subs = Invoke-Json GET "$base/api/subgrupos-categoria/grupo/$($grupo.id)" $h $null
$sub = ($subs | Where-Object { $_.ativo -ne $false } | Select-Object -First 1)
$motivos = Invoke-Json GET "$base/api/motivos?subgrupoId=$($sub.id)" $h $null
$motivo = ($motivos | Where-Object { $_.ativo -ne $false } | Select-Object -First 1)
$encBody = @{
    grupoId                    = [long]$grupo.id
    subgrupoId                 = [long]$sub.id
    motivoId                   = [long]$motivo.id
    comentarioEncerramento     = "Smoke Sprint 293 encerramento"
    enviarPesquisaSatisfacao   = $false
}

function Encerrar-AtivoSeExistir($contatoWhatsappId) {
    try {
        $ativo = Invoke-RestMethod -Method GET -Uri "$base/api/tickets/ativo?clienteId=$clienteId&contatoWhatsappId=$contatoWhatsappId" -Headers $h -ErrorAction Stop
        if ($ativo.numeroTicket) {
            Invoke-Json PUT "$base/api/tickets/$($ativo.numeroTicket)/encerrar" $h $encBody | Out-Null
            Write-Host "Encerrado ativo $($ativo.numeroTicket) contatoWhatsappId=$contatoWhatsappId"
        }
    } catch [System.Net.WebException] {
        if ($_.Exception.Response.StatusCode.value__ -eq 204) { return }
        throw
    }
}

Encerrar-AtivoSeExistir $contatoId

$tag = [DateTimeOffset]::UtcNow.ToUnixTimeSeconds()
$bodyPrincipal = @{
    telefone           = $telPrincipal
    nomeContato        = "Smoke293 Principal"
    mensagem           = "Entrada principal smoke $tag"
    canal              = "WHATSAPP"
    whatsappMatrizId   = $matrizId
    origemExternaId    = "smoke293-principal-$tag"
}
$resP = Invoke-Json POST "$base/api/integracoes/whatsapp/mensagens" $h $bodyPrincipal
$numeroPrincipal = $resP.numeroTicket
if ($resP.aguardandoDecisao -and $resP.pendenciaDecisaoId) {
    $gerP = Invoke-Json POST "$base/api/chats/interacoes-pendentes/$($resP.pendenciaDecisaoId)/gerar-ticket" $h $null
    $numeroPrincipal = $gerP.numeroTicket
} elseif (-not $resP.ticketCriado) {
    throw "Falha entrada principal; $($resP | ConvertTo-Json -Compress)"
}
$tkP = Invoke-Json GET "$base/api/tickets/$numeroPrincipal" $h $null
if ($tkP.contatoId -ne $contatoId) { throw "Principal contatoId=$($tkP.contatoId) esperado $contatoId" }
if ($tkP.atendimentoTelefoneTipo -ne "PRINCIPAL") { throw "Principal tipo=$($tkP.atendimentoTelefoneTipo)" }

Invoke-Json PUT "$base/api/tickets/$numeroPrincipal/encerrar" $h $encBody | Out-Null

$bodyAdic = @{
    telefone           = $telAdicional
    nomeContato        = "Smoke293 Adicional"
    mensagem           = "Entrada adicional smoke $tag"
    canal              = "WHATSAPP"
    whatsappMatrizId   = $matrizId
    origemExternaId    = "smoke293-adicional-$tag"
}
$resA = Invoke-Json POST "$base/api/integracoes/whatsapp/mensagens" $h $bodyAdic
$numeroAdicional = $resA.numeroTicket
if ($resA.aguardandoDecisao -and $resA.pendenciaDecisaoId) {
    $gerA = Invoke-Json POST "$base/api/chats/interacoes-pendentes/$($resA.pendenciaDecisaoId)/gerar-ticket" $h $null
    $numeroAdicional = $gerA.numeroTicket
} elseif (-not $resA.ticketCriado) {
    throw "Falha entrada adicional; $($resA | ConvertTo-Json -Compress)"
}
$tkA = Invoke-Json GET "$base/api/tickets/$numeroAdicional" $h $null
if ($tkA.contatoId -ne $contatoId) { throw "Adicional contatoId=$($tkA.contatoId) esperado $contatoId" }
if ($tkA.atendimentoTelefoneTipo -ne "ADICIONAL") { throw "Adicional tipo=$($tkA.atendimentoTelefoneTipo)" }

$hist = Invoke-Json GET "$base/api/contatos/$contatoId/historico-tickets" $h $null
$hp = $hist | Where-Object { $_.protocolo -eq $numeroPrincipal } | Select-Object -First 1
$ha = $hist | Where-Object { $_.protocolo -eq $numeroAdicional } | Select-Object -First 1
if ($hp.atendimentoTelefoneTipo -ne "PRINCIPAL") { throw "Historico principal tipo=$($hp.atendimentoTelefoneTipo)" }
if ($ha.atendimentoTelefoneTipo -ne "ADICIONAL") { throw "Historico adicional tipo=$($ha.atendimentoTelefoneTipo)" }

@{
    status = "OK"
    ticketPrincipal = $numeroPrincipal
    ticketAdicional = $numeroAdicional
    contatoId = $contatoId
    tipoPrincipal = $tkP.atendimentoTelefoneTipo
    tipoAdicional = $tkA.atendimentoTelefoneTipo
    atendimentoTelefonePrincipal = $tkP.atendimentoTelefone
    atendimentoTelefoneAdicional = $tkA.atendimentoTelefone
} | ConvertTo-Json -Depth 4
