$ErrorActionPreference = 'Stop'
$body = '{"email":"robertaobolivia@gmail.com","senha":"@Hipcom123789"}'
$login = Invoke-RestMethod -Uri 'http://localhost:8080/api/analistas/login' -Method Post -Body $body -ContentType 'application/json; charset=utf-8'
$headers = @{
    'X-Analista-Id'    = [string]$login.id
    'X-Analista-Token' = [string]$login.authToken
}

function Get-Contatos([string]$query) {
    $uri = "http://localhost:8080/api/contatos?$query"
    return @(Invoke-RestMethod -Uri $uri -Headers $headers)
}

$all = Get-Contatos 'gestao=true'
Write-Output "TOTAL=$($all.Count)"
$impossivel = Get-Contatos 'gestao=true&busca=zzznomatch999'
Write-Output "BUSCA_IMPOSSIVEL count=$($impossivel.Count) (esperado 0 se filtro ativo)"
$all | ForEach-Object {
    Write-Output "  id=$($_.id) cli=$($_.clienteId) ativos=$($_.chamadosAtivos) total=$($_.totalChamados) etiq=[$($_.etiquetasResumo)] cidade=$($_.cidade) uf=$($_.uf)"
}
$all | Select-Object -First 5 id, clienteId, clienteRazaoSocial, nome, whatsapp, cidade, uf, etiquetasResumo, chamadosAtivos | Format-Table -AutoSize

$clientesRaw = Invoke-RestMethod -Uri 'http://localhost:8080/api/clientes' -Headers $headers
if ($clientesRaw -is [System.Array]) {
    $clientes = @($clientesRaw)
} else {
    $clientes = @($clientesRaw)
}
Write-Output "CLIENTES=$($clientes.Count)"
foreach ($c in $clientes) {
    Write-Output "  cliente $($c.id) $($c.razaoSocial)"
}

$cid = $null
if ($clientes.Count -gt 0) {
    $cid = [int]$clientes[0].id
}
$byCliente = Get-Contatos "gestao=true&clienteId=$cid"
$badMix = $byCliente | Where-Object { $_.clienteId -ne $cid }
Write-Output "FILTRO_CLIENTE id=$cid count=$($byCliente.Count) mistura=$($badMix.Count)"

if ($all.Count -gt 0) {
    $nome = $all[0].nome
    if ($nome -and $nome.Length -ge 3) {
        $term = $nome.Substring(0, 3)
        $byBusca = Get-Contatos "gestao=true&busca=$([uri]::EscapeDataString($term))"
        Write-Output "BUSCA termo=$term count=$($byBusca.Count)"
    }
}

$semEt = Get-Contatos 'gestao=true&semEtiqueta=true'
Write-Output "SEM_ETIQUETA count=$($semEt.Count)"

$abertos = Get-Contatos 'gestao=true&comTicketsAbertos=true'
Write-Output "TICKETS_ABERTOS count=$($abertos.Count)"

$ruim = Get-Contatos 'gestao=true&comAvaliacaoRuim=true'
Write-Output "AVALIACAO_RUIM count=$($ruim.Count)"

$etiquetas = @(Invoke-RestMethod -Uri 'http://localhost:8080/api/etiquetas/ativas' -Headers $headers)
if ($etiquetas.Count -gt 0 -and $all.Count -gt 0) {
    $eid = $etiquetas[0].id
    $byEt = Get-Contatos "gestao=true&etiquetaId=$eid"
    Write-Output "ETIQUETA id=$eid count=$($byEt.Count)"
    if ($cid) {
        $combo = Get-Contatos "gestao=true&clienteId=$cid&etiquetaId=$eid&comTicketsAbertos=true"
        $comboBad = $combo | Where-Object { $_.clienteId -ne $cid }
        Write-Output "COMBO cliente+etiqueta+abertos count=$($combo.Count) mistura=$($comboBad.Count)"
    }
}

$cidadeSample = ($all | Where-Object { $_.cidade } | Select-Object -First 1)
if ($cidadeSample) {
    $c = $cidadeSample.cidade
    $byCidade = Get-Contatos "gestao=true&cidade=$([uri]::EscapeDataString($c))"
    Write-Output "CIDADE $c count=$($byCidade.Count)"
}
$ufSample = ($all | Where-Object { $_.uf } | Select-Object -First 1)
if ($ufSample) {
    $u = $ufSample.uf
    $byUf = Get-Contatos "gestao=true&uf=$u"
    Write-Output "UF $u count=$($byUf.Count)"
}

$byCidadeCamp = Get-Contatos 'gestao=true&cidade=Campinas'
Write-Output "CIDADE Campinas count=$($byCidadeCamp.Count) (esperado >=1 apos seed)"

$wa = Get-Contatos 'gestao=true&busca=5511980010111'
Write-Output "BUSCA_WHATSAPP count=$($wa.Count) (esperado 1)"

$ruim2 = Get-Contatos 'gestao=true&comAvaliacaoRuim=true'
Write-Output "AVALIACAO_RUIM_FINAL count=$($ruim2.Count) (esperado >=1 apos seed)"

$et1 = Get-Contatos 'gestao=true&etiquetaId=1'
Write-Output "ETIQUETA_1_FINAL count=$($et1.Count) (esperado >=1 apos seed)"

$combo87 = Get-Contatos 'gestao=true&clienteId=87&etiquetaId=1&comTicketsAbertos=true'
$combo87Bad = $combo87 | Where-Object { $_.clienteId -ne 87 }
Write-Output "COMBO87 cliente+etiqueta+abertos count=$($combo87.Count) mistura=$($combo87Bad.Count) (esperado 1 e 0)"
