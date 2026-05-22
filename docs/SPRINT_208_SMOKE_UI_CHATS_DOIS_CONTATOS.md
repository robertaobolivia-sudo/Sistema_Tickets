# Sprint 208 — Smoke UI reproduzível: Chats com dois contatos

**Backup:** `C:\Users\João Falcone\Desktop\Sistemas_BKP\BKP_Sprint_208_Smoke_UI_Chats`  
**Roteiro:** `scripts/smoke-ui-chats-dois-contatos.md`  
**Referência:** `docs/SPRINT_207_SMOKE_BROWSER_CHATS_DOIS_CONTATOS.md`

## Massa

| Item | Valor |
|------|--------|
| Cliente | id **69** |
| Matriz | id **2** — `551198877665544` |
| Contato 8 | Smoke 205 Contato Novo → **TK-000086** (ABERTO) |
| Contato 9 | Smoke 205 Numero Matriz → **TK-000087** (ABERTO) |

## Instrumentação (`data-testid`)

Adicionados sem alteração visual:

- Login: `login-email`, `login-password`, `login-submit`
- Navegação: `nav-chats`, `chats-tab-fila`, `chats-list`
- Cards: `chats-card-{numeroTicket}` (gerado em `chatsPage.js`)
- Painel: `chats-panel-cliente`, `chats-panel-contato`, `chats-panel-entrada`, `chats-panel-chamado`
- Timeline: `chats-timeline`

## Resultado do smoke UI (2026-05-21)

Automação via browser CDP após login com seletores estáveis.

| Critério | Resultado |
|----------|-----------|
| Login ADMIN | OK |
| Chats + aba Fila | OK |
| Cards TK-000086 e TK-000087 na Fila | OK (21 cards na fila no ambiente) |
| Painel TK-086 — Contato 8 | OK (`Smoke 205 Contato Novo`) |
| Painel TK-086 — Matriz na Entrada | OK |
| Timeline TK-086 — A e B, sem C | OK |
| Painel TK-087 — Contato 9 | OK |
| Painel TK-087 — Matriz na Entrada | OK |
| Timeline TK-087 — só C, sem A/B | OK (aguardar carregamento ao trocar ticket) |
| Console erro crítico | Nenhum capturado no fluxo |

**Observação:** ao trocar de ticket, aguardar ~2s antes de ler a timeline; leitura imediata pode ainda refletir o ticket anterior.

## Etiquetas

Testadas na **Sprint 209** — `docs/SPRINT_209_SMOKE_UI_ETIQUETAS_CONTATO.md`.

## Ajustes de código

- `index.html` — `data-testid` login, nav Chats, aba Fila, lista, painéis, timeline
- `chatsPage.js` — `data-testid="chats-card-{numero}"` nos itens da lista

## Testes

- `npm test` — 137 testes OK
- `mvn clean install -DskipTests` — OK (static embutido no JAR)
- HTTP 200 em `http://localhost:8080/`

## Pendências

- Etiquetas por contato (smoke manual).
- Playwright/Cypress formal (opcional), reutilizando seletores do roteiro.
