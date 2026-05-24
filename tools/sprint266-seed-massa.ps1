# Massa mínima Sprint 266 — etiqueta, cidade/UF, avaliação ruim (DEV)
$ErrorActionPreference = 'Stop'
$body = '{"email":"robertaobolivia@gmail.com","senha":"@Hipcom123789"}'
$login = Invoke-RestMethod -Uri 'http://localhost:8080/api/analistas/login' -Method Post -Body $body -ContentType 'application/json; charset=utf-8'
$headers = @{
    'X-Analista-Id'    = [string]$login.id
    'X-Analista-Token' = [string]$login.authToken
    'Content-Type'     = 'application/json'
}

$contatoId = 63
$contato = Invoke-RestMethod -Uri "http://localhost:8080/api/contatos/$contatoId" -Headers $headers
$payload = @{
    clienteId    = $contato.clienteId
    nome         = $contato.nome
    email        = $contato.email
    empresaLocal = $contato.empresaLocal
    cidade       = 'Campinas'
    uf           = 'SP'
    observacoes  = $contato.observacoes
    ativo        = $true
} | ConvertTo-Json
Invoke-RestMethod -Uri "http://localhost:8080/api/contatos/$contatoId" -Method Put -Headers $headers -Body $payload | Out-Null
Write-Output "OK cidade/UF contato $contatoId"

$etiqBody = '{"etiquetaIds":[1]}'
Invoke-RestMethod -Uri "http://localhost:8080/api/contatos/$contatoId/etiquetas" -Method Put -Headers $headers -Body $etiqBody | Out-Null
Write-Output 'OK etiqueta id=1 no contato 63'

$hist = @(Invoke-RestMethod -Uri "http://localhost:8080/api/contatos/$contatoId/historico-tickets" -Headers $headers)
if ($hist.Count -lt 1) { throw 'Sem ticket para contato 63' }
$protocolo = $hist[0].protocolo
Write-Output "Ticket protocolo=$protocolo"

try {
    $sat = Invoke-WebRequest -Uri "http://localhost:8080/api/tickets/$protocolo/satisfacao" -Headers $headers -UseBasicParsing
    if ($sat.StatusCode -eq 204) {
        $reg = '{"nota":2,"comentario":"Smoke 266 avaliacao ruim"}'
        Invoke-RestMethod -Uri "http://localhost:8080/api/tickets/$protocolo/satisfacao" -Method Post -Headers $headers -Body $reg | Out-Null
        Write-Output 'OK satisfacao criada com nota 2'
    }
} catch {
    $resp = '{"nota":2,"comentario":"Smoke 266 avaliacao ruim"}'
    Invoke-RestMethod -Uri "http://localhost:8080/api/tickets/$protocolo/satisfacao/responder" -Method Post -Headers $headers -Body $resp | Out-Null
    Write-Output 'OK satisfacao respondida nota 2'
}
