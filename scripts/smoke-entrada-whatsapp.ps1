# Smoke operacional — entrada WhatsApp (Sprint 205)
# Uso local/dev. Requer app em http://localhost:8080 e MySQL com dados de analista ADMIN.
#
# Variáveis de ambiente (opcional):
#   SMOKE_BASE_URL          (default http://localhost:8080)
#   SMOKE_ADMIN_EMAIL       (obrigatório se não usar -Email)
#   SMOKE_ADMIN_SENHA       (obrigatório se não usar -Senha)
#   SMOKE_CLIENTE_NOME_LIKE (default Fenix — busca parcial no nome do cliente)

param(
    [string]$BaseUrl = $(if ($env:SMOKE_BASE_URL) { $env:SMOKE_BASE_URL } else { 'http://localhost:8080' }),
    [string]$Email = $env:SMOKE_ADMIN_EMAIL,
    [string]$Senha = $env:SMOKE_ADMIN_SENHA,
    [string]$ClienteNomeLike = $(if ($env:SMOKE_CLIENTE_NOME_LIKE) { $env:SMOKE_CLIENTE_NOME_LIKE } else { 'Fenix' }),
    [int]$ClienteIdFixo = $(if ($env:SMOKE_CLIENTE_ID) { [int]$env:SMOKE_CLIENTE_ID } else { 0 })
)

$ErrorActionPreference = 'Stop'

function Write-Step([string]$msg) { Write-Host "`n==> $msg" -ForegroundColor Cyan }
function Write-Ok([string]$msg) { Write-Host "OK: $msg" -ForegroundColor Green }
function Write-Warn([string]$msg) { Write-Host "AVISO: $msg" -ForegroundColor Yellow }
function Write-Fail([string]$msg) { Write-Host "FALHA: $msg" -ForegroundColor Red }

if (-not $Email -or -not $Senha) {
    Write-Fail 'Defina SMOKE_ADMIN_EMAIL e SMOKE_ADMIN_SENHA (ou parametros -Email / -Senha).'
    Write-Host 'Exemplo: $env:SMOKE_ADMIN_EMAIL="admin@exemplo.com"; $env:SMOKE_ADMIN_SENHA="***"; .\scripts\smoke-entrada-whatsapp.ps1'
    exit 1
}

$api = "$BaseUrl/api"
$sessionHeaders = @{}

function Invoke-SmokeJson {
    param(
        [string]$Method,
        [string]$Uri,
        [object]$Body = $null,
        [hashtable]$ExtraHeaders = @{}
    )
    $headers = @{ 'Content-Type' = 'application/json' }
    foreach ($k in $sessionHeaders.Keys) { $headers[$k] = $sessionHeaders[$k] }
    foreach ($k in $ExtraHeaders.Keys) { $headers[$k] = $ExtraHeaders[$k] }
    $params = @{
        Method      = $Method
        Uri         = $Uri
        Headers     = $headers
        TimeoutSec  = 60
        ErrorAction = 'Stop'
    }
    if ($null -ne $Body) {
        $params.Body = ($Body | ConvertTo-Json -Depth 6 -Compress)
    }
    try {
        $parsed = Invoke-RestMethod @params
        return @{ Status = 200; Raw = ($parsed | ConvertTo-Json -Compress); Json = $parsed }
    } catch {
        $status = 500
        $raw = $_.Exception.Message
        if ($_.ErrorDetails -and $_.ErrorDetails.Message) {
            $raw = $_.ErrorDetails.Message
            try {
                $parsed = $raw | ConvertFrom-Json
                if ($parsed.status) { $status = [int]$parsed.status }
            } catch { $parsed = $null }
            return @{ Status = $status; Raw = $raw; Json = $parsed }
        }
        if ($_.Exception.Response) {
            $status = [int]$_.Exception.Response.StatusCode.value__
        }
        return @{ Status = $status; Raw = $raw; Json = $null }
    }
}

# Telefones únicos por execução (13 dígitos BR: 55 + DDD 11 + 9 + 8 dígitos)
$suffix = (Get-Date).ToString('HHmmss')
$rand8 = '{0:D8}' -f (Get-Random -Minimum 0 -Maximum 100000000)
$telNovo = "55119$rand8"
$telReuso = $telNovo
$rand8b = '{0:D8}' -f (Get-Random -Minimum 0 -Maximum 100000000)
$telMatriz = "55119$rand8b"

Write-Step "Login ADMIN"
$loginBody = @{ email = $Email; senha = $Senha } | ConvertTo-Json -Compress
$loginJson = Invoke-RestMethod -Uri "$api/analistas/login" -Method Post -ContentType 'application/json' -Body $loginBody -TimeoutSec 60
$login = @{ Status = 200; Json = $loginJson }
if (-not $login.Json.id -or -not $login.Json.authToken) {
    Write-Fail "Login falhou (HTTP $($login.Status)). Verifique credenciais e se a app está no ar."
    exit 1
}
$sessionHeaders['X-Analista-Id'] = [string]$login.Json.id
$sessionHeaders['X-Analista-Token'] = [string]$login.Json.authToken
Write-Ok "Sessao analista id=$($login.Json.id)"

Write-Step "Resolver Cliente contratante (like '$ClienteNomeLike')"
$cliente = $null
if ($ClienteIdFixo -gt 0) {
    $one = Invoke-SmokeJson -Method GET -Uri "$api/clientes/$ClienteIdFixo"
    if ($one.Status -eq 200 -and $one.Json) {
        $cliente = $one.Json
        Write-Ok "Cliente fixo id=$ClienteIdFixo"
    }
}
if (-not $cliente) {
    $clientes = Invoke-SmokeJson -Method GET -Uri "$api/clientes"
    if ($clientes.Status -ne 200) {
        Write-Fail "Listar clientes HTTP $($clientes.Status)"
        exit 1
    }
    foreach ($c in @($clientes.Json)) {
        if (-not $c.nome) { continue }
        if ($c.nome -like "*$ClienteNomeLike*" -or $c.nome -like '*F*ni*') {
            $cliente = $c
            break
        }
    }
    if (-not $cliente) {
        Write-Warn "Cliente nao encontrado; criando Cliente Smoke 205 $suffix"
        $novo = Invoke-SmokeJson -Method POST -Uri "$api/clientes" -Body @{
            nome            = "Cliente Smoke 205 $suffix"
            telefone        = "110000$suffix"
            telefoneContato = "110001$suffix"
            email           = "smoke205_$suffix@teste.local"
        }
        if ($novo.Status -ge 400 -or -not $novo.Json) {
            Write-Fail "Criar cliente HTTP $($novo.Status) - $($novo.Raw)"
            exit 1
        }
        $cliente = $novo.Json
    }
}
$clienteId = [int]$cliente.id
$clienteNome = $cliente.nome
Write-Ok "clienteId=$clienteId nome=$clienteNome"

Write-Step "Resolver WhatsApp Matriz ativa"
$matrizes = Invoke-SmokeJson -Method GET -Uri "$api/whatsapp-matrizes?clienteId=$clienteId"
if ($matrizes.Status -ne 200) {
    Write-Fail "Listar matrizes HTTP $($matrizes.Status)"
    exit 1
}
$matriz = $matrizes.Json | Where-Object { $_.ativo -eq $true } | Select-Object -First 1
if (-not $matriz) {
    $numeroMatriz = "55119$rand8"
    Write-Warn "Criando matriz de teste numero=$numeroMatriz"
    $criada = Invoke-SmokeJson -Method POST -Uri "$api/whatsapp-matrizes" -Body @{
        clienteId = $clienteId
        nome      = "Matriz Smoke 205"
        numero    = $numeroMatriz
        ativo     = $true
    }
    if ($criada.Status -ge 400 -or -not $criada.Json -or -not $criada.Json.id) {
        Write-Fail "Criar matriz HTTP $($criada.Status) - $($criada.Raw)"
        exit 1
    }
    $matriz = $criada.Json
}
$matrizId = [int]$matriz.id
$numeroMatrizNorm = $matriz.numero
if (-not $numeroMatrizNorm) { $numeroMatrizNorm = $matriz.numeroNormalizado }
if ($matrizId -le 0 -or -not $numeroMatrizNorm) {
    Write-Fail "WhatsApp Matriz invalida (id=$matrizId). Verifique criacao/listagem."
    exit 1
}
Write-Ok "whatsappMatrizId=$matrizId numeroMatriz=$numeroMatrizNorm"

function Send-Mensagem([hashtable]$payload, [string]$label) {
    Write-Step $label
    $r = Invoke-SmokeJson -Method POST -Uri "$api/integracoes/whatsapp/mensagens" -Body $payload
    $ok = $r.Status -in 200, 201
    Write-Host "HTTP $($r.Status)"
    if ($r.Json) {
        $r.Json | ConvertTo-Json -Compress | Write-Host
    } else {
        Write-Host $r.Raw
    }
    if (-not $ok) {
        Write-Fail $label
        return $null
    }
    return $r.Json
}

function Assert-Ticket([string]$numero, [int]$expectedClienteId, [int]$expectedMatrizId) {
    if (-not $numero) { return $false }
    $t = Invoke-SmokeJson -Method GET -Uri "$api/tickets/$([uri]::EscapeDataString($numero))"
    if ($t.Status -ne 200) {
        Write-Fail "GET ticket $numero HTTP $($t.Status)"
        return $false
    }
    $ok = $true
    if ([int]$t.Json.clienteId -ne $expectedClienteId) {
        Write-Fail "cliente_id esperado $expectedClienteId obtido $($t.Json.clienteId)"
        $ok = $false
    }
    if (-not $t.Json.contatoId) {
        Write-Fail 'contato_id vazio no ticket'
        $ok = $false
    } else {
        Write-Ok "contato_id=$($t.Json.contatoId)"
    }
    if ($expectedMatrizId -and [int]$t.Json.whatsappMatrizId -ne $expectedMatrizId) {
        Write-Fail "whatsapp_matriz_id esperado $expectedMatrizId obtido $($t.Json.whatsappMatrizId)"
        $ok = $false
    } else {
        Write-Ok "whatsapp_matriz_id=$($t.Json.whatsappMatrizId)"
    }
    Write-Ok "Ticket $numero status=$($t.Json.status) cliente=$($t.Json.cliente)"
    return $ok
}

$origA = "smoke-205-a-$suffix"
$resA = Send-Mensagem @{
    whatsappMatrizId  = $matrizId
    telefone          = $telNovo
    nomeContato       = "Smoke 205 Contato Novo"
    mensagem          = "Mensagem smoke 205 payload A $suffix"
    canal             = 'WHATSAPP'
    origemExternaId   = $origA
} 'Payload A - contato novo (whatsappMatrizId)'

if (-not $resA -or -not $resA.ticketCriado) {
    Write-Fail 'Payload A deveria criar ticket (ticketCriado=true)'
    exit 1
}
$ticketA = $resA.numeroTicket
Assert-Ticket -numero $ticketA -expectedClienteId $clienteId -expectedMatrizId $matrizId | Out-Null

$contatosPosA = Invoke-SmokeJson -Method GET -Uri "$api/contatos?clienteId=$clienteId"
$contatoA = $contatosPosA.Json | Where-Object { $_.whatsapp -eq $telNovo } | Select-Object -First 1
if ($contatoA) {
    Write-Ok "Contato apos A: id=$($contatoA.id) whatsapp=$($contatoA.whatsapp)"
} else {
    Write-Warn 'Contato nao listado por telefone exato (verificar normalizacao no GET contatos)'
}

$resB = Send-Mensagem @{
    whatsappMatrizId  = $matrizId
    telefone          = $telReuso
    nomeContato       = 'Smoke 205 Contato Novo'
    mensagem          = "Mensagem smoke 205 payload B reuso $suffix"
    canal             = 'WHATSAPP'
    origemExternaId   = "smoke-205-b-$suffix"
} 'Payload B - mesmo contato (ticket ativo)'

if (-not $resB) { exit 1 }
if ($resB.ticketCriado) {
    Write-Warn 'Payload B criou novo ticket (esperado reutilizar ativo se A ainda ABERTO)'
} else {
    Write-Ok "Payload B reutilizou ticket $($resB.numeroTicket) mensagemRegistrada=$($resB.mensagemRegistrada)"
    if ($resB.numeroTicket -ne $ticketA) {
        Write-Warn "Numero ticket B ($($resB.numeroTicket)) diferente de A ($ticketA)"
    }
}

$resC = Send-Mensagem @{
    numeroMatriz      = $numeroMatrizNorm
    telefone          = $telMatriz
    nomeContato       = 'Smoke 205 Numero Matriz'
    mensagem          = "Mensagem smoke 205 payload C $suffix"
    canal             = 'WHATSAPP'
    origemExternaId   = "smoke-205-c-$suffix"
} 'Payload C - numeroMatriz'

if (-not $resC) { exit 1 }
if (-not $resC.ticketCriado) {
    Write-Fail "Payload C (outro Contato) deveria criar ticket novo, nao reutilizar $($resC.numeroTicket)"
    exit 1
}
if ($resC.numeroTicket -eq $ticketA) {
    Write-Fail 'Payload C retornou o mesmo numero do Contato A — regra Cliente+Contato violada (Sprint 206)'
    exit 1
}
Write-Ok "Payload C criou ticket $($resC.numeroTicket) para outro Contato"
Assert-Ticket -numero $resC.numeroTicket -expectedClienteId $clienteId -expectedMatrizId $matrizId | Out-Null

Write-Step 'Resumo'
Write-Host @"

Cliente: $clienteNome (id $clienteId)
WhatsApp Matriz: id $matrizId numero $numeroMatrizNorm
Telefone novo/reuso: $telNovo
Telefone payload C: $telMatriz
Ticket A: $ticketA
Ticket C: $($resC.numeroTicket)

Proximo passo manual (Chats):
  1. Login ADMIN no navegador
  2. Chats > Fila — localizar $ticketA
  3. Abrir ticket — validar Cliente, Contato, Entrada (matriz), Chamado e timeline

"@

Write-Ok 'Smoke API concluido.'
