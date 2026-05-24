# Sprint F22 / F22-B — Smoke Chats pós F20/F21

**Data:** 2026-05-23  
**Tipo:** validação / smoke (sem feature nova)

## Pré-requisitos

- App em `http://localhost:8080/` → **HTTP 200**
- Credencial DEV válida (seed Sprint 94 — analista ADMIN)
- Cache: **Ctrl+F5** ou URL com query (`?f22b=`) após login

## F22 — estático (OK)

| Item | Resultado |
|------|-----------|
| `chatsPage.js` sem `carteiraService` / `listCarteiras` | OK |
| `static/` sem `chats-conexao-*` / `chatsConexao*` | OK |
| Vitest `chatsView.test.js` (F21) | 51/51 OK |
| HTTP GET `/` | 200 OK |

## F22-B — smoke browser (2026-05-23, fechado)

**Credencial:** `robertaobolivia@gmail.com` (ADMIN, seed DEV) — senha não registrada neste doc.

| # | Critério | Resultado |
|---|----------|-----------|
| 1 | Login DEV | OK (API + sessão no app) |
| 2 | Abrir Chats | OK |
| 3 | Lista conversas | OK — Fila 3, Atendendo 4, Encerrados 11, Não atend. 6 |
| 4 | Selecionar conversa | OK — **TK-000339** (Bruno Fast), Fila |
| 5 | Header = Cliente | OK — título **Bruno Fast** (sem “Conexão não informada”) |
| 6 | Arte Cliente | N/A neste ticket — header em **gradiente** |
| 7 | Painel direito | OK — Cliente / Contato / Entrada do atendimento / Chamado atual |
| 8 | Sem Carteira/Conexão/Revenda no `#page-chats` | OK (texto visível) |
| 9 | Composer + timeline | OK |
| 10 | Network `/api/carteiras` ao **navegar para Chats** | OK — **0** chamadas (hook `fetch` ao reabrir Chats) |
| 11 | Network `/uploads/conexoes/header-chats` | OK — **0** |
| 12 | Console crítico (`chatsConexao*`, `carteiraService`) | OK — nenhum observado na sessão |

**Ctrl+F5:** reload com `?f22b=2` após login (cache bust de entrada).

**Evidência:** screenshot automação `page-2026-05-23T20-06-47-848Z.png` (Chats Fila + TK-000339).

**Nota Network:** `/api/carteiras` ainda existe no app global (ex.: Configurações). O critério F22 é **não chamar ao abrir o fluxo Chats** — confirmado no re-open Chats (`/api/tickets`, `/api/etiquetas/ativas`, `/api/chats/interacoes-pendentes` apenas).

## Script auxiliar

```powershell
$env:SMOKE_EMAIL = "robertaobolivia@gmail.com"
$env:SMOKE_SENHA = "<senha DEV>"
.\scripts\sprint-f22-smoke-chats.ps1
```

## Regressão conhecida (fora do escopo Chats)

- **Meus Tickets** — filtro label “Conexão” (outra página, DOM oculto).
- **Dashboard** — “Atendimentos por Conexão”.
- **Configurações** — “Conexões / Revendas” + `carteiraService`.

## Critério

**F22 + F22-B aprovados** para Chats pós F20/F21 (smoke operacional DEV).
