# Sprint 207 — Smoke browser: Chats com dois Contatos

**Backup:** `C:\Users\João Falcone\Desktop\Sistemas_BKP\BKP_Sprint_207_Smoke_Browser_Chats`  
**Script:** `scripts/smoke-entrada-whatsapp.ps1` (Sprint 205/206)

## Objetivo

Validar no produto (Chats) que a correção da Sprint 206 se mantém: **dois Contatos** do mesmo Cliente → **dois tickets** na Fila, painéis e timelines **sem mistura**.

## Massa (execução 2026-05-21)

| Campo | Valor |
|-------|--------|
| Cliente | id **69** — Cliente Smoke 205 171201 |
| WhatsApp Matriz | id **2** — `551198877665544` |
| Contato 1 | id **8** — Smoke 205 Contato Novo — `5511963978963` |
| Contato 2 | id **9** — Smoke 205 Numero Matriz — `5511911109775` |
| Ticket 1 | **TK-000086** (ABERTO) |
| Ticket 2 | **TK-000087** (ABERTO) |

## Preparação

```powershell
$env:SMOKE_CLIENTE_ID = '69'
$env:SMOKE_ADMIN_EMAIL = '<admin>'
$env:SMOKE_ADMIN_SENHA = '<senha>'
.\scripts\smoke-entrada-whatsapp.ps1
```

Resultado esperado do script: A cria 086, B reutiliza 086, **C cria 087** (ticket diferente).

## Validação API (dados que alimentam o Chats)

**Status:** concluída (API 207); UI fechada na **Sprint 208** — ver `docs/SPRINT_208_SMOKE_UI_CHATS_DOIS_CONTATOS.md`.

Conferido via `GET /api/tickets/{numero}` e `GET /api/tickets/{numero}/interacoes` com sessão ADMIN:

| Ticket | contatoId | contatoNome | whatsapp contato | matriz | Interações relevantes |
|--------|-----------|-------------|------------------|--------|------------------------|
| TK-000086 | 8 | Smoke 205 Contato Novo | 5511963978963 | 551198877665544 | ABERTURA payload A; MENSAGEM_CLIENTE payload B |
| TK-000087 | 9 | Smoke 205 Numero Matriz | 5511911109775 | 551198877665544 | ABERTURA payload C apenas |

**Conclusão API:** não há mensagem do Contato 2 no ticket do Contato 1 nem o contrário. Campos `contatoId`, `contatoNome`, `contatoWhatsapp`, `whatsappMatrizNumero` corretos para o painel direito.

`GET /api/tickets` (escopo ALL, Chats) inclui **TK-000086** e **TK-000087** com `contatoId` 8 e 9 e nomes distintos — base da Fila.

## Smoke browser — checklist (Sprint 208)

Roteiro reproduzível: `scripts/smoke-ui-chats-dois-contatos.md`. Resultado UI: **OK** (Fila, painel, timeline sem mistura; `data-testid` adicionados).

### Fila — OK

TK-000086 e TK-000087 visíveis como cards separados (`chats-card-*`).

### Ticket TK-000086 — OK

Contato 8, matriz na Entrada, timeline A/B sem C.

### Ticket TK-000087 — OK

Contato 9, matriz na Entrada, timeline só C.

### Etiquetas — pendente

Não testadas na 208.

## Bugs encontrados

| # | Descrição | Ajuste |
|---|-----------|--------|
| — | Nenhum bug de mistura Contato/ticket na API após Sprint 206 | — |

## Pendências

- Preencher colunas OK/Pendente após passo manual no navegador.
- Screenshots opcionais na pasta do projeto, se desejado.

## Referências

- `docs/SPRINT_206_SMOKE_TICKET_ATIVO_CONTATO.md`
- `docs/CHATS_DIRETRIZES.md` (painel Cliente / Contato / Entrada / Chamado)
