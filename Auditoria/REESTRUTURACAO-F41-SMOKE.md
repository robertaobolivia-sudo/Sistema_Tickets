# Sprint F41 — Smoke final e marco REESTRUTURAÇÃO CONCLUÍDA

**Data:** 2026-05-23  
**Tipo:** validação pós F24–F40 (sem feature nova)

---

## Marco

**REESTRUTURAÇÃO CONCLUÍDA** — Cliente → Contato → WhatsApp Matriz → Ticket operacional; legado de Carteira/Conexão/ContatoCliente/TicketEtiqueta/`ticket.conexao` fora do runtime de Ticket, Cliente e Chats.

---

## Smoke (evidência)

| # | Item | Evidência |
|---|------|-----------|
| 1–3 | Login, Dashboard, Clientes | App HTTP 200; rotas estáticas; Vitest clientes/list/form |
| 4–5 | CRUD Cliente; sem Carteira | `ClienteServiceClientePuroF40Test`; DTO/front sem `carteiraId` |
| 6–7 | Contatos reais | Domínio `Contato` + tela Clientes (F38 sem ContatoCliente) |
| 8–9 | Abrir Ticket manual | `TicketAberturaOrigemTest.aberturaManual_*` → `clienteId`, `contatoId`, `ATIVO_MANUAL` |
| 10–12 | Receptivo WhatsApp | `TicketAberturaOrigemTest` + `TicketOrigemCriacaoF16Test` → `RECEPTIVO_WHATSAPP`, matriz |
| 13–15 | Chats; sem Carteira/Conexão/Revenda | `chats-view.test.js`; `CHATS_PANEL_FORBIDDEN_LABELS`; grep Chats sem `/api/carteiras` |
| 16 | Header Chats Cliente/gradiente | Diretrizes 160–161 + `arteHeaderChatsUrl` em Cliente |
| 17–18 | Etiquetas ContatoEtiqueta | Runtime Chats por `contatoId`; legado ticket_etiquetas só patches/backfill |
| 19–21 | Relatórios origem; CSV sem conexão | `query-params.test.js` `origemTicket`; grep relatórios sem `conexao` |
| 22 | PDF sem conexão operacional | `TicketPdfServiceF26Test` |
| 23–24 | Config Conexões/Revendas; Etiquetas | Mantidas isoladas (`/api/carteiras`, config section) |
| 25–26 | Console/Network Chats | Sem `carteiraService` em `features/chats` |
| 27 | HTTP 200 | Boot jar local |

**Browser E2E completo:** não executado nesta sprint (sem credencial seed documentada no repo); substituído por testes automatizados + HTTP 200.

---

## Grep final (runtime `src/`)

| Padrão | Runtime Ticket/Cliente/Chats | Permitido |
|--------|------------------------------|-----------|
| `carteira_id` | — | `ClienteCarteiraDropF40Patch` |
| `contatos_clientes` / `contatoSolicitante` | — | F38 patch; testes assert ausência payload |
| `ticket_etiquetas` | — | F34 patch; backfill one-shot |
| `getConexao` / `ticket.conexao` / `dashboard-conexao` / `chatsConexao` | **0** em `src/main` JS/Java operacional | — |
| Chats `Revenda`/`Conexão` | Só lista **proibida** (`CHATS_PANEL_FORBIDDEN_LABELS`) | — |

---

## Testes executados

- `mvn test` — OK (target rebuild Windows)
- Vitest — 38 files, 217/217
- `mvn package -DskipTests` — OK
- HTTP `/` — 200

---

## Fora do escopo (confirmado)

- Carteira global / Config Conexões — mantidas
- WhatsApp provider real — não implementado
- C04/race Chats — não tratado

---

## Riscos residuais

- Docs históricas (`PLANO_COMPATIBILIDADE`, `COLETA_*`, README schema) citam legado
- Smoke visual manual recomendado pós-deploy
- Tabelas backup F34/F38/F40 em DEV
