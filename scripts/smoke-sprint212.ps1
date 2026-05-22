# Sprint 212 — smoke encerramento + pesquisa + ticket ativo (API)
param(
    [string]$BaseUrl = 'http://localhost:8080',
    [string]$Email = $env:SMOKE_ADMIN_EMAIL,
    [string]$Senha = $env:SMOKE_ADMIN_SENHA
)
if (-not $Email) { $Email = 'robertaobolivia@gmail.com' }
if (-not $Senha) { $Senha = '@Hipcom123789' }

$ErrorActionPreference = 'Stop'
$login = Invoke-RestMethod -Uri "$BaseUrl/api/analistas/login" -Method Post -ContentType 'application/json' -Body (@{ email = $Email; senha = $Senha } | ConvertTo-Json)
$h = @{ 'X-Analista-Id' = "$($login.id)"; 'X-Analista-Token' = $login.authToken }

$mot = (Invoke-RestMethod -Uri "$BaseUrl/api/motivos?subgrupoId=7" -Headers $h)[0]
$encBody = @{
    grupoId        = $mot.grupoId
    subgrupoId     = $mot.subgrupoId
    motivoId       = $mot.id
    comentarioEncerramento = 'Smoke 212'
} 

$suffix = [DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds()
function New-TicketIntegracao([string]$telefone, [string]$origem) {
    $origem = "$origem-$suffix"
    $p = @{
        whatsappMatrizId = 2
        telefone         = $telefone
        nomeContato      = 'Smoke 212'
        mensagem         = "Abertura $origem"
        canal            = 'WHATSAPP'
        origemExternaId  = $origem
    } | ConvertTo-Json
    Invoke-RestMethod -Uri "$BaseUrl/api/integracoes/whatsapp/mensagens" -Method Post -Headers ($h + @{ 'Content-Type' = 'application/json' }) -Body $p
}

# A — sem pesquisa
$telA = '5511963978920'
$tA = New-TicketIntegracao $telA 'smoke-212-a'
$bodyA = $encBody.Clone(); $bodyA['enviarPesquisaSatisfacao'] = $false
$rA = Invoke-RestMethod -Uri "$BaseUrl/api/tickets/$($tA.numeroTicket)/encerrar" -Method Put -Headers ($h + @{ 'Content-Type' = 'application/json' }) -Body ($bodyA | ConvertTo-Json)
$detA = Invoke-RestMethod -Uri "$BaseUrl/api/tickets/$($tA.numeroTicket)" -Headers $h
$satA = Invoke-RestMethod -Uri "$BaseUrl/api/tickets/$($tA.numeroTicket)/satisfacao" -Headers $h
Write-Output "A ticket=$($tA.numeroTicket) status=$($rA.status) sat=$($satA.status) motivo=$($detA.motivoNome)"

# B — com pesquisa
$telB = '5511963978921'
$tB = New-TicketIntegracao $telB 'smoke-212-b'
$bodyB = $encBody.Clone(); $bodyB['enviarPesquisaSatisfacao'] = $true
$rB = Invoke-RestMethod -Uri "$BaseUrl/api/tickets/$($tB.numeroTicket)/encerrar" -Method Put -Headers ($h + @{ 'Content-Type' = 'application/json' }) -Body ($bodyB | ConvertTo-Json)
$satB = Invoke-RestMethod -Uri "$BaseUrl/api/tickets/$($tB.numeroTicket)/satisfacao" -Headers $h
$link = $rB.avaliacaoLinkPublico
if (-not $link) { throw 'B: avaliacaoLinkPublico ausente' }
if ($link -notmatch 'token=([^&]+)') { throw 'B: token nao encontrado no link' }
$token = $Matches[1]
Write-Output "B ticket=$($tB.numeroTicket) status=$($rB.status) sat=$($satB.status) envio=$($satB.envioStatus) linkOk=1"

# C — publico
$pubGet = Invoke-RestMethod -Uri "$BaseUrl/api/public/avaliacoes/$token"
$pubPost = Invoke-RestMethod -Uri "$BaseUrl/api/public/avaliacoes/$token/responder" -Method Post -ContentType 'application/json' -Body '{"nota":5,"comentario":"Smoke 212 ok"}'
try {
    Invoke-RestMethod -Uri "$BaseUrl/api/public/avaliacoes/$token/responder" -Method Post -ContentType 'application/json' -Body '{"nota":4,"comentario":"dup"}'
    throw 'C: segunda resposta deveria falhar'
} catch {
    if ($_.Exception.Response.StatusCode.value__ -ne 400) { throw $_ }
}
Write-Output "C publico status=$($pubPost.status) msg=$($pubPost.mensagem)"

# D — ticket ativo (telefone dedicado evita pendência legada do Contato 8)
$telD = '5511963978922'
$tD1 = New-TicketIntegracao $telD 'smoke-212-d1'
$m2 = Invoke-RestMethod -Uri "$BaseUrl/api/integracoes/whatsapp/mensagens" -Method Post -Headers ($h + @{ 'Content-Type' = 'application/json' }) -Body (@{
    whatsappMatrizId = 2; telefone = $telD; nomeContato = 'Smoke 212 D'; mensagem = 'D reuse'; canal = 'WHATSAPP'; origemExternaId = 'smoke-212-d2'
} | ConvertTo-Json)
$tOutro = New-TicketIntegracao '5511888777666' 'smoke-212-d-outro'
Write-Output "D1 criado=$($tD1.ticketCriado) num=$($tD1.numeroTicket) D2 criado=$($m2.ticketCriado) num=$($m2.numeroTicket) outro=$($tOutro.numeroTicket) criadoOutro=$($tOutro.ticketCriado)"

Write-Output 'SMOKE_212_OK'
