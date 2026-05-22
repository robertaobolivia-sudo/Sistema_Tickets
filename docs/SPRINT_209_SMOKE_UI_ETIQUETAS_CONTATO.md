# Sprint 209 — Smoke UI: etiquetas por Contato no Chats

**Backup:** `C:\Users\João Falcone\Desktop\Sistemas_BKP\BKP_Sprint_209_Etiquetas_Contato`  
**Roteiro base:** `scripts/smoke-ui-chats-dois-contatos.md` (seção Etiquetas abaixo)

## Massa

| Item | Valor |
|------|--------|
| Cliente | id **69** |
| Matriz | id **2** |
| Ticket Contato 8 | **TK-000086** (ABERTO) |
| Ticket Contato 9 | **TK-000087** (ABERTO) |
| Etiqueta | **Smoke Contato** (id **1**, ativa) |

## Instrumentação

`data-testid` adicionados (sem mudança visual):

- `chats-etiquetas-list`, `chats-etiquetas-save`, `chats-etiquetas-legacy-hint`

## Resultado — Contato 8 (TK-000086)

| Critério | Resultado |
|----------|-----------|
| Aplicar “Smoke Contato” no Chats | OK |
| Salvar | OK — feedback “Etiquetas salvas com sucesso.” |
| Endpoint | **PUT `/api/contatos/8/etiquetas`** (não ticket) |
| Persistência ao trocar ticket e voltar | OK — chip `selected` / `aria-selected=true` |
| Hint legado | Oculto (ticket com `contatoId`) |

## Resultado — Contato 9 (TK-000087)

| Critério | Resultado |
|----------|-----------|
| Etiqueta do Contato 8 visível/selecionada no 9 | **Não** — `aria-selected=false` |
| Painel Contato | OK — Contato 9 (Numero Matriz) |
| GET ao abrir | **`/api/contatos/9/etiquetas`** |
| Mistura entre contatos | **Não observada** |

## Mesmo Contato em outro ticket

Não havia outro ticket **ABERTO** do Contato 8 além do TK-000086 no ambiente. Regra coberta por API (vínculo em `contato_etiquetas`) e por testes JS (`chatsView.test.js` — contexto `contato:{id}`).

## Fallback legado (ticket sem `contatoId`)

Massa manual não disponível (nenhum ticket ABERTO sem `contatoId` na listagem). Preservado em código e teste automatizado:

- `resolveChatsEtiquetasSource` → `ticket` + hint `CHATS_ETIQUETAS_LEGACY_HINT`
- `chatsView.test.js` — “ticket sem contatoId mantém fallback legado”

## Console

Sem erro crítico no fluxo CDP assistido.

## Ajustes de código

- `index.html` — `data-testid` na seção Etiquetas do painel Chats

## Testes

- `npm test` — 137 OK
- `mvn clean install -DskipTests` — OK (JAR com static atualizado)
- HTTP **200** — `http://localhost:8080/`

## Pendências (fechadas na Sprint 210)

Ver `docs/SPRINT_210_SMOKE_ETIQUETAS_CONTATO_FALLBACK.md` — outro ticket (**TK-000088**) e fallback **TK-000020**.
