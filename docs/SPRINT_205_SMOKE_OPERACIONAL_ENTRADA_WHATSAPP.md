# Sprint 205 — Smoke operacional: entrada WhatsApp (API real)

**Data:** 2026-05-21  
**Backup:** `C:\Users\João Falcone\Desktop\Sistemas_BKP\BKP_Sprint_205_Smoke_Operacional_WhatsApp`  
**Script:** `scripts/smoke-entrada-whatsapp.ps1`

## Objetivo

Repetir o fluxo **Matriz → Cliente → Contato → Ticket** com sessão ADMIN real, IDs do banco e validação via `GET /api/tickets/{numero}`; em seguida validar **Chats** manualmente.

## Pré-requisitos

1. App rodando (`java -jar target/suporte-tickets-1.0.0.jar` ou IDE), HTTP 200 em `/`.
2. MySQL `suporte_tickets` acessível (mesma config de `application.properties`).
3. Analista **ADMIN** ativo (credenciais locais — não commitar senha no script).
4. PowerShell 5.1+ (Windows).

## Configuração do script

```powershell
cd C:\Users\João Falcone\Desktop\Sistema\suporte-tickets
$env:SMOKE_BASE_URL = 'http://localhost:8080'
$env:SMOKE_ADMIN_EMAIL = 'seu-admin@email.com'
$env:SMOKE_ADMIN_SENHA = 'sua-senha'
# Opcional: buscar cliente por nome parcial
$env:SMOKE_CLIENTE_NOME_LIKE = 'Fenix'
.\scripts\smoke-entrada-whatsapp.ps1
```

Ou parâmetros: `.\scripts\smoke-entrada-whatsapp.ps1 -Email '...' -Senha '...'`

O script:

- Faz login (`POST /api/analistas/login`) e usa headers `X-Analista-Id` / `X-Analista-Token`.
- Resolve **Cliente** (nome contendo `Fenix` ou cria “Cliente Smoke 205 …”).
- Resolve **WhatsApp Matriz** ativa (`GET /api/whatsapp-matrizes?clienteId=`) ou cria matriz de teste.
- Executa payloads **A**, **B**, **C**.
- Valida ticket com `GET /api/tickets/{numeroTicket}` (`clienteId`, `contatoId`, `whatsappMatrizId`).

## Massa de teste (execução 2026-05-21, ambiente local)

| Campo | Valor |
|-------|--------|
| clienteId | 69 |
| clienteNome | Cliente Smoke 205 171201 |
| whatsappMatrizId | 2 |
| numeroMatriz | 551198877665544 |
| telefoneContatoNovo / reuso | 5511928883358 |
| telefoneContato C | 5511900086124 |
| ticketPayloadA / B | TK-000083 |
| contatoId (apos A) | 4 |

> Fênix não encontrado no banco; usado `SMOKE_CLIENTE_ID=69` (cliente de teste da sprint). Para Fênix real: cadastrar matriz ativa e omitir `SMOKE_CLIENTE_ID`.

## Payloads

### A — contato novo (`whatsappMatrizId`)

```json
{
  "whatsappMatrizId": "<id>",
  "telefone": "55119XXXXXXXX",
  "nomeContato": "Smoke 205 Contato Novo",
  "mensagem": "Mensagem smoke 205 payload A",
  "canal": "WHATSAPP",
  "origemExternaId": "smoke-205-a-<sufixo>"
}
```

**Esperado:** HTTP 201, `ticketCriado=true`, `contatoId` e `whatsappMatrizId` no ticket.

### B — mesmo contato

Mesma matriz e **mesmo telefone** de A, nova mensagem.

**Esperado:** HTTP 200, `ticketCriado=false`, `mensagemRegistrada=true`, mesmo `numeroTicket` se ticket A ainda ativo (ABERTO/EM_ATENDIMENTO).

### C — `numeroMatriz`

```json
{
  "numeroMatriz": "<numero normalizado da matriz>",
  "telefone": "<outro 55119XXXXXXXX>",
  "nomeContato": "Smoke 205 Numero Matriz",
  "mensagem": "Mensagem smoke 205 payload C",
  "canal": "WHATSAPP",
  "origemExternaId": "smoke-205-c-<sufixo>"
}
```

**Esperado:** matriz resolvida por número, ticket criado com `cliente_id` do contratante.

## Resultado API (script executado com sucesso)

| Cenário | HTTP | ticketCriado | numeroTicket | contato_id OK | whatsapp_matriz_id OK | cliente_id OK |
|---------|------|--------------|--------------|---------------|------------------------|---------------|
| A | 200 | true | TK-000083 | 4 | 2 | 69 |
| B | 200 | false | TK-000083 | (reuso) | (reuso) | (reuso) |
| C | 200 | false | TK-000083 | ver nota | ver nota | ver nota |

**Nota C (corrigida na Sprint 206):** outro Contato no mesmo Cliente deve **criar ticket novo**. Ver `docs/SPRINT_206_SMOKE_TICKET_ATIVO_CONTATO.md`.

## Validação Chats (manual)

1. Login ADMIN no navegador.
2. **Chats → Fila** — localizar ticket do payload A (status ABERTO / EM_ATENDIMENTO / AGUARDANDO_CLIENTE).
3. Abrir conversa — painel direito:
   - **Cliente** = contratante da matriz;
   - **Contato** = nome/telefone do smoke;
   - **Entrada do atendimento** = número/nome da matriz;
   - **Chamado atual** = protocolo do ticket.
4. Timeline com texto das mensagens A e B.
5. Console sem erro crítico.

| Item | OK / Pendente | Observação |
|------|---------------|------------|
| Ticket na Fila | | |
| Cliente no painel | | |
| Contato no painel | | |
| Entrada matriz | | |
| Timeline mensagem A | | |
| Timeline mensagem B | | |

## Bugs encontrados / ajustes

| # | Descrição | Ajuste |
|---|-----------|--------|
| 1 | Script travava com `Invoke-WebRequest` no login | Login direto com `Invoke-RestMethod` |
| 2 | `whatsappMatrizId=0` ao criar matriz com número longo/inválido | Número matriz no padrão `55119` + 8 dígitos; validação `id > 0` |
| 3 | Parser PowerShell quebrava com caracter `—` (em dash) | Substituído por `-` nos rótulos |
| 4 | Payload C anexa ao ticket ativo do cliente (regra `TicketAtivoService`) | Documentado; sem alteração de regra nesta sprint |

## Pendências

- Preencher tabela “Massa” e “Resultado API” após cada execução do script.
- Se payload B criar ticket novo: verificar se A já foi encerrado ou regra de ticket ativo (`TicketAtivoService`).
- Sprint 206: automação browser (MCP) no Chats ou `@SpringBootTest` com banco de teste.

## Referências

- Sprint 204: `docs/SPRINT_204_SMOKE_ENTRADA_WHATSAPP.md`
- Integração: `docs/WHATSAPP_MATRIZ.md`
- Chats: `docs/CHATS_DIRETRIZES.md`
